// 纪念日 - 纪念日卡片视图
// 对应 iOS: Home/MemorialCardView.swift

package com.memorialday.app.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorialday.app.models.MemorialDay
import com.memorialday.app.ui.detail.CardDetailScreen
import com.memorialday.app.ui.theme.AppColors
import com.memorialday.app.utils.fromHex
import com.memorialday.app.viewmodels.MemorialDayViewModel
import kotlin.math.roundToInt

@Composable
fun MemorialCardView(
    day: MemorialDay,
    isSelectMode: Boolean,
    modifier: Modifier = Modifier
) {
    val viewModel = remember { MemorialDayViewModel.getInstance() }
    var showDetail by remember { mutableStateOf(false) }
    var showSwipeActions by remember { mutableStateOf(false) }
    var showDeleteAlert by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }

    val selectedDays by viewModel.selectedDays.collectAsState()
    val isSelected = selectedDays.contains(day.id)

    // 删除确认弹窗
    if (showDeleteAlert) {
        AlertDialog(
            onDismissRequest = { showDeleteAlert = false; showSwipeActions = false },
            title = { Text("确认删除「${day.title}」？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDay(day.id)
                    showDeleteAlert = false
                    showSwipeActions = false
                }) {
                    Text("删除", color = AppColors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAlert = false; showSwipeActions = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 详情页
    if (showDetail) {
        CardDetailScreen(day = day, onDismiss = { showDetail = false })
    }

    Box(modifier = modifier) {
        // 左侧操作按钮（滑动显示）
        if (showSwipeActions) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 置顶按钮
                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .height(100.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppColors.primary)
                        .clickable {
                            viewModel.togglePin(day.id)
                            showSwipeActions = false
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        if (day.isPinned) Icons.Filled.PushPin else Icons.Filled.PushPin,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Text(if (day.isPinned) "取消置顶" else "置顶", fontSize = 11.sp, color = Color.White)
                }

                // 删除按钮
                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .height(100.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppColors.error)
                        .clickable { showDeleteAlert = true },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Text("删除", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        // 卡片内容
        CardContent(
            day = day,
            isSelected = isSelected,
            isSelectMode = isSelectMode,
            onToggleSelect = { viewModel.toggleSelection(day.id) },
            onClick = { showDetail = true },
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -160f) {
                                showSwipeActions = true
                            } else if (offsetX > 160f) {
                                showSwipeActions = false
                            }
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX += dragAmount
                        }
                    )
                }
        )
    }
}

@Composable
private fun CardContent(
    day: MemorialDay,
    isSelected: Boolean,
    isSelectMode: Boolean,
    onToggleSelect: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = Color.fromHex(day.backgroundColorHex)
    val textColor = Color.fromHex(day.textColorHex)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(day.cornerRadius.dp))
            .background(
                if (day.showGradient && day.backgroundEndColorHex != null) {
                    Brush.linearGradient(
                        listOf(bgColor, Color.fromHex(day.backgroundEndColorHex!!))
                    )
                } else {
                    Brush.linearGradient(listOf(bgColor, bgColor))
                }
            )
            .clickable { if (isSelectMode) onToggleSelect() else onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 选择模式：左侧选择圈
        if (isSelectMode) {
            Icon(
                if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.Circle,
                null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 4.dp),
                tint = if (isSelected) AppColors.accent else AppColors.textTertiaryLight
            )
            Spacer(modifier = Modifier.width(16.dp))
        }

        // 左侧图标 + 天数
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(bgColor.copy(alpha = 0.4f), bgColor.copy(alpha = 0.2f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.Favorite,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = textColor.copy(alpha = 0.7f)
                )
                Text(
                    "${day.displayDayCount}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (day.dayType.name == "COUNTDOWN") AppColors.info else AppColors.success
                )
                Text(
                    if (day.dayType.name == "COUNTDOWN") "天后" else "天前",
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 中间文字
        Column(modifier = Modifier.weight(1f)) {
            Text(
                day.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                day.subtitle,
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.6f)
            )

            if (day.notes.isNotEmpty()) {
                Text(
                    day.notes,
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 标签
            if (day.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    day.tags.take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = textColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "#$tag",
                                fontSize = 10.sp,
                                color = textColor.copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // 右侧箭头
        Icon(
            Icons.Filled.ChevronRight,
            null,
            modifier = Modifier.size(14.dp),
            tint = textColor.copy(alpha = 0.4f)
        )

        // 置顶标记
        if (day.isPinned) {
            Box(modifier = Modifier.align(Alignment.Top)) {
                Icon(
                    Icons.Filled.PushPin,
                    null,
                    modifier = Modifier.size(12.dp).padding(8.dp),
                    tint = AppColors.warning
                )
            }
        }
    }
}

// Overlay: 置顶标记放在右上角需要重组 Parent
