// 绵绵月历 - 主页面
package com.memorialday.app.mooncalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

// 粉色主题色
private val Pink = Color(0xFFFF6B9D)
private val PinkLight = Color(0xFFFFB6C1)
private val PinkBg = Color(0xFFFFF0F5)
private val Orange = Color(0xFFFFA500)
private val OrangeBg = Color(0xFFFFD700)
private val Blue = Color(0xFF4A90D9)

@Composable
fun MoonCalendarScreen(modifier: Modifier = Modifier) {
    val settings = remember { MoonCalendarStorage.getSettings() }
    val calculator = remember { PeriodCalculator(settings) }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var showRecordSheet by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val today = remember { Calendar.getInstance() }
    val phase = remember { calculator.getPhase(Date()) }
    val daysUntilNext = remember { calculator.daysUntilNextPeriod() }
    val dayInCycle = remember { calculator.dayInCycle() }

    if (showSettings) {
        MoonCalendarSettingsScreen(onBack = { showSettings = false })
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F5))
    ) {
        // 顶部标题栏
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("绵绵月历", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C2C2C))
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { showSettings = true }) {
                Icon(Icons.Filled.Settings, null, tint = Color(0xFF8E8E93))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 顶部状态卡片
            item {
                StatusCard(phase, daysUntilNext, dayInCycle, settings)
            }

            // 月历
            item {
                CalendarGrid(
                    currentMonth = currentMonth,
                    records = MoonCalendarStorage.getAllRecords(),
                    settings = settings,
                    calculator = calculator,
                    onPrevMonth = {
                        currentMonth = Calendar.getInstance().apply {
                            time = currentMonth.time
                            add(Calendar.MONTH, -1)
                        }
                    },
                    onNextMonth = {
                        currentMonth = Calendar.getInstance().apply {
                            time = currentMonth.time
                            add(Calendar.MONTH, 1)
                        }
                    }
                )
            }

            // 图例
            item {
                Legend()
            }

            // 今日记录卡片
            item {
                TodayRecordCard(
                    date = Date(),
                    record = MoonCalendarStorage.getRecord(Date()),
                    onClick = { showRecordSheet = true }
                )
            }

            // 操作按钮
            item {
                ActionButtons(
                    onRecord = { showRecordSheet = true },
                    onReport = { /* TODO */ }
                )
            }
        }
    }

    if (showRecordSheet) {
        RecordBottomSheet(
            date = Date(),
            existingRecord = MoonCalendarStorage.getRecord(Date()),
            onDismiss = { showRecordSheet = false },
            onSaved = { showRecordSheet = false }
        )
    }
}

@Composable
private fun StatusCard(phase: PeriodPhase, daysUntilNext: Int, dayInCycle: Int, settings: PeriodSettings) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = PinkBg
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when (phase) {
                PeriodPhase.PERIOD -> {
                    Text("🩸 姨妈来啦第 ${dayInCycle + 1} 天，加油～", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${daysUntilNext}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Pink)
                    RichText("天")
                }
                PeriodPhase.FERTILE -> {
                    Text("🌱 易孕期，注意哦", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("距离下次姨妈还有", fontSize = 14.sp, color = Color.Gray)
                    Text("${daysUntilNext}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Pink)
                    Text("天", fontSize = 18.sp, color = Color.Gray)
                }
                PeriodPhase.OVULATORY -> {
                    Text("🌟 今天是排卵日", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("距离下次姨妈还有", fontSize = 14.sp, color = Color.Gray)
                    Text("${daysUntilNext}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Pink)
                    Text("天", fontSize = 18.sp, color = Color.Gray)
                }
                PeriodPhase.SAFE -> {
                    Text("🟢 当前处于：安全期", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("距离下次姨妈还有", fontSize = 14.sp, color = Color.Gray)
                    Text("${daysUntilNext}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Pink)
                    Text("天", fontSize = 18.sp, color = Color.Gray)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarGrid(
    currentMonth: Calendar,
    records: List<DailyRecord>,
    settings: PeriodSettings,
    calculator: PeriodCalculator,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val cal = Calendar.getInstance().apply { time = currentMonth.time }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    val firstDayOfMonth = Calendar.getInstance().apply { set(year, month, 1) }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startDayOfWeek = (firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 月份切换
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevMonth) { Icon(Icons.Filled.ChevronLeft, null) }
                Text("${year}年${month + 1}月", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onNextMonth) { Icon(Icons.Filled.ChevronRight, null) }
            }

            // 星期标题
            Row(Modifier.fillMaxWidth()) {
                listOf("一","二","三","四","五","六","日").forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                        fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(4.dp))

            // 日期网格
            val todayCal = Calendar.getInstance()
            val totalCells = startDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - startDayOfWeek + 1
                        val isInMonth = day in 1..daysInMonth

                        Box(
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isInMonth) {
                                val dateCal = Calendar.getInstance().apply { set(year, month, day, 0, 0, 0) }
                                val date = dateCal.time
                                val record = records.find { r ->
                                    val rc = Calendar.getInstance().apply { time = r.date }
                                    rc.get(Calendar.YEAR) == year && rc.get(Calendar.MONTH) == month && rc.get(Calendar.DAY_OF_MONTH) == day
                                }
                                val isToday = todayCal.get(Calendar.YEAR) == year && todayCal.get(Calendar.MONTH) == month && todayCal.get(Calendar.DAY_OF_MONTH) == day
                                val phase = calculator.getPhase(date)

                                val bgColor = when {
                                    record?.isPeriod == true || phase == PeriodPhase.PERIOD -> PinkLight
                                    phase == PeriodPhase.OVULATORY -> OrangeBg
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier.size(32.dp)
                                        .then(if (bgColor != Color.Transparent) Modifier.background(bgColor, CircleShape) else Modifier)
                                        .then(if (isToday) Modifier.border(2.dp, Blue, CircleShape) else Modifier),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$day", fontSize = 14.sp,
                                        color = when {
                                            isToday -> Blue
                                            phase == PeriodPhase.PERIOD -> Color.White
                                            phase == PeriodPhase.OVULATORY -> Color.White
                                            phase == PeriodPhase.FERTILE -> Orange
                                            else -> Color(0xFF2C2C2C)
                                        },
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Legend() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text("🩸经期　　🟡易孕期　　🟢安全期", fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun TodayRecordCard(date: Date, record: DailyRecord?, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF2EFEC)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Edit, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Spacer(Modifier.width(10.dp))
            if (record == null) {
                Text("今天还没记录哦，点击记录一下吧～", fontSize = 13.sp, color = Color.Gray)
            } else {
                val flowText = when (record.flow) { FlowLevel.LIGHT -> "少"; FlowLevel.MEDIUM -> "中"; FlowLevel.HEAVY -> "多"; else -> "-" }
                val painText = when (record.painLevel) { PainLevel.MILD -> "轻"; PainLevel.MODERATE -> "中"; PainLevel.SEVERE -> "重"; else -> "无" }
                Text("流量：$flowText ｜ 痛经：$painText ｜ 心情：${record.mood.emoji}", fontSize = 13.sp, color = Color(0xFF2C2C2C))
            }
        }
    }
}

@Composable
private fun ActionButtons(onRecord: () -> Unit, onReport: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onRecord,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Pink)
        ) {
            Text("记录今日", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        OutlinedButton(
            onClick = onReport,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Pink)
        ) {
            Text("查看报告", color = Pink, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}
