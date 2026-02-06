package com.surpasslike.calendar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.surpasslike.calendar.MyApplication
import com.surpasslike.calendar.data.entity.ScheduleEntity
import com.surpasslike.calendar.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    // 管理日程数据的增删改查
    private val scheduleRepository: ScheduleRepository

    init {
        // 通过application获取数据库appDatabase和DAO,创建Repository
        val appDatabase = (application as MyApplication).appDatabase // 数据库实例,只会初始化一次,让整个应用只有一个数据库实例,避免数据混乱
        val scheduleDao = appDatabase.scheduleDao() // 存取窗口,在Repository里执行具体的SQL操作
        scheduleRepository = ScheduleRepository(scheduleDao) // 管理者,通过它在CalendarViewModel里调用增删改查
    }

    // 查: 获取指定日期的日程列表(含重复日程)
    // @param targetDate 该日期的时间戳,精确到日(当天零点),不精确到时分秒
    // @return Flow<List<ScheduleEntity>> 会持续发射数据变化
    fun getSchedulesByDate(targetDate: Long): Flow<List<ScheduleEntity>> {
        return scheduleRepository.observeSchedulesByDate(targetDate)
    }

    // 增: 新增日程
    // viewModelScope: ViewModel自带的协程作用域, ViewModel销毁时自动取消, 不会内存泄漏
    // launch: 启动一个协程, 因为 insertSchedule 是 suspend 函数, 必须在协程中调用
    fun insertSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            scheduleRepository.insertSchedule(schedule)
        }
    }

    // 删: 删除日程
    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            scheduleRepository.deleteSchedule(schedule)
        }
    }

    // 删: 按id删除日程
    fun deleteScheduleById(id: Long) {
        viewModelScope.launch {
            scheduleRepository.deleteScheduleById(id)
        }
    }

    // 改: 修改日程
    fun updateSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            scheduleRepository.updateSchedule(schedule)
        }
    }

}