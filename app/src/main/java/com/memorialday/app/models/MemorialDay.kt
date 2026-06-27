// 纪念日 - 核心数据模型
// 对应 iOS: MemorialDay.swift

package com.memorialday.app.models

import com.google.gson.annotations.SerializedName
import java.util.*

/** 纪念日类型：倒数日 vs 正数日 */
enum class DayType(val rawValue: String) {
    @SerializedName("倒数日") COUNTDOWN("倒数日"),
    @SerializedName("正数日") COUNTUP("正数日");
}

/** 纪念日主模型 */
data class MemorialDay(
    var id: UUID = UUID.randomUUID(),
    var createdDate: Date = Date(),

    // 核心信息
    var title: String = "",
    var notes: String = "",
    var targetDate: Date = Date(),
    var dayType: DayType = DayType.COUNTDOWN,

    // 分组管理
    var categoryID: UUID? = null,
    var tags: MutableList<String> = mutableListOf(),
    var isPinned: Boolean = false,
    var sortOrder: Int = 0,

    // 农历支持
    var useLunarCalendar: Boolean = false,
    var lunarYear: Int? = null,
    var lunarMonth: Int? = null,
    var lunarDay: Int? = null,
    var isLeapMonth: Boolean = false,

    // 卡片样式
    var backgroundColorHex: String = "#F5F0EB",
    var backgroundEndColorHex: String? = null,
    var textColorHex: String = "#2C2C2C",
    var fontName: String? = null,
    var fontSize: Double = 28.0,
    var iconName: String = "favorite",  // Android drawable / Material icon
    var cornerRadius: Double = 16.0,
    var shadowRadius: Double = 8.0,
    var showGradient: Boolean = false,

    // 循环标记
    var isYearlyRepeat: Boolean = false,

    // 提醒
    var reminderSettings: ReminderSettings = ReminderSettings(),

    // 已读/状态
    var isArchived: Boolean = false
) {
    val calendar: Calendar get() = Calendar.getInstance()

    /** 当前距原始目标日期的天数 */
    val daysFromNow: Int
        get() {
            val today = calendar.apply { time = Date() }.let {
                it.set(Calendar.HOUR_OF_DAY, 0); it.set(Calendar.MINUTE, 0)
                it.set(Calendar.SECOND, 0); it.set(Calendar.MILLISECOND, 0)
                it
            }
            val target = calendar.apply { time = targetDate }.let {
                it.set(Calendar.HOUR_OF_DAY, 0); it.set(Calendar.MINUTE, 0)
                it.set(Calendar.SECOND, 0); it.set(Calendar.MILLISECOND, 0)
                it
            }
            val diffMillis = target.timeInMillis - today.timeInMillis
            return (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        }

    /** 显示用的天数字符串 */
    val displayDayCount: Int
        get(): Int {
            return when (dayType) {
                DayType.COUNTDOWN -> {
                    if (isYearlyRepeat || daysFromNow <= 0) return daysUntilNextOccurrence
                    kotlin.math.max(daysFromNow, 0)
                }
                DayType.COUNTUP -> kotlin.math.max(kotlin.math.abs(daysFromNow), 1)
            }
        }

    /** 计算下次发生距离今天的天数 */
    val daysUntilNextOccurrence: Int
        get() {
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val thisYear = today.get(Calendar.YEAR)

            if (useLunarCalendar && lunarMonth != null && lunarDay != null) {
                // 农历分支
                val lunarTarget = LunarDate(thisYear, lunarMonth!!, lunarDay!!, isLeapMonth)
                val solarDate = com.memorialday.app.services.LunarCalendarService.lunarToSolar(lunarTarget)
                if (solarDate != null) {
                    val nextDate = Calendar.getInstance().apply {
                        time = solarDate
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }
                    if (nextDate.before(today)) {
                        val nextLunar = LunarDate(thisYear + 1, lunarMonth!!, lunarDay!!, isLeapMonth)
                        val nextYearDate = com.memorialday.app.services.LunarCalendarService.lunarToSolar(nextLunar)
                        if (nextYearDate != null) {
                            nextDate.time = nextYearDate
                            nextDate.set(Calendar.HOUR_OF_DAY, 0); nextDate.set(Calendar.MINUTE, 0)
                            nextDate.set(Calendar.SECOND, 0); nextDate.set(Calendar.MILLISECOND, 0)
                        }
                    }
                    val diff = (nextDate.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)
                    return kotlin.math.max(diff.toInt(), 0)
                }
                return 0
            }

            // 公历分支
            val targetCal = Calendar.getInstance().apply { time = targetDate }
            val month = targetCal.get(Calendar.MONTH)
            val day = targetCal.get(Calendar.DAY_OF_MONTH)

            var nextDate = Calendar.getInstance().apply {
                set(thisYear, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (nextDate.before(today)) {
                nextDate.set(Calendar.YEAR, thisYear + 1)
            }
            val diff = (nextDate.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)
            return kotlin.math.max(diff.toInt(), 0)
        }

    /** 显示的副标题 */
    val subtitle: String
        get() {
            val formatter = java.text.SimpleDateFormat("yyyy年M月d日", java.util.Locale.CHINESE)
            val dateStr = formatter.format(targetDate)
            if (useLunarCalendar && lunarMonth != null && lunarDay != null && lunarMonth!! > 0 && lunarDay!! > 0) {
                return "农历 ${lunarMonth}月${lunarDay}日 · $dateStr"
            }
            return dateStr
        }

    /** 实际倒计时目标日期 */
    val effectiveTargetDate: Date
        get() {
            if (dayType != DayType.COUNTDOWN) return targetDate
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val thisYear = today.get(Calendar.YEAR)

            if (isYearlyRepeat || daysFromNow <= 0) {
                if (useLunarCalendar && lunarMonth != null && lunarDay != null) {
                    val lunarTarget = LunarDate(thisYear, lunarMonth!!, lunarDay!!, isLeapMonth)
                    val sd = com.memorialday.app.services.LunarCalendarService.lunarToSolar(lunarTarget)
                    if (sd != null) {
                        val nextDate = Calendar.getInstance().apply {
                            time = sd
                            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                        }
                        if (nextDate.before(today)) {
                            val nextLunar = LunarDate(thisYear + 1, lunarMonth!!, lunarDay!!, isLeapMonth)
                            val nyd = com.memorialday.app.services.LunarCalendarService.lunarToSolar(nextLunar)
                            if (nyd != null) return nyd
                        }
                        return nextDate.time
                    }
                    return targetDate
                }

                val targetCal = Calendar.getInstance().apply { time = targetDate }
                val month = targetCal.get(Calendar.MONTH)
                val day = targetCal.get(Calendar.DAY_OF_MONTH)
                var nextDate = Calendar.getInstance().apply {
                    set(thisYear, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (nextDate.before(today)) {
                    nextDate.set(Calendar.YEAR, thisYear + 1)
                }
                return nextDate.time
            }
            return targetDate
        }

    /** 倒计时说明文字 */
    val countdownDescription: String
        get() {
            if (dayType == DayType.COUNTDOWN && (isYearlyRepeat || daysFromNow <= 0)) {
                if (useLunarCalendar) return "距离今年农历${title}还有"
                return "距离今年${title}还有"
            }
            return if (dayType == DayType.COUNTDOWN) "距离${title}还有" else "已经"
        }

    /** 详细倒计时（天/时/分/秒） */
    val detailedTimeRemaining: TimeComponents
        get() {
            val now = Date()
            val target = effectiveTargetDate
            val diffMs = kotlin.math.abs(target.time - now.time)
            val totalSeconds = (diffMs / 1000).toInt()
            val d = totalSeconds / 86400
            val h = (totalSeconds % 86400) / 3600
            val m = (totalSeconds % 3600) / 60
            val s = totalSeconds % 60
            return TimeComponents(d, h, m, s)
        }
}

data class TimeComponents(val days: Int, val hours: Int, val minutes: Int, val seconds: Int)

/** 农历日期结构 */
data class LunarDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val isLeapMonth: Boolean = false
)
