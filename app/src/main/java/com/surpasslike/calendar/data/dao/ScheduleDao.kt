package com.surpasslike.calendar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.surpasslike.calendar.data.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    // 增: 新增日程到仓库中
    @Insert
    suspend fun insertSchedule(scheduleEntity: ScheduleEntity): Long

    // 删: 删除仓库中的某个日程
    @Delete
    suspend fun deleteSchedule(scheduleEntity: ScheduleEntity)

    // 按 id 删除
    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)

    // 改: 修改已有日程信息
    @Update
    suspend fun updateSchedule(scheduleEntity: ScheduleEntity)

    // 查: 按日期查找,flow持续观察
    @Query("SELECT * FROM schedules WHERE date = :targetDate ORDER BY startTime ASC")
    fun observeSchedulesByDate(targetDate: Long): Flow<List<ScheduleEntity>>

    // 查: 按ID查找具体的某一个
    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): ScheduleEntity?
}