// 纪念日 - 纪念日详情视图
// 对应 iOS: CardDetailView.swift

package com.memorialday.app.ui.detail

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorialday.app.models.MemorialDay
import com.memorialday.app.models.DayType
import com.memorialday.app.services.ImageService
import com.memorialday.app.ui.components.TimeBlock
import com.memorialday.app.ui.theme.AppColors
import com.memorialday.app.ui.theme.AppRadius
import com.memorialday.app.ui.theme.AppSpacing
import com.memorialday.app.utils.fromHex
import com.memorialday.app.viewmodels.MemorialDayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    day: MemorialDay,
    onDismiss: () -> Unit
) {
    val viewModel = remember { MemorialDayViewModel.getInstance() }
    var currentDay by remember { mutableStateOf(day) }
    var showEditor by remember { mutableStateOf(false) }
    var showDeleteAlert by remember { mutableStateOf(false) }
    var currentSeconds by remember { mutableIntStateOf(0) }

    // 定时器（每秒更新）
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            currentSeconds++
            // 从 ViewModel 刷新最新数据
            viewModel.days.value.find { it.id == day.id }?.let {
                currentDay = it
            }
        }
    }

    // 编辑器关闭后刷新
    LaunchedEffect(showEditor) {
        if (!showEditor) {
            viewModel.days.value.find { it.id == day.id }?.let {
                currentDay = it
            }
            viewModel.refreshDisplay()
        }
    }

    if (showDeleteAlert) {
        AlertDialog(
            onDismissRequest = { showDeleteAlert = false },
            title = { Text("确认删除「${currentDay.title}」？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDay(currentDay.id)
                    showDeleteAlert = false
                    onDismiss()
                }) { Text("删除", color = AppColors.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAlert = false }) { Text("取消") }
            }
        )
    }

    if (showEditor) {
        CardEditorScreen(
            editDay = currentDay,
            onDismiss = { showEditor = false }
        )
    }

    val cardTextColor = Color.fromHex(currentDay.textColorHex)
    val cardSecondaryTextColor = Color.fromHex(currentDay.textColorHex).copy(alpha = 0.65f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("详情", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, null)
                    }
                },
                actions = {
                    TextButton(onClick = { showEditor = true }) {
                        Text("编辑", color = AppColors.accent, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.backgroundLight
                )
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
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 第一区块：主计数区
            MainCountCard(
                day = currentDay,
                cardTextColor = cardTextColor,
                cardSecondaryTextColor = cardSecondaryTextColor
            )

            // 第二区块：精确倒计时
            if (currentDay.dayType == DayType.COUNTDOWN) {
                PreciseCountdownCard(day = currentDay)
            }

            // 备注
            if (currentDay.notes.isNotEmpty()) {
                NotesCard(notes = currentDay.notes)
            }

            // 标签
            if (currentDay.tags.isNotEmpty()) {
                TagsCard(tags = currentDay.tags)
            }

            // 第三区块：提醒
            ReminderCard(day = currentDay)

            // 第四区块：操作按钮
            ActionButtonsCard(
                onSharePoster = { /* 生成并分享海报 */ },
                onDelete = { showDeleteAlert = true }
            )
        }
    }
}

@Composable
private fun MainCountCard(
    day: MemorialDay,
    cardTextColor: Color,
    cardSecondaryTextColor: Color
) {
    val bgColor = Color.fromHex(day.backgroundColorHex)
    val gradient = if (day.showGradient && day.backgroundEndColorHex != null) {
        Brush.linearGradient(listOf(bgColor, Color.fromHex(day.backgroundEndColorHex!!)))
    } else {
        Brush.linearGradient(listOf(bgColor.copy(alpha = 0.9f), bgColor))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .background(brush = gradient, shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                day.title,
                fontSize = day.fontSize.toInt().sp,
                fontWeight = FontWeight.SemiBold,
                color = cardTextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "${day.displayDayCount}",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = cardTextColor,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                day.countdownDescription,
                fontSize = 16.sp,
                color = cardSecondaryTextColor
            )
        }
    }
}

@Composable
private fun PreciseCountdownCard(day: MemorialDay) {
    val detail = day.detailedTimeRemaining
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.md.dp),
        color = AppColors.cardLight,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader("精确倒计时")
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimeBlock(detail.days, "天", AppColors.info, modifier = Modifier.weight(1f))
                TimeBlock(detail.hours, "时", AppColors.success, modifier = Modifier.weight(1f))
                TimeBlock(detail.minutes, "分", AppColors.warning, modifier = Modifier.weight(1f))
                TimeBlock(detail.seconds, "秒", AppColors.accent, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.md.dp),
        color = AppColors.cardLight,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader("备注")
            Spacer(modifier = Modifier.height(6.dp))
            Text(notes, fontSize = 15.sp, color = AppColors.textPrimaryLight, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun TagsCard(tags: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.md.dp),
        color = AppColors.cardLight,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader("标签")
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppColors.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            "#$tag",
                            fontSize = 13.sp,
                            color = AppColors.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(day: MemorialDay) {
    val settings = day.reminderSettings
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.md.dp),
        color = AppColors.cardLight,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader("提醒")
            Spacer(modifier = Modifier.height(10.dp))
            if (!settings.isEnabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.NotificationsOff, null, modifier = Modifier.size(14.dp), tint = AppColors.textTertiaryLight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("未设置提醒", fontSize = 14.sp, color = AppColors.textSecondaryLight)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Notifications, null, modifier = Modifier.size(13.dp), tint = AppColors.warning)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(settings.displayDescription, fontSize = 14.sp, color = AppColors.textPrimaryLight)
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsCard(onSharePoster: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.md.dp),
        color = AppColors.cardLight,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSharePoster,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.info)
            ) {
                Icon(Icons.Filled.Share, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("分享海报", color = Color.White, fontSize = 14.sp)
            }
            Button(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.error)
            ) {
                Icon(Icons.Filled.Delete, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("删除", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.textPrimaryLight
    )
}
