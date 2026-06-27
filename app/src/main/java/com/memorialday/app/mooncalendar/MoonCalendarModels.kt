// 绵绵月历 - 数据模型
package com.memorialday.app.mooncalendar

import java.util.*

/** 每日记录 */
data class DailyRecord(
    val date: Date,               // 日期
    val isPeriod: Boolean = false, // 是否经期
    val flow: FlowLevel = FlowLevel.NONE,  // 流量
    val painLevel: PainLevel = PainLevel.NONE,  // 痛经
    val symptoms: List<String> = emptyList(), // 症状
    val mood: Mood = Mood.PEACEFUL,  // 心情
    val sleepHours: Double = 7.5,  // 睡眠
    val exercised: Boolean = false, // 运动
    val waterCups: Int = 8,        // 水杯数
    val notes: String = ""          // 备注
)

enum class FlowLevel { NONE, LIGHT, MEDIUM, HEAVY }
enum class PainLevel { NONE, MILD, MODERATE, SEVERE }
enum class Mood(val emoji: String, val label: String) {
    HAPPY("😊", "开心"),
    PEACEFUL("😌", "平静"),
    SAD("😔", "低落"),
    IRRITATED("😤", "烦躁"),
    TIRED("😴", "疲惫")
}

/** 周期设置 */
data class PeriodSettings(
    var cycleDays: Int = 28,
    var periodDays: Int = 5,
    var lastPeriodStart: Date = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_MONTH, -14) // 默认14天前
    }.time,
    var periodReminder: Boolean = true,
    var periodReminderDays: Int = 2,
    var dailyReminder: Boolean = false,
    var dailyReminderHour: Int = 21,
    var dailyReminderMinute: Int = 0,
    var pregnancyMode: Boolean = false,
    var privacyLock: Boolean = false
)

/** 周期阶段 */
enum class PeriodPhase {
    PERIOD,       // 经期
    SAFE,         // 安全期
    OVULATORY,    // 排卵期
    FERTILE       // 易孕期
}

/** 周期计算 */
class PeriodCalculator(private val settings: PeriodSettings) {

    /** 获取指定日期的周期阶段 */
    fun getPhase(date: Date): PeriodPhase {
        val cal = Calendar.getInstance()
        cal.time = settings.lastPeriodStart
        val lastStart = cal.time

        // 计算从上次经期开始到目标日期的天数差
        val targetCal = Calendar.getInstance().apply { time = date }
        val diffDays = ((targetCal.timeInMillis - cal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        // 相对周期位置（所在周期中的第几天）
        val posInCycle = ((diffDays % settings.cycleDays) + settings.cycleDays) % settings.cycleDays

        // 经期: 前 periodDays 天
        if (posInCycle < settings.periodDays) return PeriodPhase.PERIOD

        // 排卵日: 下次经期前14天
        val ovulationDay = settings.cycleDays - 14
        // 易孕期: 排卵日前5天到后1天
        val fertileStart = ovulationDay - 5
        val fertileEnd = ovulationDay + 1

        return when {
            posInCycle in fertileStart..fertileEnd -> PeriodPhase.FERTILE
            posInCycle == ovulationDay -> PeriodPhase.OVULATORY
            posInCycle in settings.periodDays until fertileStart -> PeriodPhase.SAFE
            posInCycle in (fertileEnd + 1) until settings.cycleDays -> PeriodPhase.SAFE
            else -> PeriodPhase.SAFE
        }
    }

    /** 计算下次经期天数 */
    fun daysUntilNextPeriod(from: Date = Date()): Int {
        val cal = Calendar.getInstance().apply { time = settings.lastPeriodStart }
        val fromCal = Calendar.getInstance().apply { time = from }
        val daysSince = ((fromCal.timeInMillis - cal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        val posInCycle = ((daysSince % settings.cycleDays) + settings.cycleDays) % settings.cycleDays
        val daysUntilNextPeriod = if (posInCycle == 0) settings.cycleDays else settings.cycleDays - posInCycle
        return daysUntilNextPeriod
    }

    /** 计算周期中的天数 */
    fun dayInCycle(from: Date = Date()): Int {
        val cal = Calendar.getInstance().apply { time = settings.lastPeriodStart }
        val fromCal = Calendar.getInstance().apply { time = from }
        val daysSince = ((fromCal.timeInMillis - cal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        return ((daysSince % settings.cycleDays) + settings.cycleDays) % settings.cycleDays
    }
}
