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
    onDismiss: () -> Unit,
    onEdit: (MemorialDay) -> Unit = {}
) {
    val viewModel = remember { MemorialDayViewModel.getInstance() }
    var currentDay by remember { mutableStateOf(day) }
    var showDeleteAlert by remember { mutableStateOf(false) }

    // 从 ViewModel 刷新最新数据
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            viewModel.days.value.find { it.id == day.id }?.let { currentDay = it }
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("详情", fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, null) } },
                    actions = {
                        TextButton(onClick = { onEdit(currentDay) }) {
                            Text("编辑", color = AppColors.accent, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.backgroundLight)
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
                val cardTextColor = Color.fromHex(currentDay.textColorHex)
                val cardSecondaryTextColor = Color.fromHex(currentDay.textColorHex).copy(alpha = 0.65f)
                val bgColor = Color.fromHex(currentDay.backgroundColorHex)
                val gradient = if (currentDay.showGradient && currentDay.backgroundEndColorHex != null) {
                    Brush.linearGradient(listOf(bgColor, Color.fromHex(currentDay.backgroundEndColorHex!!)))
                } else {
                    Brush.linearGradient(listOf(bgColor.copy(alpha = 0.9f), bgColor))
                }

                // 第一区块：主计数区
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.background(brush = gradient, shape = RoundedCornerShape(20.dp))
                            .padding(horizontal = 24.dp).padding(top = 20.dp, bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(currentDay.title, fontSize = currentDay.fontSize.toInt().sp,
                            fontWeight = FontWeight.SemiBold, color = cardTextColor, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("${currentDay.displayDayCount}", fontSize = 72.sp, fontWeight = FontWeight.Bold,
                            color = cardTextColor, maxLines = 1)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(currentDay.countdownDescription, fontSize = 16.sp, color = cardSecondaryTextColor)
                    }
                }

                // 第二区块：精确倒计时
                if (currentDay.dayType == DayType.COUNTDOWN) {
                    val detail = currentDay.detailedTimeRemaining
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(AppRadius.md.dp),
                        color = AppColors.cardLight, shadowElevation = 2.dp) {
                        Column(Modifier.padding(16.dp)) {
                            Text("精确倒计时", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimaryLight)
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                TimeBlock(detail.days, "天", AppColors.info, Modifier.weight(1f))
                                TimeBlock(detail.hours, "时", AppColors.success, Modifier.weight(1f))
                                TimeBlock(detail.minutes, "分", AppColors.warning, Modifier.weight(1f))
                                TimeBlock(detail.seconds, "秒", AppColors.accent, Modifier.weight(1f))
                            }
                        }
                    }
                }

                // 备注
                if (currentDay.notes.isNotEmpty()) {
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(AppRadius.md.dp),
                        color = AppColors.cardLight, shadowElevation = 2.dp) {
                        Column(Modifier.padding(16.dp)) {
                            Text("备注", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimaryLight)
                            Spacer(Modifier.height(6.dp))
                            Text(currentDay.notes, fontSize = 15.sp, color = AppColors.textPrimaryLight, lineHeight = 22.sp)
                        }
                    }
                }

                // 标签
                if (currentDay.tags.isNotEmpty()) {
                    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(AppRadius.md.dp),
                        color = AppColors.cardLight, shadowElevation = 2.dp) {
                        Column(Modifier.padding(16.dp)) {
                            Text("标签", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimaryLight)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                currentDay.tags.forEach { tag ->
                                    Surface(shape = RoundedCornerShape(20.dp), color = AppColors.primary.copy(alpha = 0.1f)) {
                                        Text("#$tag", fontSize = 13.sp, color = AppColors.primary,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // 提醒
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(AppRadius.md.dp),
                    color = AppColors.cardLight, shadowElevation = 2.dp) {
                    Column(Modifier.padding(16.dp)) {
                        Text("提醒", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimaryLight)
                        Spacer(Modifier.height(10.dp))
                        if (!currentDay.reminderSettings.isEnabled) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.NotificationsOff, null, Modifier.size(14.dp), tint = AppColors.textTertiaryLight)
                                Spacer(Modifier.width(8.dp))
                                Text("未设置提醒", fontSize = 14.sp, color = AppColors.textSecondaryLight)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Notifications, null, Modifier.size(13.dp), tint = AppColors.warning)
                                Spacer(Modifier.width(10.dp))
                                Text(currentDay.reminderSettings.displayDescription, fontSize = 14.sp, color = AppColors.textPrimaryLight)
                            }
                        }
                    }
                }

                // 操作按钮
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 分享海报
                    Button(onClick = { }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.info)) {
                        Icon(Icons.Filled.Share, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("分享海报", color = Color.White, fontSize = 14.sp)
                    }
                    // 删除
                    Button(onClick = { showDeleteAlert = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.error)) {
                        Icon(Icons.Filled.Delete, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("删除", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
