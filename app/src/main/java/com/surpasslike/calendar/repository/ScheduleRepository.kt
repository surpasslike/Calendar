package com.surpasslike.calendar.repository

import com.surpasslike.calendar.data.dao.ScheduleDao
import com.surpasslike.calendar.data.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val scheduleDao: ScheduleDao) {

    suspend fun insertSchedule(scheduleEntity: ScheduleEntity): Long {
        return scheduleDao.insertSchedule(scheduleEntity)
    }

    suspend fun deleteSchedule(schedule: ScheduleEntity) {
        scheduleDao.deleteSchedule(schedule)
    }

    suspend fun deleteScheduleById(id: Long) {
        scheduleDao.deleteScheduleById(id)
    }

    suspend fun updateSchedule(schedule: ScheduleEntity) {
        scheduleDao.updateSchedule(schedule)
    }

    fun observeSchedulesByDate(date: Long): Flow<List<ScheduleEntity>> {
        return scheduleDao.observeSchedulesByDate(date)
    }

    suspend fun getScheduleById(id: Long): ScheduleEntity? {
        return scheduleDao.getScheduleById(id)
    }
}