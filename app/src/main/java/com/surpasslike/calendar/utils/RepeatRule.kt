package com.surpasslike.calendar.utils

import com.blankj.utilcode.util.TimeUtils

/**
 * 重复规则 (enum, 无参数, 始终以日程的起始日期 date 为基准)
 *
 * 规则: 日程创建在哪天, 就按那天的星期/日期/月日来重复
 *
 * 示例:
 * - DAILY   → 每天重复, 如: 每天吃药
 * - WEEKLY  → 每周重复(与 date 同一星期几), 如: date=周一 → 每周一开会
 * - MONTHLY → 每月重复(与 date 同一天), 如: date=15号 → 每月15号发工资
 * - YEARLY  → 每年重复(与 date 同月同日), 如: date=2月14日 → 每年情人节
 *
 * 创建日程示例:
 * ```
 * // 每周一去健身房 (date=2026-02-02 是周一, 之后每个周一都会命中)
 * ScheduleEntity(
 *     title      = "去健身房",
 *     date       = TimeUtils.string2Millis("2026-02-02", "yyyy-MM-dd"), // string2Millis代表把字符串2026-02-02转换成时间戳
 *     startTime  = TimeUtils.string2Millis("2026-02-02 18:00", "yyyy-MM-dd HH:mm"),
 *     repeatRule = RepeatRule.WEEKLY
 * )
 * ```
 */
enum class RepeatRule {
    DAILY,   // 每天
    WEEKLY,  // 每周(与起始日期同一星期几)
    MONTHLY, // 每月(与起始日期同一天)
    YEARLY;  // 每年(与起始日期同月同日)

    /**
     * 判断该重复规则是否命中目标日期
     *
     * @param originDate 日程的起始日期, Long毫秒时间戳(当天零点), 如 1738368000000L 代表 2026-02-02 00:00:00
     * @param targetDate 要判断的目标日期, Long毫秒时间戳(当天零点), 如 1738972800000L 代表 2026-02-09 00:00:00
     *
     * 示例: 日程 "每周一去健身房", originDate=1738368000000L(也就是2026-02-02 周一), rule=WEEKLY
     * - targetDate=1738972800000L(2026-02-09 周一) → "1"=="1" → true  命中
     * - targetDate=1739059200000L(2026-02-10 周二) → "2"!="1" → false 过滤掉
     * - targetDate=1739577600000L(2026-02-16 周一) → "1"=="1" → true  命中
     */
    fun matches(originDate: Long, targetDate: Long): Boolean = when (this) {
        // 每天都命中, SQL 已保证 originDate <= targetDate
        // 例: 每天吃药, 起始日期之后的任意一天都返回 true
        DAILY -> true

        // 周几: "u" 格式: 1=周一, 2=周二, ..., 7=周日
        WEEKLY -> TimeUtils.millis2String(originDate, "u") ==
            TimeUtils.millis2String(targetDate, "u")

        // 几号: "dd" 格式: "01"~"31"
        MONTHLY -> TimeUtils.millis2String(originDate, "dd") ==
            TimeUtils.millis2String(targetDate, "dd")

        // 几月几号: "MM-dd" 格式: "02-14"
        YEARLY -> TimeUtils.millis2String(originDate, "MM-dd") ==
            TimeUtils.millis2String(targetDate, "MM-dd")
    }
}

// millis2String(long millis, @NonNull final String pattern)
// 把时间戳(第一个参数)转换成字符串,字符串样式取决于第二个参数.
// "大数字 → 按需提取 → 小字符串",举例如下
/*
* // 1. 提取"星期几"
    TimeUtils.millis2String(timestamp, "u")
    // 过程: 1769961600000L → "2026-02-02 周一" → 提取"周几" → "1"
    // 结果: "1"

    // 2. 提取"几号"
    TimeUtils.millis2String(timestamp, "dd")
    // 过程: 1769961600000L → "2026-02-02" → 提取"日" → "02"
    // 结果: "02"

    // 3. 提取"月份"
    TimeUtils.millis2String(timestamp, "MM")
    // 过程: 1769961600000L → "2026-02-02" → 提取"月" → "02"
    // 结果: "02"

    // 4. 提取"月-日"组合
    TimeUtils.millis2String(timestamp, "MM-dd")
    // 过程: 1769961600000L → "2026-02-02" → 提取"月-日" → "02-02"
    // 结果: "02-02"

    // 5. 提取"完整日期"
    TimeUtils.millis2String(timestamp, "yyyy-MM-dd")
    // 过程: 1769961600000L → 提取"年-月-日" → "2026-02-02"
    // 结果: "2026-02-02"

    // 6. 提取"时间"
    TimeUtils.millis2String(timestamp, "HH:mm:ss")
    // 过程: 1769961600000L → 提取"时:分:秒" → "00:00:00"
    // 结果: "00:00:00"
* */