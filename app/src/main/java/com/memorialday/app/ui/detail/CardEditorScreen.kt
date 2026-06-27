// 纪念日 - 纪念日编辑视图
// 对应 iOS: CardEditorView.swift

@file:OptIn(ExperimentalMaterial3Api::class)

package com.memorialday.app.ui.detail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
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
    var shadowRadius by remember { mutableStateOf(editDay?.shadowRadius ?: 8.0) }  // 默认阴影强度改为8
    var selectedIcon by remember { mutableStateOf("favorite") }

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
                .padding(bottom = 100.dp),
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
                shadowRadius = shadowRadius, onShadowRadiusChange = { shadowRadius = it },
                iconName = selectedIcon, onIconChange = { selectedIcon = it }
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

// MARK: - 日期设置（滚轮选择器）

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
    val cal = Calendar.getInstance()
    cal.time = targetDate
    var displayYear = cal.get(Calendar.YEAR)
    var displayMonth = cal.get(Calendar.MONTH) + 1
    var displayDay = cal.get(Calendar.DAY_OF_MONTH)

    fun updateDate() {
        val c = Calendar.getInstance()
        c.set(displayYear, displayMonth - 1, displayDay, 0, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        onDateChange(c.time)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.md.dp),
        color = AppColors.secondaryBgLight
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 模块标题：日期设置（加粗）
            Text(
                "日期设置",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimaryLight
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (!useLunarCalendar) {
                // ---------- 公历：年月滚轮选择器 ----------
                // 顶部展示当前选中年月（粉色文字+下拉箭头）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = AppColors.accent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${displayYear}年${displayMonth}月${displayDay}日",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.accent
                    )
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        null,
                        modifier = Modifier.size(20.dp),
                        tint = AppColors.accent
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 年月日滚轮区域：三列
                Row(modifier = Modifier.fillMaxWidth()) {
                    // 年份选择
                    AndroidView(
                        modifier = Modifier.weight(1f).height(160.dp),
                        factory = { ctx ->
                            android.widget.NumberPicker(ctx).apply {
                                minValue = 1901; maxValue = 2999; value = displayYear
                                setOnValueChangedListener { _, _, newVal ->
                                    displayYear = newVal; updateDate()
                                }
                                descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
                            }
                        },
                        update = { picker -> picker.value = displayYear }
                    )
                    // 月份选择
                    AndroidView(
                        modifier = Modifier.weight(1f).height(160.dp),
                        factory = { ctx ->
                            android.widget.NumberPicker(ctx).apply {
                                minValue = 1; maxValue = 12; value = displayMonth
                                setDisplayedValues(arrayOf("1月","2月","3月","4月","5月","6月","7月","8月","9月","10月","11月","12月"))
                                setOnValueChangedListener { _, _, newVal ->
                                    displayMonth = newVal; updateDate()
                                }
                                descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
                            }
                        },
                        update = { picker -> picker.value = displayMonth }
                    )
                    // 日选择
                    AndroidView(
                        modifier = Modifier.weight(1f).height(160.dp),
                        factory = { ctx ->
                            android.widget.NumberPicker(ctx).apply {
                                minValue = 1; maxValue = 31; value = displayDay
                                setOnValueChangedListener { _, _, newVal ->
                                    displayDay = newVal; updateDate()
                                }
                                descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
                            }
                        },
                        update = { picker -> picker.value = displayDay }
                    )
                }
            } else {
                // ---------- 农历：年月日滚轮 ----------
                Row(modifier = Modifier.fillMaxWidth()) {
                    // 农历年
                    AndroidView(
                        modifier = Modifier.weight(1f).height(160.dp),
                        factory = { ctx ->
                            android.widget.NumberPicker(ctx).apply {
                                minValue = 1901; maxValue = 2100; value = lunarYear
                                setOnValueChangedListener { _, _, newVal -> onLunarYearChange(newVal) }
                                descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
                            }
                        },
                        update = { picker -> picker.value = lunarYear }
                    )
                    // 农历月
                    AndroidView(
                        modifier = Modifier.weight(1f).height(160.dp),
                        factory = { ctx ->
                            android.widget.NumberPicker(ctx).apply {
                                minValue = 1; maxValue = 12; value = if (lunarMonth > 0) lunarMonth else 1
                                setDisplayedValues(arrayOf("正","二","三","四","五","六","七","八","九","十","冬","腊"))
                                setOnValueChangedListener { _, _, newVal -> onLunarMonthChange(newVal) }
                                descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
                            }
                        },
                        update = { picker -> picker.value = if (lunarMonth > 0) lunarMonth else 1 }
                    )
                    // 农历日
                    AndroidView(
                        modifier = Modifier.weight(1f).height(160.dp),
                        factory = { ctx ->
                            android.widget.NumberPicker(ctx).apply {
                                minValue = 1; maxValue = 30; value = if (lunarDay > 0) lunarDay else 1
                                setOnValueChangedListener { _, _, newVal -> onLunarDayChange(newVal) }
                                descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
                            }
                        },
                        update = { picker -> picker.value = if (lunarDay > 0) lunarDay else 1 }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("闰月", fontSize = 14.sp, color = AppColors.textPrimaryLight)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = isLeapMonth, onCheckedChange = onLeapMonthChange)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = AppColors.textTertiaryLight.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // 历法切换
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("使用农历", fontSize = 15.sp, color = AppColors.textPrimaryLight)
                    Text(
                        if (useLunarCalendar) "直接设置农历月日，自动换算公历"
                        else "开启后直接输入农历日期",
                        fontSize = 11.sp,
                        color = AppColors.textSecondaryLight
                    )
                }
                Switch(
                    checked = useLunarCalendar,
                    onCheckedChange = { onUseLunarChange(it) }
                )
            }
        }
    }
}

// MARK: - 日期选择器（滚轮样式）

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
    shadowRadius: Double, onShadowRadiusChange: (Double) -> Unit,
    iconName: String, onIconChange: (String) -> Unit
) {
    SectionContainer("卡片样式") {
        // 背景颜色
        Text("背景颜色", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondaryLight)
        Row(verticalAlignment = Alignment.CenterVertically) {
            var showColorPicker by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.fromHex(bgColorHex))
                    .clickable { showColorPicker = true }
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = { showColorPicker = true }) {
                Text("色板", fontSize = 13.sp, color = AppColors.accent)
            }
            Spacer(modifier = Modifier.width(4.dp))
            OutlinedTextField(
                value = bgColorHex,
                onValueChange = onBgColorChange,
                modifier = Modifier.width(100.dp),
                singleLine = true
            )
            if (showColorPicker) {
                AlertDialog(
                    onDismissRequest = { showColorPicker = false },
                    title = { Text("选择颜色") },
                    text = { ColorSpectrumPicker(currentColor = bgColorHex, onColorSelected = { onBgColorChange(it); showColorPicker = false }) },
                    confirmButton = { TextButton(onClick = { showColorPicker = false }) { Text("确定") } }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 渐变
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("渐变底色", fontSize = 15.sp, color = AppColors.textPrimaryLight)
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = useGradient, onCheckedChange = onUseGradientChange,
                colors = SwitchDefaults.colors(checkedThumbColor = AppColors.accent, checkedTrackColor = AppColors.accent.copy(alpha = 0.3f)))
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
            Spacer(modifier = Modifier.height(6.dp))
            // 彩色取色圆环 + Hex文字提示 + 色板按钮
            Row(verticalAlignment = Alignment.CenterVertically) {
                var show by remember { mutableStateOf(false) }
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(20.dp))
                    .background(Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)))
                    .clickable { show = true })
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { show = true }) { Text("色板", fontSize = 13.sp, color = AppColors.accent) }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedTextField(value = bgEndColorHex, onValueChange = onBgEndColorChange, modifier = Modifier.width(100.dp), singleLine = true)
                if (show) ColorGridDialog("选择渐变颜色", bgEndColorHex.ifEmpty { bgColorHex }, { onBgEndColorChange(it); show = false }, { show = false })
            }
            // 下方配套色板选择区域
            Spacer(modifier = Modifier.height(6.dp))
            ColorSpectrumPicker(currentColor = bgEndColorHex.ifEmpty { bgColorHex }, onColorSelected = onBgEndColorChange)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 文字颜色
        Text("文字颜色", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondaryLight)
        Row(verticalAlignment = Alignment.CenterVertically) {
            var show by remember { mutableStateOf(false) }
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(20.dp))
                .background(Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)))
                .clickable { show = true })
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = { show = true }) { Text("色板", fontSize = 13.sp, color = AppColors.accent) }
            Spacer(modifier = Modifier.width(4.dp))
            OutlinedTextField(value = textColorHex, onValueChange = onTextColorChange, modifier = Modifier.width(100.dp), singleLine = true)
            if (show) ColorGridDialog("选择文字颜色", textColorHex, { onTextColorChange(it); show = false }, { show = false })
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. 图标选择栏：2行16个
        val iconList = listOf(
            "favorite" to Icons.Filled.Favorite, "star" to Icons.Filled.Star,
            "card_giftcard" to Icons.Filled.CardGiftcard, "auto_awesome" to Icons.Filled.AutoAwesome,
            "local_fire_department" to Icons.Filled.LocalFireDepartment, "dark_mode" to Icons.Filled.DarkMode,
            "light_mode" to Icons.Filled.LightMode, "balloon" to Icons.Filled.Circle,
            "crown" to Icons.Filled.Star, "auto_awesome_mosaic" to Icons.Filled.GridView,
            "celebration" to Icons.Filled.Celebration, "celebration" to Icons.Filled.Celebration,
            "menu_book" to Icons.Filled.MenuBook, "work" to Icons.Filled.Work,
            "flight" to Icons.Filled.Flight, "notifications" to Icons.Filled.Notifications
        )
        Text("选择图标", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textSecondaryLight)
        (0..1).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                iconList.drop(row * 8).take(8).forEach { (name, icon) ->
                    val selected = iconName == name
                    IconButton(onClick = { onIconChange(name) },
                        modifier = Modifier.size(36.dp)
                            .then(if (selected) Modifier.background(AppColors.accent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            else Modifier.background(Color.Transparent))
                    ) { Icon(icon, null, Modifier.size(18.dp), tint = if (selected) AppColors.accent else AppColors.textSecondaryLight) }
                }
            }
            if (row == 0) Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. 滑块（粉色系）
        // 字号
        Text("标题字号: ${fontSize.toInt()}", fontSize = 12.sp, color = AppColors.textSecondaryLight)
        Slider(value = fontSize.toFloat(), onValueChange = { onFontSizeChange(it.toDouble()) },
            valueRange = 14f..48f, steps = 16,
            colors = SliderDefaults.colors(thumbColor = AppColors.accent, activeTrackColor = AppColors.accent))

        // 圆角
        Text("卡片圆角: ${cornerRadius.toInt()}", fontSize = 12.sp, color = AppColors.textSecondaryLight)
        Slider(value = cornerRadius.toFloat(), onValueChange = { onCornerRadiusChange(it.toDouble()) },
            valueRange = 0f..28f,
            colors = SliderDefaults.colors(thumbColor = AppColors.accent, activeTrackColor = AppColors.accent))

        Text("阴影强度: ${shadowRadius.toInt()}", fontSize = 12.sp, color = AppColors.textSecondaryLight)
        Slider(value = shadowRadius.toFloat(), onValueChange = { onShadowRadiusChange(it.toDouble()) },
            valueRange = 0f..20f,
            colors = SliderDefaults.colors(thumbColor = AppColors.accent, activeTrackColor = AppColors.accent))
    }
}

// MARK: - 颜色选择弹窗

@Composable
private fun ColorGridDialog(title: String, currentColor: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { ColorSpectrumPicker(currentColor = currentColor, onColorSelected = onSelect) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("确定") } }
    )
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

            Text("提醒时间", fontSize = 13.sp, color = AppColors.textSecondaryLight)
            Spacer(modifier = Modifier.height(4.dp))
            // 时分滚轮：inline NumberPicker
            Row(modifier = Modifier.fillMaxWidth()) {
                AndroidView(
                    modifier = Modifier.weight(1f).height(160.dp),
                    factory = { ctx ->
                        android.widget.NumberPicker(ctx).apply {
                            minValue = 0; maxValue = 23; value = reminderHour
                            setOnValueChangedListener { _, _, newVal -> onReminderHourChange(newVal) }
                            descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
                        }
                    },
                    update = { picker -> picker.value = reminderHour }
                )
                AndroidView(
                    modifier = Modifier.weight(1f).height(160.dp),
                    factory = { ctx ->
                        android.widget.NumberPicker(ctx).apply {
                            minValue = 0; maxValue = 59; value = reminderMinute
                            setDisplayedValues(Array(60) { it.toString().padStart(2, '0') })
                            setOnValueChangedListener { _, _, newVal -> onReminderMinuteChange(newVal) }
                            descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
                        }
                    },
                    update = { picker -> picker.value = reminderMinute }
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

// MARK: - 颜色光谱网格

private val colorGrid = listOf(
    "#FFFFFF","#F5F0EB","#FAF0E6","#FFF5EE","#FFE4E1","#FFDAB9","#FFC0CB","#FFB6C1",
    "#E8F5E9","#C8E6C9","#A5D6A7","#81C784","#BBDEFB","#90CAF9","#64B5F6","#42A5F5",
    "#FFF8E1","#FFECB3","#FFE082","#FFD54F","#F3E5F5","#E1BEE7","#CE93D8","#BA68C8",
    "#FFEBEE","#FFCDD2","#EF9A9A","#E57373","#FFF3E0","#FFE0B2","#FFCC80","#FFB74D",
    "#E0F2F1","#B2DFDB","#80CBC4","#4DB6AC","#ECEFF1","#CFD8DC","#B0BEC5","#90A4AE",
    "#2C2C2C","#333333","#555555","#777777","#999999","#BBBBBB","#DDDDDD","#EEEEEE"
)

@Composable
private fun ColorSpectrumPicker(
    currentColor: String,
    onColorSelected: (String) -> Unit
) {
    val chunks = colorGrid.chunked(8)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        chunks.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { hex ->
                    val isSelected = hex.equals(currentColor, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.fromHex(hex))
                            .then(
                                if (isSelected) Modifier.border(2.dp, AppColors.accent, RoundedCornerShape(4.dp))
                                else Modifier.border(0.5.dp, Color.LightGray, RoundedCornerShape(4.dp))
                            )
                            .clickable { onColorSelected(hex) }
                    )
                }
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
