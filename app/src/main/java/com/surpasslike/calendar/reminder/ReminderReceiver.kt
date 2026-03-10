package com.surpasslike.calendar.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.LogUtils
import com.surpasslike.calendar.MainActivity
import com.surpasslike.calendar.R
import com.surpasslike.calendar.data.entity.ScheduleEntity
import com.surpasslike.calendar.utils.RepeatRule

/**
 * 闹钟触发接收器: 收到闹钟后发送通知提醒用户
 * 若为重复日程,会自动注册下一次闹钟
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ReminderReceiver"
        private const val CHANNEL_ID = "schedule_reminder"
        private const val CHANNEL_NAME = "日程提醒"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("schedule_id", -1L)
        val title = intent.getStringExtra("schedule_title") ?: "日程提醒"
        val description = intent.getStringExtra("schedule_description")
        val startTime = intent.getLongExtra("schedule_startTime", 0L)
        val date = intent.getLongExtra("schedule_date", 0L)
        val reminderMinutes = intent.getIntExtra("schedule_reminderMinutes", 0)
        val repeatRuleName = intent.getStringExtra("schedule_repeatRule")

        LogUtils.d(TAG, "onReceive: id=$id, title=$title")

        if (id == -1L) return

        // 创建通知渠道
        createNotificationChannel(context)

        // 构建点击通知打开 MainActivity 的 Intent
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, id.toInt(), contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 构建通知
        val contentText = description ?: "你有一个日程即将开始"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id.toInt(), notification)
        LogUtils.d(TAG, "onReceive: 通知已发送")

        // 重复日程: 注册下一次闹钟
        if (repeatRuleName != null) {
            val repeatRule = try {
                RepeatRule.valueOf(repeatRuleName)
            } catch (e: Exception) {
                null
            }
            if (repeatRule != null && startTime > 0 && reminderMinutes > 0) {
                val schedule = ScheduleEntity(
                    id = id,
                    title = title,
                    description = description,
                    date = date,
                    startTime = startTime,
                    repeatRule = repeatRule,
                    reminderMinutes = reminderMinutes
                )
                ReminderScheduler.scheduleAlarm(context, schedule)
                LogUtils.d(TAG, "onReceive: 已为重复日程注册下一次闹钟")
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "日程提醒通知"
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
