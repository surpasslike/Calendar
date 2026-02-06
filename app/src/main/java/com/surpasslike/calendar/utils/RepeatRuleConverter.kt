package com.surpasslike.calendar.utils

import androidx.room.TypeConverter

/**
 * Room TypeConverter: RepeatRule(enum) ↔ String(数据库存储)
 *
 * 数据库里存的是枚举名称字符串, 代码里用的是枚举类型:
 * - 写入数据库时: RepeatRule.WEEKLY → "WEEKLY"
 * - 读取数据库时: "WEEKLY" → RepeatRule.WEEKLY
 *
 * 在 AppDatabase 上加 @TypeConverters(RepeatRuleConverter::class) 即可生效
 */
class RepeatRuleConverter {

    // RepeatRule → String, 存入数据库时调用
    // 例: RepeatRule.WEEKLY → "WEEKLY"
    @TypeConverter
    fun fromRepeatRule(rule: RepeatRule?): String? = rule?.name

    // String → RepeatRule, 从数据库读取时调用
    // 例: "WEEKLY" → RepeatRule.WEEKLY
    @TypeConverter
    fun toRepeatRule(str: String?): RepeatRule? = str?.let { RepeatRule.valueOf(it) }
}
