// 纪念日 - 时间计算器视图模型
// 对应 iOS: DateCalculatorViewModel.swift

package com.memorialday.app.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

enum class CalculatorMode(val rawValue: String) {
    INTERVAL("日期间隔"),
    DATE_CALC("日期推算"),
    CONVERT("时间换算"),
    WORKDAY("工作日计算");
}

enum class TimeConversionType(val rawValue: String) {
    HOURS_TO_MINUTES("小时 → 分钟"),
    HOURS_TO_SECONDS("小时 → 秒"),
    MINUTES_TO_SECONDS("分钟 → 秒"),
    SECONDS_TO_MINUTES("秒 → 分钟"),
    MINUTES_TO_HOURS("分钟 → 小时"),
    SECONDS_TO_HOURS("秒 → 小时");
}

class DateCalculatorViewModel : ViewModel() {

    private val _selectedMode = MutableStateFlow(CalculatorMode.INTERVAL)
    val selectedMode: StateFlow<CalculatorMode> = _selectedMode.asStateFlow()

    // 日期间隔
    private val _startDate = MutableStateFlow(Date())
    val startDate: StateFlow<Date> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(Date(System.currentTimeMillis() + 86400000 * 7))
    val endDate: StateFlow<Date> = _endDate.asStateFlow()

    private val _excludeWeekends = MutableStateFlow(false)
    val excludeWeekends: StateFlow<Boolean> = _excludeWeekends.asStateFlow()

    private val _intervalResult = MutableStateFlow<DateIntervalResult?>(null)
    val intervalResult: StateFlow<DateIntervalResult?> = _intervalResult.asStateFlow()

    // 日期推算
    private val _baseDate = MutableStateFlow(Date())
    val baseDate: StateFlow<Date> = _baseDate.asStateFlow()

    private val _calcValue = MutableStateFlow(30)
    val calcValue: StateFlow<Int> = _calcValue.asStateFlow()

    private val _calcUnit = MutableStateFlow(Calendar.DAY_OF_MONTH)
    val calcUnit: StateFlow<Int> = _calcUnit.asStateFlow()

    private val _calcDirection = MutableStateFlow(true) // true=未来
    val calcDirection: StateFlow<Boolean> = _calcDirection.asStateFlow()

    private val _calcResult = MutableStateFlow<Date?>(null)
    val calcResult: StateFlow<Date?> = _calcResult.asStateFlow()

    // 时间换算
    private val _convertType = MutableStateFlow(TimeConversionType.HOURS_TO_MINUTES)
    val convertType: StateFlow<TimeConversionType> = _convertType.asStateFlow()

    private val _convertInput = MutableStateFlow("1")
    val convertInput: StateFlow<String> = _convertInput.asStateFlow()

    private val _convertResult = MutableStateFlow("")
    val convertResult: StateFlow<String> = _convertResult.asStateFlow()

    // 工作日
    private val _workStartDate = MutableStateFlow(Date())
    val workStartDate: StateFlow<Date> = _workStartDate.asStateFlow()

    private val _workEndDate = MutableStateFlow(Date(System.currentTimeMillis() + 86400000 * 14))
    val workEndDate: StateFlow<Date> = _workEndDate.asStateFlow()

    private val _workdayResult = MutableStateFlow<WorkdayResult?>(null)
    val workdayResult: StateFlow<WorkdayResult?> = _workdayResult.asStateFlow()

    val calendarComponents = listOf(
        "天" to Calendar.DAY_OF_MONTH,
        "周" to Calendar.WEEK_OF_YEAR,
        "月" to Calendar.MONTH,
        "年" to Calendar.YEAR
    )

    // Actions

    fun setMode(mode: CalculatorMode) { _selectedMode.value = mode }
    fun setStartDate(date: Date) { _startDate.value = date }
    fun setEndDate(date: Date) { _endDate.value = date }
    fun setExcludeWeekends(exclude: Boolean) { _excludeWeekends.value = exclude }
    fun setBaseDate(date: Date) { _baseDate.value = date }
    fun setCalcValue(value: Int) { _calcValue.value = value }
    fun setCalcUnit(unit: Int) { _calcUnit.value = unit }
    fun setCalcDirection(future: Boolean) { _calcDirection.value = future }
    fun setConvertType(type: TimeConversionType) { _convertType.value = type }
    fun setConvertInput(input: String) { _convertInput.value = input }
    fun setWorkStartDate(date: Date) { _workStartDate.value = date }
    fun setWorkEndDate(date: Date) { _workEndDate.value = date }

    /** 计算日期间隔 */
    fun calculateInterval() {
        val cal = Calendar.getInstance()
        cal.time = _startDate.value
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.time = _endDate.value
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val end = cal.timeInMillis

        val diffMs = kotlin.math.abs(end - start)
        val totalDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
        val totalHours = (diffMs / (1000 * 60 * 60)).toInt()
        val totalMinutes = (diffMs / (1000 * 60)).toInt()
        val years = totalDays / 365
        val months = (totalDays % 365) / 30
        val remainingDays = totalDays - years * 365 - months * 30
        val weeks = totalDays / 7
        val weekRemaining = totalDays % 7
        val isPast = end < start

        var workdays: Int? = null
        if (_excludeWeekends.value) {
            workdays = countWorkdays(
                Date(kotlin.math.min(start, end)),
                Date(kotlin.math.max(start, end))
            )
        }

        _intervalResult.value = DateIntervalResult(
            totalDays, totalHours, totalMinutes,
            years, months, remainingDays, weeks, weekRemaining,
            workdays, isPast
        )
    }

    /** 推算日期 */
    fun calculateDateOffset() {
        val cal = Calendar.getInstance()
        cal.time = _baseDate.value
        val value = if (_calcDirection.value) _calcValue.value else -_calcValue.value
        cal.add(_calcUnit.value, value)
        _calcResult.value = cal.time
    }

    /** 时间单位换算 */
    fun calculateConversion() {
        val input = _convertInput.value.toDoubleOrNull()
        if (input == null) {
            _convertResult.value = "请输入有效数字"
            return
        }
        _convertResult.value = when (_convertType.value) {
            TimeConversionType.HOURS_TO_MINUTES -> "$input 小时 = ${input * 60} 分钟"
            TimeConversionType.HOURS_TO_SECONDS -> "$input 小时 = ${input * 3600} 秒"
            TimeConversionType.MINUTES_TO_SECONDS -> "$input 分钟 = ${input * 60} 秒"
            TimeConversionType.SECONDS_TO_MINUTES -> "$input 秒 = ${input / 60} 分钟"
            TimeConversionType.MINUTES_TO_HOURS -> "$input 分钟 = ${input / 60} 小时"
            TimeConversionType.SECONDS_TO_HOURS -> "$input 秒 = ${input / 3600} 小时"
        }
    }

    /** 计算工作日 */
    fun calculateWorkdays() {
        val cal = Calendar.getInstance()
        cal.time = _workStartDate.value
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.time = _workEndDate.value
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val end = cal.timeInMillis

        val absDays = (kotlin.math.abs(end - start) / (1000 * 60 * 60 * 24)).toInt()
        val workdays = countWorkdays(
            Date(kotlin.math.min(start, end)),
            Date(kotlin.math.max(start, end))
        )
        val weekends = absDays - workdays

        _workdayResult.value = WorkdayResult(absDays, workdays, weekends, end < start)
    }

    private fun countWorkdays(from: Date, to: Date): Int {
        val cal = Calendar.getInstance()
        cal.time = from
        var count = 0
        while (cal.time.before(to)) {
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek != Calendar.SUNDAY && dayOfWeek != Calendar.SATURDAY) {
                count++
            }
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return count
    }

    fun formatDate(date: Date): String {
        val fmt = SimpleDateFormat("yyyy年M月d日", Locale.CHINESE)
        return fmt.format(date)
    }
}

// 结果模型

data class DateIntervalResult(
    val totalDays: Int, val totalHours: Int, val totalMinutes: Int,
    val years: Int, val months: Int, val days: Int,
    val weeks: Int, val weekRemainingDays: Int,
    val workdays: Int?, val isPast: Boolean
) {
    val directionText: String get() = if (isPast) "已过" else "相距"
}

data class WorkdayResult(
    val totalDays: Int, val workdays: Int, val weekends: Int, val isPast: Boolean
)
