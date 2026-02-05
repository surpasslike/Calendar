package com.surpasslike.calendar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.surpasslike.calendar.MyApplication
import com.surpasslike.calendar.repository.ScheduleRepository

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val scheduleRepository: ScheduleRepository

    init {
        // 通过application获取数据库appDatabase和DAO,创建Repository
        val appDatabase = (application as MyApplication).appDatabase // 数据库实例,只会初始化一次,让整个应用只有一个数据库实例,避免数据混乱
        val scheduleDao = appDatabase.scheduleDao() // 存取窗口,在Repository里执行具体的SQL操作
        scheduleRepository = ScheduleRepository(scheduleDao) // 管理者,通过它在CalendarViewModel里调用增删改查
    }
}