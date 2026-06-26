// 纪念日 - 提醒设置模型
// 对应 iOS: ReminderSettings.swift

package com.memorialday.app.models

import java.util.*

data class ReminderSettings(
    var id: UUID = UUID.randomUUID(),
    var isEnabled: Boolean = false,
    var advanceDays: Int = 1,
    var isYearlyRepeat: Boolean = false,
    var reminderHour: Int = 9,
    var reminderMinute: Int = 0,
    var customNote: String = ""
) {
    val displayDescription: String
        get() {
            if (!isEnabled) return "提醒已关闭"
            val dayStr = if (advanceDays == 0) "当天" else "提前${advanceDays}天"
            val cycleStr = if (isYearlyRepeat) "（每年循环）" else "（单次）"
            val timeStr = String.format(" %02d:%02d", reminderHour, reminderMinute)
            return dayStr + cycleStr + timeStr
        }

    /** 计算推送日期 */
    fun fireDate(targetDate: Date, isYearlyEvent: Boolean): Date? {
        val calendar = Calendar.getInstance()
        calendar.time = targetDate
        calendar.add(Calendar.DAY_OF_MONTH, -advanceDays)
        calendar.set(Calendar.HOUR_OF_DAY, reminderHour)
        calendar.set(Calendar.MINUTE, reminderMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}
