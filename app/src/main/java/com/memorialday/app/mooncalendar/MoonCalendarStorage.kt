// 绵绵月历 - 本地存储服务
package com.memorialday.app.mooncalendar

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

object MoonCalendarStorage {
    private const val PREFS_NAME = "moon_calendar"
    private const val KEY_RECORDS = "records"
    private const val KEY_SETTINGS = "settings"

    private lateinit var prefs: android.content.SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // 记录存储
    fun saveRecord(record: DailyRecord) {
        val records = getAllRecords().toMutableList()
        records.removeAll { isSameDay(it.date, record.date) }
        records.add(record)
        prefs.edit().putString(KEY_RECORDS, gson.toJson(records)).apply()
    }

    fun getRecord(date: Date): DailyRecord? {
        return getAllRecords().find { isSameDay(it.date, date) }
    }

    fun getAllRecords(): List<DailyRecord> {
        val json = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<DailyRecord>>() {}.type
            gson.fromJson(json, type) as? List<DailyRecord> ?: emptyList()
        } catch (_: Exception) { emptyList() }
    }

    // 设置存储
    fun saveSettings(settings: PeriodSettings) {
        prefs.edit().putString(KEY_SETTINGS, gson.toJson(settings)).apply()
    }

    fun getSettings(): PeriodSettings {
        val json = prefs.getString(KEY_SETTINGS, null) ?: return PeriodSettings()
        return try {
            gson.fromJson(json, PeriodSettings::class.java) ?: PeriodSettings()
        } catch (_: Exception) { PeriodSettings() }
    }

    private fun isSameDay(a: Date, b: Date): Boolean {
        val calA = Calendar.getInstance().apply { time = a }
        val calB = Calendar.getInstance().apply { time = b }
        return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
               calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
    }
}
