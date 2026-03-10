package com.surpasslike.calendar.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.blankj.utilcode.util.LogUtils
import com.surpasslike.calendar.data.entity.ScheduleEntity
import java.util.Calendar

/**
 * 闹钟调度器: 负责注册和取消日程提醒闹钟
 *
 * 触发时间 = startTime - reminderMinutes * 60000
 * 重复日程会计算下一次出现的触发时间
 */
object ReminderScheduler {

    private const val TAG = "ReminderScheduler"

    /**
     * 为日程注册闹钟提醒
     * @param context Context
     * @param schedule 日程实体(必须有 startTime 和 reminderMinutes)
     */
    fun scheduleAlarm(context: Context, schedule: ScheduleEntity) {
        val reminderMin = schedule.reminderMinutes ?: return
        val startTime = schedule.startTime ?: return

        LogUtils.d(TAG, "scheduleAlarm: id=${schedule.id}, title=${schedule.title}, reminderMinutes=$reminderMin")

        // 计算触发时间
        val triggerTime = if (schedule.repeatRule != null) {
            calculateNextRepeatTriggerTime(schedule, reminderMin)
        } else {
            startTime - reminderMin * 60_000L
        }

        if (triggerTime == null || triggerTime <= System.currentTimeMillis()) {
            LogUtils.d(TAG, "scheduleAlarm: 触发时间已过, 跳过注册")
            return
        }

        val intent = buildIntent(context, schedule)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        LogUtils.d(TAG, "scheduleAlarm: 已注册闹钟, triggerTime=$triggerTime")
    }

    /**
     * 取消已注册的闹钟
     * @param context Context
     * @param scheduleId 日程ID
     */
    fun cancelAlarm(context: Context, scheduleId: Long) {
        LogUtils.d(TAG, "cancelAlarm: scheduleId=$scheduleId")
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            LogUtils.d(TAG, "cancelAlarm: 已取消闹钟")
        }
    }

    /**
     * 计算重复日程下一次出现的触发时间
     * 从日程起始日期开始,按重复规则向前迭代,找到未来的触发时间
     */
    private fun calculateNextRepeatTriggerTime(schedule: ScheduleEntity, reminderMin: Int): Long? {
        val rule = schedule.repeatRule ?: return null
        val startTime = schedule.startTime ?: return null
        val now = System.currentTimeMillis()

        // 从 startTime 提取时分秒偏移
        val originCal = Calendar.getInstance().apply { timeInMillis = startTime }
        val hour = originCal.get(Calendar.HOUR_OF_DAY)
        val minute = originCal.get(Calendar.MINUTE)

        // 从今天开始向后查找,最多查 400 天
        val checkCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val originDate = schedule.date

        for (i in 0..400) {
            val targetDate = checkCal.timeInMillis
            if (targetDate >= originDate && rule.matches(originDate, targetDate)) {
                // 计算该天的触发时间
                val eventTime = Calendar.getInstance().apply {
                    timeInMillis = targetDate
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val triggerTime = eventTime - reminderMin * 60_000L
                if (triggerTime > now) {
                    return triggerTime
                }
            }
            checkCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return null
    }

    /**
     * 构建携带日程信息的 Intent
     */
    private fun buildIntent(context: Context, schedule: ScheduleEntity): Intent {
        return Intent(context, ReminderReceiver::class.java).apply {
            putExtra("schedule_id", schedule.id)
            putExtra("schedule_title", schedule.title)
            putExtra("schedule_description", schedule.description)
            putExtra("schedule_startTime", schedule.startTime)
            putExtra("schedule_date", schedule.date)
            putExtra("schedule_reminderMinutes", schedule.reminderMinutes)
            putExtra("schedule_repeatRule", schedule.repeatRule?.name)
        }
    }
}
