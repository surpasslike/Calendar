package com.surpasslike.calendar.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "schedules", indices = [Index(value = ["date"])])
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // 主键,由数据库自动生成和递增

    val title: String, // 标题
    val description: String? = null, // 描述
    val date: Long, // 日程所在的日期
    val createdAtTime: Long = System.currentTimeMillis(), // 创建时间
    val updatedAtTime: Long = System.currentTimeMillis(), // 更新时间
    val startTime: Long? = null, // 日程开始时间
    val endTime: Long? = null, // 日程结束时间
    val isAllDay: Boolean = false, // 是否全天
    val repeatRule: String? = null, // 重复规则
    val reminderMinutes: Int? = null, // 提醒的提前分钟数,null表示不提醒
    val priority: Int = 0, // 优先级: 2=重要, 1=次重要, 0=日常
)