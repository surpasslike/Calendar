package com.surpasslike.calendar.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blankj.utilcode.util.LogUtils
import com.surpasslike.calendar.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 开机广播接收器: 设备重启后重新注册所有日程闹钟
 * 因为 AlarmManager 注册的闹钟在设备重启后会丢失
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        LogUtils.d(TAG, "onReceive: 设备重启, 开始重新注册闹钟")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getInstance(context).scheduleDao()
                val schedules = dao.getSchedulesWithReminder()
                LogUtils.d(TAG, "onReceive: 查询到 ${schedules.size} 个需要提醒的日程")
                schedules.forEach { schedule ->
                    ReminderScheduler.scheduleAlarm(context, schedule)
                }
                LogUtils.d(TAG, "onReceive: 闹钟重新注册完成")
            } catch (e: Exception) {
                LogUtils.e(TAG, "onReceive: 重新注册闹钟失败", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
