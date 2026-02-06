package com.surpasslike.calendar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.surpasslike.calendar.data.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

/*
* 数据访问对象,定义数据库的操作方法:增删改查
* */
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

    // 查: 按日期查找(含重复日程),flow持续观察
    // @param targetDate: Long毫秒时间戳(当天零点), 如 1738972800000L 代表 2026-02-09 00:00:00
    // 条件1: date = targetDate → 查出当天的普通日程
    // 条件2: repeatRule != null AND date <= targetDate → 查出所有"已开始"的重复日程
    //   例: 用户传入 targetDate=1738972800000L(2026-02-09), 会查出:
    //   - date=1738972800000L(2026-02-09) 的普通日程(如"小明生日")
    //   - date<=1738972800000L 且有repeatRule的重复日程(如"每周一去健身房", date=1738368000000L即2026-02-02)
    //   注意: 这里是粗筛, 精确过滤由 Repository 的 RepeatRule.matches() 完成
    // ORDER BY负责按照startTime排序,ASC是升序排列（从小到大）
    @Query(
        """
        SELECT * FROM schedules
        WHERE date = :targetDate
           OR (repeatRule IS NOT NULL AND date <= :targetDate)
        ORDER BY startTime ASC
        """
    )
    fun observeSchedulesByDate(targetDate: Long): Flow<List<ScheduleEntity>>

    // 查: 按ID查找具体的某一个
    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): ScheduleEntity?
}