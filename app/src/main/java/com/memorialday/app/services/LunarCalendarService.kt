// 纪念日 - 农历/公历转换服务
// 对应 iOS: LunarCalendarService.swift
// 使用 Android 内置 android.icu.util.ChineseCalendar 进行公历/农历转换
// 对应 iOS Calendar(identifier: .chinese)

package com.memorialday.app.services

import android.icu.util.ChineseCalendar
import android.icu.util.TimeZone
import com.memorialday.app.models.LunarDate
import java.util.*

object LunarCalendarService {

    private const val BASE_YEAR = 1901  // ChineseCalendar 支持范围

    /** 公历 → 农历 */
    fun solarToLunar(date: Date): LunarDate {
        val cc = ChineseCalendar()
        cc.time = date
        val year = cc[ChineseCalendar.EXTENDED_YEAR] - 2637  // ChineseCalendar 元年偏移
        val month = cc[ChineseCalendar.MONTH] + 1  // 1=正月
        val day = cc[ChineseCalendar.DAY_OF_MONTH]
        val isLeap = cc[ChineseCalendar.IS_LEAP_MONTH] == 1
        return LunarDate(year = year, month = month, day = day, isLeapMonth = isLeap)
    }

    /** 农历 → 公历 */
    fun lunarToSolar(lunar: LunarDate): Date? {
        val cc = ChineseCalendar()
        cc.clear()
        cc[ChineseCalendar.EXTENDED_YEAR] = lunar.year + 2637
        cc[ChineseCalendar.MONTH] = lunar.month - 1
        cc[ChineseCalendar.DAY_OF_MONTH] = lunar.day
        cc[ChineseCalendar.IS_LEAP_MONTH] = if (lunar.isLeapMonth) 1 else 0
        return cc.time
    }

    /** 公历 → 农历字符串 */
    fun solarToLunarString(date: Date): String {
        val lunar = solarToLunar(date)
        return "${lunarYearName(lunar.year)}年${lunarMonthName(lunar.month)}${lunarDayName(lunar.day)}"
    }

    private val tianGan = arrayOf("甲","乙","丙","丁","戊","己","庚","辛","壬","癸")
    private val diZhi = arrayOf("子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥")
    private val months = arrayOf("正月","二月","三月","四月","五月","六月","七月","八月","九月","十月","冬月","腊月")
    private val days = arrayOf(
        "初一","初二","初三","初四","初五","初六","初七","初八","初九","初十",
        "十一","十二","十三","十四","十五","十六","十七","十八","十九","二十",
        "廿一","廿二","廿三","廿四","廿五","廿六","廿七","廿八","廿九","三十"
    )

    fun lunarYearName(year: Int): String {
        val g = (year - 4) % 10; if (g < 0) return "${year}年"
        val z = (year - 4) % 12; if (z < 0) return "${year}年"
        return "${tianGan[g]}${diZhi[z]}"
    }

    fun lunarMonthName(month: Int): String =
        if (month in 1..12) months[month - 1] else "${month}月"

    fun lunarDayName(day: Int): String =
        if (day in 1..30) days[day - 1] else "${day}日"
}
