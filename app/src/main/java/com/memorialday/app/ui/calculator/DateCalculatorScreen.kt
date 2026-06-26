// 纪念日 - 时间计算器视图
// 对应 iOS: DateCalculatorView.swift

package com.memorialday.app.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorialday.app.ui.components.DetailRow
import com.memorialday.app.ui.components.QuickConvertButton
import com.memorialday.app.ui.components.WorkdayStat
import com.memorialday.app.ui.theme.AppColors
import com.memorialday.app.viewmodels.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateCalculatorScreen(modifier: Modifier = Modifier) {
    val viewModel = remember { DateCalculatorViewModel() }
    val selectedMode by viewModel.selectedMode.collectAsState()

    Column(modifier = modifier.fillMaxSize().background(AppColors.backgroundLight)) {
        // 模式选择
        ModePicker(
            selectedMode = selectedMode,
            onModeSelected = { viewModel.setMode(it) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedMode) {
                CalculatorMode.INTERVAL -> IntervalView(viewModel)
                CalculatorMode.DATE_CALC -> DateCalcView(viewModel)
                CalculatorMode.CONVERT -> ConvertView(viewModel)
                CalculatorMode.WORKDAY -> WorkdayView(viewModel)
            }
        }
    }
}

@Composable
private fun ModePicker(selectedMode: CalculatorMode, onModeSelected: (CalculatorMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        CalculatorMode.entries.forEach { mode ->
            val isSelected = selectedMode == mode
            val icon = when (mode) {
                CalculatorMode.INTERVAL -> Icons.Filled.DateRange
                CalculatorMode.DATE_CALC -> Icons.Filled.EditCalendar
                CalculatorMode.CONVERT -> Icons.Filled.Timer
                CalculatorMode.WORKDAY -> Icons.Filled.Work
            }
            Surface(
                onClick = { onModeSelected(mode) },
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) AppColors.primary else AppColors.secondaryBgLight
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(icon, null, modifier = Modifier.size(18.dp), tint = if (isSelected) Color.White else AppColors.textSecondaryLight)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(mode.rawValue, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (isSelected) Color.White else AppColors.textSecondaryLight)
                }
            }
        }
    }
}

// MARK: - 卡片容器

@Composable
private fun CardContent(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color = AppColors.primary, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = AppColors.cardLight, shadowElevation = 2.dp) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, modifier = Modifier.size(14.dp), tint = color)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = color)
            }
            Column(modifier = Modifier.padding(14.dp)) { content() }
        }
    }
}

@Composable
private fun ResultCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(listOf(AppColors.accent.copy(alpha = 0.12f), AppColors.primaryLight.copy(alpha = 0.08f)))
                )
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun CalculateButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(AppColors.accent, AppColors.primaryLight)),
                    RoundedCornerShape(14.dp)
                )
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(12.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("计算", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

// MARK: - 日期行

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRow(label: String, date: Date, onDateChange: (Date) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val fmt = remember { java.text.SimpleDateFormat("yyyy年M月d日", Locale.CHINESE) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CalendarMonth, null, modifier = Modifier.size(16.dp), tint = AppColors.accent)
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 14.sp, color = AppColors.textPrimaryLight)
        Spacer(modifier = Modifier.width(12.dp))
        TextButton(onClick = { showDialog = true }) {
            Text(fmt.format(date), fontSize = 14.sp, color = AppColors.primary)
        }
    }

    if (showDialog) {
        val state = rememberDatePickerState(initialSelectedDateMillis = date.time)
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onDateChange(Date(it)) }
                    showDialog = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("取消") } }
        ) {
            DatePicker(state = state)
        }
    }
}

// MARK: - ① 日期间隔

@Composable
private fun IntervalView(viewModel: DateCalculatorViewModel) {
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val excludeWeekends by viewModel.excludeWeekends.collectAsState()
    val intervalResult by viewModel.intervalResult.collectAsState()

    CardContent("选择日期", Icons.Filled.DateRange, AppColors.primary) {
        DateRow("开始日期", startDate) { viewModel.setStartDate(it) }
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        DateRow("结束日期", endDate) { viewModel.setEndDate(it) }
    }

    // 排除周末
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.cardLight)
            .clickable { viewModel.setExcludeWeekends(!excludeWeekends) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (excludeWeekends) Icons.Filled.CheckCircle else Icons.Filled.Circle,
            null,
            modifier = Modifier.size(18.dp),
            tint = if (excludeWeekends) AppColors.success else AppColors.textTertiaryLight
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text("排除周末（仅计算工作日）", fontSize = 13.sp, color = AppColors.textSecondaryLight)
    }

    CalculateButton(onClick = { viewModel.calculateInterval() })

    intervalResult?.let { result ->
        ResultCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(16.dp), tint = AppColors.success)
                Spacer(modifier = Modifier.width(8.dp))
                Text("计算结果", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimaryLight)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${result.totalDays}", fontSize = 52.sp, fontWeight = FontWeight.Bold, color = AppColors.accent)
                Text("天", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondaryLight)
            }
            Text(result.directionText, fontSize = 14.sp, color = AppColors.textSecondaryLight)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            DetailRow("间隔", "${result.totalDays} 天")
            DetailRow("折合", "${result.years}年 ${result.months}月 ${result.days}天")
            DetailRow("折合周", "${result.weeks}周 ${result.weekRemainingDays}天")
            DetailRow("小时", "${result.totalHours} 小时")
            DetailRow("分钟", "${result.totalMinutes} 分钟")
            result.workdays?.let { DetailRow("工作日", "$it 天") }
        }
    }
}

// MARK: - ② 日期推算

@Composable
private fun DateCalcView(viewModel: DateCalculatorViewModel) {
    val baseDate by viewModel.baseDate.collectAsState()
    val calcValue by viewModel.calcValue.collectAsState()
    val calcUnit by viewModel.calcUnit.collectAsState()
    val calcDirection by viewModel.calcDirection.collectAsState()
    val calcResult by viewModel.calcResult.collectAsState()

    CardContent("基准日期", Icons.Filled.EditCalendar, AppColors.info) {
        DateRow("选择基准日期", baseDate) { viewModel.setBaseDate(it) }
    }

    CardContent("推算设置", Icons.Filled.SwapHoriz, AppColors.info) {
        // 方向
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("方向", fontSize = 13.sp, color = AppColors.textSecondaryLight)
            Spacer(modifier = Modifier.width(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                com.memorialday.app.ui.components.FilterChip("未来", null, calcDirection) { viewModel.setCalcDirection(true) }
                com.memorialday.app.ui.components.FilterChip("过去", null, !calcDirection) { viewModel.setCalcDirection(false) }
            }
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("数量", fontSize = 13.sp, color = AppColors.textSecondaryLight)
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedTextField(
                value = calcValue.toString(),
                onValueChange = { it.toIntOrNull()?.let { v -> viewModel.setCalcValue(v) } },
                modifier = Modifier.width(70.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            // 单位选择
            var expanded by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { expanded = true }) {
                    val names = mapOf(
                        java.util.Calendar.DAY_OF_MONTH to "天",
                        java.util.Calendar.WEEK_OF_YEAR to "周",
                        java.util.Calendar.MONTH to "月",
                        java.util.Calendar.YEAR to "年"
                    )
                    Text(names[calcUnit] ?: "天")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    viewModel.calendarComponents.forEach { (name, unit) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = { viewModel.setCalcUnit(unit); expanded = false }
                        )
                    }
                }
            }
        }
    }

    CalculateButton(onClick = { viewModel.calculateDateOffset() })

    calcResult?.let { result ->
        ResultCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(16.dp), tint = AppColors.success)
                Text("推算结果", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimaryLight)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(viewModel.formatDate(result), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppColors.info)
            val direction = if (calcDirection) "后" else "前"
            val names = mapOf(
                java.util.Calendar.DAY_OF_MONTH to "天",
                java.util.Calendar.WEEK_OF_YEAR to "周",
                java.util.Calendar.MONTH to "月",
                java.util.Calendar.YEAR to "年"
            )
            Text("基准日期${direction} ${calcValue}${names[calcUnit] ?: ""}", fontSize = 13.sp, color = AppColors.textSecondaryLight)
        }
    }
}

// MARK: - ③ 时间换算

@Composable
private fun ConvertView(viewModel: DateCalculatorViewModel) {
    val convertType by viewModel.convertType.collectAsState()
    val convertInput by viewModel.convertInput.collectAsState()
    val convertResult by viewModel.convertResult.collectAsState()

    CardContent("换算类型", Icons.Filled.Timer, AppColors.warning) {
        var expanded by remember { mutableStateOf(false) }
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(convertType.rawValue, color = AppColors.accent)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                TimeConversionType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.rawValue) },
                        onClick = { viewModel.setConvertType(type); expanded = false }
                    )
                }
            }
        }
    }

    CardContent("输入数值", Icons.Filled.Keyboard, AppColors.warning) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = convertInput,
                onValueChange = { viewModel.setConvertInput(it) },
                placeholder = { Text("请输入数字") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { viewModel.calculateConversion() },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent)
            ) { Text("换算", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
        }
    }

    if (convertResult.isNotEmpty()) {
        ResultCard {
            Text("换算结果", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondaryLight)
            Spacer(modifier = Modifier.height(8.dp))
            Text(convertResult, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = AppColors.info)
        }
    }

    // 常用换算
    Column(modifier = Modifier.padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Bolt, null, modifier = Modifier.size(12.dp), tint = AppColors.warning)
            Spacer(modifier = Modifier.width(8.dp))
            Text("常用换算", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondaryLight)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickConvertButton("1小时 = 60分钟", modifier = Modifier.weight(1f)) {
                viewModel.setConvertInput("1")
                viewModel.setConvertType(TimeConversionType.HOURS_TO_MINUTES)
                viewModel.calculateConversion()
            }
            QuickConvertButton("1天 = 24小时", modifier = Modifier.weight(1f)) {
                viewModel.setConvertInput("24")
                viewModel.setConvertType(TimeConversionType.HOURS_TO_MINUTES)
                viewModel.calculateConversion()
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickConvertButton("1小时 = 3600秒", modifier = Modifier.weight(1f)) {
                viewModel.setConvertInput("1")
                viewModel.setConvertType(TimeConversionType.HOURS_TO_SECONDS)
                viewModel.calculateConversion()
            }
            QuickConvertButton("30分钟 = 0.5小时", modifier = Modifier.weight(1f)) {
                viewModel.setConvertInput("30")
                viewModel.setConvertType(TimeConversionType.MINUTES_TO_HOURS)
                viewModel.calculateConversion()
            }
        }
    }
}

// MARK: - ④ 工作日计算

@Composable
private fun WorkdayView(viewModel: DateCalculatorViewModel) {
    val workStartDate by viewModel.workStartDate.collectAsState()
    val workEndDate by viewModel.workEndDate.collectAsState()
    val workdayResult by viewModel.workdayResult.collectAsState()

    CardContent("选择日期范围", Icons.Filled.Work, AppColors.success) {
        DateRow("开始日期", workStartDate) { viewModel.setWorkStartDate(it) }
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        DateRow("结束日期", workEndDate) { viewModel.setWorkEndDate(it) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.info.copy(alpha = 0.08f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Info, null, modifier = Modifier.size(14.dp), tint = AppColors.info)
        Spacer(modifier = Modifier.width(8.dp))
        Text("自动排除周六、周日，仅计算工作日", fontSize = 13.sp, color = AppColors.textSecondaryLight)
    }

    CalculateButton(onClick = { viewModel.calculateWorkdays() })

    workdayResult?.let { result ->
        ResultCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(16.dp), tint = AppColors.success)
                Text("计算结果", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimaryLight)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WorkdayStat("${result.totalDays}", "总天数", AppColors.primary, Modifier.weight(1f))
                WorkdayStat("${result.workdays}", "工作日", AppColors.success, Modifier.weight(1f))
                WorkdayStat("${result.weekends}", "周末", AppColors.warning, Modifier.weight(1f))
            }
            DetailRow("日期范围", "${viewModel.formatDate(workStartDate)} → ${viewModel.formatDate(workEndDate)}")
        }
    }
}
