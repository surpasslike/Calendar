package com.surpasslike.calendar.repository

import com.surpasslike.calendar.data.dao.ScheduleDao
import com.surpasslike.calendar.data.entity.ScheduleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/*
* 仓库层: 管理日程数据的增删改查
* */
class ScheduleRepository(private val scheduleDao: ScheduleDao) {

    // 增: 新增日程到仓库中
    suspend fun insertSchedule(scheduleEntity: ScheduleEntity): Long {
        return scheduleDao.insertSchedule(scheduleEntity)
    }

    // 删: 删除仓库中的某个日程
    suspend fun deleteSchedule(schedule: ScheduleEntity) {
        scheduleDao.deleteSchedule(schedule)
    }

    // 按 id 删除
    suspend fun deleteScheduleById(id: Long) {
        scheduleDao.deleteScheduleById(id)
    }

    // 改: 修改已有日程信息
    suspend fun updateSchedule(schedule: ScheduleEntity) {
        scheduleDao.updateSchedule(schedule)
    }

    // 获取指定日期的日程列表(含重复日程的精确过滤)
    // @param date: Long毫秒时间戳(当天零点), 如 1770566400000L 代表 2026-02-09 00:00:00
    //
    // DAO 粗筛: 查出当天普通日程 + 所有已开始的重复日程
    // Repository 精筛: 用 repeatRule.matches(originDate, targetDate) 判断重复日程是否真的命中当天
    //   (originDate 和 targetDate 都是 Long 毫秒时间戳)
    //
    // 例: 数据库中有3条记录(date 都是当天零点的毫秒时间戳):
    //   A: date=1770566400000L(2026-02-09 周一), repeatRule=null,    title="小明生日"  → 普通日程
    //   B: date=1769961600000L(2026-02-02 周一), repeatRule=WEEKLY,  title="健身房"    → 每周一
    //   C: date=1769875200000L(2026-02-01),      repeatRule=MONTHLY, title="发工资"    → 每月1号
    //
    // 用户点击 2026-02-09(周一), 传入 date=1770566400000L:
    //   DAO 粗筛 → A(date匹配), B(有repeatRule且date<=), C(有repeatRule且date<=) → 3条
    //   精筛:
    //     A: repeatRule=null → 保留
    //     B: WEEKLY.matches(1769961600000L, 1770566400000L) → 都是周一 "1"=="1" → 保留
    //     C: MONTHLY.matches(1769875200000L, 1770566400000L) → "01"!="09" → 过滤掉
    //   最终结果: A + B = 2条
    fun observeSchedulesByDate(targetDate: Long): Flow<List<ScheduleEntity>> {
        return scheduleDao.observeSchedulesByDate(targetDate).map { list ->
            list.filter { schedule ->
                if (schedule.repeatRule == null) true
                else schedule.repeatRule.matches(schedule.date, targetDate)
            }
        }.flowOn(Dispatchers.Default) // map里的过滤逻辑放到计算线程中,防止主线程卡顿
    }

    // 查: 按ID查找具体的某一个
    suspend fun getScheduleById(id: Long): ScheduleEntity? {
        return scheduleDao.getScheduleById(id)
    }
}