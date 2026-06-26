// 纪念日 - 纪念日编辑视图
// 对应 iOS: CardEditorView.swift

@file:OptIn(ExperimentalMaterial3Api::class)

package com.memorialday.app.ui.detail

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.memorialday.app.models.*
import com.memorialday.app.services.LunarCalendarService
import com.memorialday.app.ui.components.FilterChip
import com.memorialday.app.ui.theme.AppColors
import com.memorialday.app.ui.theme.AppRadius
import com.memorialday.app.utils.fromHex
import com.memorialday.app.utils.toHex
import com.memorialday.app.viewmodels.MemorialDayViewModel
import com.memorialday.app.viewmodels.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditorScreen(
    editDay: MemorialDay? = null,
    onDismiss: () -> Unit
) {
    val viewModel = remember { MemorialDayViewModel.getInstance() }
    val settingsVM = remember { SettingsViewModel.getInstance() }
    val categories by viewModel.categories.collectAsState()
    val isEditing = editDay != null

    // 字段状态
    var title by remember { mutableStateOf(editDay?.title ?: "") }
    var notes by remember { mutableStateOf(editDay?.notes ?: "") }
    var targetDate by remember { mutableStateOf(editDay?.targetDate ?: Date()) }
    var dayType by remember { mutableStateOf(editDay?.dayType ?: DayType.COUNTDOWN) }
    var selectedCategoryID by remember { mutableStateOf(editDay?.categoryID) }
    var tagText by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(editDay?.tags?.toMutableList() ?: mutableListOf()) }

    // 卡片样式
    var bgColorHex by remember { mutableStateOf(editDay?.backgroundColorHex ?: "#F5F0EB") }
    var bgEndColorHex by remember { mutableStateOf(editDay?.backgroundEndColorHex ?: "") }
    var textColorHex by remember { mutableStateOf(editDay?.textColorHex ?: "#2C2C2C") }
    var useGradient by remember { mutableStateOf(editDay?.showGradient ?: false) }
    var fontSize by remember { mutableStateOf(editDay?.fontSize ?: 28.0) }
    var cornerRadius by remember { mutableStateOf(editDay?.cornerRadius ?: 16.0) }
    var shadowRadius by remember { mutableStateOf(editDay?.shadowRadius ?: 8.0) }

    // 提醒设置
    var reminderEnabled by remember { mutableStateOf(editDay?.reminderSettings?.isEnabled ?: false) }
    var advanceDays by remember { mutableIntStateOf(editDay?.reminderSettings?.advanceDays ?: 1) }
    var reminderHour by remember { mutableIntStateOf(editDay?.reminderSettings?.reminderHour ?: 9) }
    var reminderMinute by remember { mutableIntStateOf(editDay?.reminderSettings?.reminderMinute ?: 0) }
    var reminderNote by remember { mutableStateOf(editDay?.reminderSettings?.customNote ?: "") }

    // 循环/农历
    var isYearlyRepeat by remember { mutableStateOf(editDay?.isYearlyRepeat ?: false) }
    var useLunarCalendar by remember { mutableStateOf(editDay?.useLunarCalendar ?: false) }
    var lunarMonth by remember { mutableIntStateOf(editDay?.lunarMonth ?: 0) }
    var lunarDay by remember { mutableIntStateOf(editDay?.lunarDay ?: 0) }
    var lunarYear by remember { mutableIntStateOf(editDay?.lunarYear ?: 2000) }
    var isLeapMonth by remember { mutableStateOf(editDay?.isLeapMonth ?: false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "编辑纪念日" else "新建纪念日") },
                navigationIcon = {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = AppColors.textSecondaryLight)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            saveDay(
                                isEditing, editDay, title, notes, targetDate, dayType,
                                selectedCategoryID, tags, bgColorHex, bgEndColorHex,
                                textColorHex, useGradient, fontSize, cornerRadius, shadowRadius,
                                reminderEnabled, advanceDays, reminderHour, reminderMinute,
                                reminderNote, isYearlyRepeat, useLunarCalendar,
                                lunarMonth, lunarDay, lunarYear, isLeapMonth,
                                viewModel, onDismiss
                            )
                        },
                        enabled = title.isNotEmpty()
                    ) {
                        Text(
                            "保存",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (title.isNotEmpty()) AppColors.accent else AppColors.textTertiaryLight
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppColors.backgroundLight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 预览卡片
            PreviewCard(
                title = title, dayType = dayType, bgColorHex = bgColorHex,
                bgEndColorHex = bgEndColorHex, useGradient = useGradient,
                textColorHex = textColorHex, fontSize = fontSize,
                cornerRadius = cornerRadius, shadowRadius = shadowRadius
            )

            // 基础信息
            BasicInfoSection(
                title = title, onTitleChange = { title = it },
                dayType = dayType, onDayTypeChange = { dayType = it },
                notes = notes, onNotesChange = { notes = it }
            )

            // 日期选择
            DateSection(
                targetDate = targetDate, onDateChange = { targetDate = it },
                useLunarCalendar = useLunarCalendar, onUseLunarChange = { useLunarCalendar = it },
                lunarYear = lunarYear, onLunarYearChange = { lunarYear = it },
                lunarMonth = lunarMonth, onLunarMonthChange = { lunarMonth = it },
                lunarDay = lunarDay, onLunarDayChange = { lunarDay = it },
                isLeapMonth = isLeapMonth, onLeapMonthChange = { isLeapMonth = it }
            )

            // 分类与标签
            CategorySection(
                categories = categories,
                selectedCategoryID = selectedCategoryID,
                onCategorySelected = { selectedCategoryID = it },
                tags = tags,
                tagText = tagText,
                onTagTextChange = { tagText = it },
                onAddTag = {
                    val tag = tagText.trim()
                    if (tag.isNotEmpty() && !tags.contains(tag)) {
                        tags.add(tag)
                        tagText = ""
                    }
                },
                onRemoveTag = { tags.remove(it) }
            )

            // 卡片样式
            CardStyleSection(
                bgColorHex = bgColorHex, onBgColorChange = { bgColorHex = it },
                bgEndColorHex = bgEndColorHex, onBgEndColorChange = { bgEndColorHex = it },
                textColorHex = textColorHex, onTextColorChange = { textColorHex = it },
                useGradient = useGradient, onUseGradientChange = { useGradient = it },
                fontSize = fontSize, onFontSizeChange = { fontSize = it },
                cornerRadius = cornerRadius, onCornerRadiusChange = { cornerRadius = it },
                shadowRadius = shadowRadius, onShadowRadiusChange = { shadowRadius = it }
            )

            // 提醒设置
            ReminderSettingsSection(
                reminderEnabled = reminderEnabled, onReminderEnabledChange = { reminderEnabled = it },
                advanceDays = advanceDays, onAdvanceDaysChange = { advanceDays = it },
                reminderHour = reminderHour, onReminderHourChange = { reminderHour = it },
                reminderMinute = reminderMinute, onReminderMinuteChange = { reminderMinute = it },
                reminderNote = reminderNote, onReminderNoteChange = { reminderNote = it },
                isYearlyRepeat = isYearlyRepeat, onYearlyRepeatChange = { isYearlyRepeat = it }
            )
        }
    }
}

// MARK: - 保存逻辑

private fun saveDay(
    isEditing: Boolean, editDay: MemorialDay?, title: String, notes: String,
    targetDate: Date, dayType: DayType, selectedCategoryID: UUID?,
    tags: List<String>, bgColorHex: String, bgEndColorHex: String,
    textColorHex: String, useGradient: Boolean, fontSize: Double,
    cornerRadius: Double, shadowRadius: Double,
    reminderEnabled: Boolean, advanceDays: Int, reminderHour: Int,
    reminderMinute: Int, reminderNote: String, isYearlyRepeat: Boolean,
    useLunarCalendar: Boolean, lunarMonth: Int, lunarDay: Int,
    lunarYear: Int, isLeapMonth: Boolean,
    viewModel: MemorialDayViewModel, onDismiss: () -> Unit
) {
    var day = editDay ?: MemorialDay()

    // 日期处理
    val finalDate = if (useLunarCalendar && lunarMonth >= 1 && lunarDay >= 1) {
        val lunarDate = LunarDate(lunarYear, lunarMonth, lunarDay, isLeapMonth)
        LunarCalendarService.lunarToSolar(lunarDate) ?: targetDate
    } else {
        targetDate
    }

    day = day.copy(
        title = title,
        notes = notes,
        targetDate = finalDate,
        dayType = dayType,
        categoryID = selectedCategoryID,
        tags = tags.toMutableList(),
        backgroundColorHex = bgColorHex,
        backgroundEndColorHex = if (useGradient && bgEndColorHex.isNotEmpty()) bgEndColorHex else null,
        textColorHex = textColorHex,
        fontSize = fontSize,
        cornerRadius = cornerRadius,
        shadowRadius = shadowRadius,
        showGradient = useGradient,
        isYearlyRepeat = isYearlyRepeat,
        useLunarCalendar = useLunarCalendar,
        lunarYear = if (useLunarCalendar) lunarYear else null,
        lunarMonth = if (useLunarCalendar) lunarMonth else null,
        lunarDay = if (useLunarCalendar) lunarDay else null,
        isLeapMonth = if (useLunarCalendar) isLeapMonth else false,
        reminderSettings = ReminderSettings(
            isEnabled = reminderEnabled,
            advanceDays = advanceDays,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute,
            customNote = reminderNote,
            isYearlyRepeat = isYearlyRepeat
        )
    )

    if (isEditing && viewModel.days.value.any { it.id == day.id }) {
        viewModel.updateDay(day)
    } else {
        viewModel.addDay(day)
    }
    onDismiss()
}

// MARK: - 预览卡片

@Composable
private fun PreviewCard(
    title: String, dayType: DayType, bgColorHex: String,
    bgEndColorHex: String, useGradient: Boolean,
    textColorHex: String, fontSize: Double,
    cornerRadius: Double, shadowRadius: Double
) {
    val bgColor = Color.fromHex(bgColorHex)
    val textColor = Color.fromHex(textColorHex)
    val bg: Brush = if (useGradient && bgEndColorHex.isNotEmpty()) {
        Brush.linearGradient(listOf(bgColor, Color.fromHex(bgEndColorHex)))
    } else {
        Brush.linearGradient(listOf(bgColor, bgColor))
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(cornerRadius.dp),
            shadowElevation = shadowRadius.dp.coerceAtMost(8.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .background(brush = bg, shape = RoundedCornerShape(cornerRadius.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Favorite, null, modifier = Modifier.size(24.dp), tint = textColor.copy(alpha = 0.7f))
                    Text(
                        title.ifEmpty { "预览" },
                        fontSize = fontSize.toInt().sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text("99", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text(
                        if (dayType == DayType.COUNTDOWN) "天后" else "天前",
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
        Text("实时预览", fontSize = 11.sp, color = AppColors.textTertiaryLight)
    }
}

// MARK: - 基础信息

@Composable
private fun BasicInfoSection(
    title: String, onTitleChange: (String) -> Unit,
    dayType: DayType, onDayTypeChange: (DayType) -> Unit,
    notes: String, onNotesChange: (String) -> Unit
) {
    SectionContainer("基本信息") {
        // 名称
        Text("名称 *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondaryLight)
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = { Text("输入纪念日名称") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 类型选择
        Text("类型", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondaryLight)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip("倒数日", null, dayType == DayType.COUNTDOWN) { onDayTypeChange(DayType.COUNTDOWN) }
            FilterChip("正数日", null, dayType == DayType.COUNTUP) { onDayTypeChange(DayType.COUNTUP) }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 备注
        Text("备注", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondaryLight)
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            placeholder = { Text("备注信息...") },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

// MARK: - 日期选择

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSection(
    targetDate: Date, onDateChange: (Date) -> Unit,
    useLunarCalendar: Boolean, onUseLunarChange: (Boolean) -> Unit,
    lunarYear: Int, onLunarYearChange: (Int) -> Unit,
    lunarMonth: Int, onLunarMonthChange: (Int) -> Unit,
    lunarDay: Int, onLunarDayChange: (Int) -> Unit,
    isLeapMonth: Boolean, onLeapMonthChange: (Boolean) -> Unit
) {
    SectionContainer("日期") {
        if (!useLunarCalendar) {
            // 公历 DatePicker
            DatePickerDialog(
                selectedDate = targetDate,
                onDateSelected = onDateChange
            )
        } else {
            // 农历选择器（简化：使用下拉菜单）
            // 年份
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("农历年", fontSize = 13.sp, color = AppColors.textSecondaryLight)
                Spacer(modifier = Modifier.weight(1f))
                // 简化年份输入
                OutlinedTextField(
                    value = lunarYear.toString(),
                    onValueChange = { it.toIntOrNull()?.let { y -> onLunarYearChange(y) } },
                    modifier = Modifier.width(100.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // 月份
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("农历月", fontSize = 13.sp, color = AppColors.textSecondaryLight)
                Spacer(modifier = Modifier.weight(1f))
                OutlinedTextField(
                    value = if (lunarMonth > 0) lunarMonth.toString() else "",
                    onValueChange = { it.toIntOrNull()?.let { m -> onLunarMonthChange(m.coerceIn(1, 12)) } },
                    modifier = Modifier.width(80.dp),
                    placeholder = { Text("月") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // 日
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("农历日", fontSize = 13.sp, color = AppColors.textSecondaryLight)
                Spacer(modifier = Modifier.weight(1f))
                OutlinedTextField(
                    value = if (lunarDay > 0) lunarDay.toString() else "",
                    onValueChange = { it.toIntOrNull()?.let { d -> onLunarDayChange(d.coerceIn(1, 30)) } },
                    modifier = Modifier.width(80.dp),
                    placeholder = { Text("日") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // 闰月
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("闰月", fontSize = 14.sp, color = AppColors.textPrimaryLight)
                Spacer(modifier = Modifier.weight(1f))
                Switch(checked = isLeapMonth, onCheckedChange = onLeapMonthChange)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 历法切换
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("使用农历", fontSize = 15.sp, color = AppColors.textPrimaryLight)
                Text(
                    if (useLunarCalendar) "直接设置农历月日，自动换算公历" else "开启后直接输入农历日期",
                    fontSize = 11.sp,
                    color = AppColors.textSecondaryLight
                )
            }
            Switch(checked = useLunarCalendar, onCheckedChange = { onUseLunarChange(it) })
        }
    }
}

@Composable
private fun DatePickerDialog(selectedDate: Date, onDateSelected: (Date) -> Unit) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time
    )
    var showDialog by remember { mutableStateOf(false) }

    TextButton(onClick = { showDialog = true }) {
        val fmt = SimpleDateFormat("yyyy年M月d日", Locale.CHINESE)
        Text(fmt.format(selectedDate), color = AppColors.primary)
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(Date(it)) }
                    showDialog = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// MARK: - 分类与标签

@Composable
private fun CategorySection(
    categories: List<Category>,
    selectedCategoryID: UUID?,
    onCategorySelected: (UUID?) -> Unit,
    tags: List<String>,
    tagText: String,
    onTagTextChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit
) {
    SectionContainer("分类与标签") {
        Text("所属分类", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondaryLight)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip("未分类", null, selectedCategoryID == null) { onCategorySelected(null) }
            }
            items(categories.size) { idx ->
                val cat = categories[idx]
                FilterChip(cat.name, null, selectedCategoryID == cat.id) { onCategorySelected(cat.id) }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 标签
        Text("标签", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondaryLight)

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = tagText,
                onValueChange = onTagTextChange,
                placeholder = { Text("输入标签后回车") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
            IconButton(onClick = onAddTag) {
                Icon(Icons.Filled.AddCircle, null, tint = AppColors.accent, modifier = Modifier.size(22.dp))
            }
        }

        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.forEach { tag ->
                    Surface(shape = RoundedCornerShape(20.dp), color = AppColors.primary.copy(alpha = 0.1f)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("#$tag", fontSize = 13.sp, color = AppColors.primary)
                            IconButton(onClick = { onRemoveTag(tag) }, modifier = Modifier.size(16.dp)) {
                                Icon(Icons.Filled.Close, null, modifier = Modifier.size(8.dp), tint = AppColors.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// MARK: - 卡片样式

@Composable
private fun CardStyleSection(
    bgColorHex: String, onBgColorChange: (String) -> Unit,
    bgEndColorHex: String, onBgEndColorChange: (String) -> Unit,
    textColorHex: String, onTextColorChange: (String) -> Unit,
    useGradient: Boolean, onUseGradientChange: (Boolean) -> Unit,
    fontSize: Double, onFontSizeChange: (Double) -> Unit,
    cornerRadius: Double, onCornerRadiusChange: (Double) -> Unit,
    shadowRadius: Double, onShadowRadiusChange: (Double) -> Unit
) {
    SectionContainer("卡片样式") {
        // 背景颜色
        Text("背景颜色", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondaryLight)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.fromHex(bgColorHex))
            )
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedTextField(
                value = bgColorHex,
                onValueChange = onBgColorChange,
                modifier = Modifier.width(100.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 渐变
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("渐变底色", fontSize = 15.sp, color = AppColors.textPrimaryLight)
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = useGradient, onCheckedChange = onUseGradientChange)
        }

        if (useGradient) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("渐变结束色", fontSize = 12.sp, color = AppColors.textSecondaryLight)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.fromHex(bgEndColorHex.ifEmpty { bgColorHex }))
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = bgEndColorHex,
                    onValueChange = onBgEndColorChange,
                    modifier = Modifier.width(100.dp),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 文字颜色
        Text("文字颜色", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondaryLight)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.fromHex(textColorHex))
            )
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedTextField(
                value = textColorHex,
                onValueChange = onTextColorChange,
                modifier = Modifier.width(100.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 字号
        Text("标题字号: ${fontSize.toInt()}", fontSize = 12.sp, color = AppColors.textSecondaryLight)
        Slider(
            value = fontSize.toFloat(),
            onValueChange = { onFontSizeChange(it.toDouble()) },
            valueRange = 14f..48f,
            steps = 16
        )

        // 圆角
        Text("圆角: ${cornerRadius.toInt()}", fontSize = 12.sp, color = AppColors.textSecondaryLight)
        Slider(
            value = cornerRadius.toFloat(),
            onValueChange = { onCornerRadiusChange(it.toDouble()) },
            valueRange = 0f..28f
        )

        // 阴影
        Text("阴影: ${shadowRadius.toInt()}", fontSize = 12.sp, color = AppColors.textSecondaryLight)
        Slider(
            value = shadowRadius.toFloat(),
            onValueChange = { onShadowRadiusChange(it.toDouble()) },
            valueRange = 0f..20f
        )
    }
}

// MARK: - 提醒设置

@Composable
private fun ReminderSettingsSection(
    reminderEnabled: Boolean, onReminderEnabledChange: (Boolean) -> Unit,
    advanceDays: Int, onAdvanceDaysChange: (Int) -> Unit,
    reminderHour: Int, onReminderHourChange: (Int) -> Unit,
    reminderMinute: Int, onReminderMinuteChange: (Int) -> Unit,
    reminderNote: String, onReminderNoteChange: (String) -> Unit,
    isYearlyRepeat: Boolean, onYearlyRepeatChange: (Boolean) -> Unit
) {
    SectionContainer("提醒设置") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("启用提醒", fontSize = 15.sp, color = AppColors.textPrimaryLight)
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = reminderEnabled, onCheckedChange = onReminderEnabledChange)
        }

        if (reminderEnabled) {
            Spacer(modifier = Modifier.height(12.dp))

            Text("提前天数", fontSize = 13.sp, color = AppColors.textSecondaryLight)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 1, 3, 7, 15, 30).forEach { d ->
                    FilterChip(if (d == 0) "当天" else "${d}天", null, advanceDays == d) {
                        onAdvanceDaysChange(d)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("提醒时间", fontSize = 13.sp, color = AppColors.textSecondaryLight)
                Spacer(modifier = Modifier.weight(1f))
                OutlinedTextField(
                    value = reminderHour.toString().padStart(2, '0'),
                    onValueChange = { it.toIntOrNull()?.let { h -> onReminderHourChange(h.coerceIn(0, 23)) } },
                    modifier = Modifier.width(60.dp),
                    singleLine = true
                )
                Text(":", fontSize = 16.sp)
                OutlinedTextField(
                    value = reminderMinute.toString().padStart(2, '0'),
                    onValueChange = { it.toIntOrNull()?.let { m -> onReminderMinuteChange(m.coerceIn(0, 59)) } },
                    modifier = Modifier.width(60.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = reminderNote,
                onValueChange = onReminderNoteChange,
                placeholder = { Text("提醒备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("每年循环", fontSize = 15.sp, color = AppColors.textPrimaryLight)
                Spacer(modifier = Modifier.weight(1f))
                Switch(checked = isYearlyRepeat, onCheckedChange = onYearlyRepeatChange)
            }
        }
    }
}

// MARK: - Section 容器

@Composable
private fun SectionContainer(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.md.dp),
        color = AppColors.secondaryBgLight
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(title)
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimaryLight)
}
