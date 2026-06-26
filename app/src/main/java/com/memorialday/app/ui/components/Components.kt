// 纪念日 - 共用 UI 组件
// 对应 iOS: HomeView 中的 FilterChip, StatBadge, UpcomingMiniCard

package com.memorialday.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorialday.app.models.MemorialDay
import com.memorialday.app.ui.theme.AppColors

/** 筛选标签 */
@Composable
fun FilterChip(
    label: String,
    icon: ImageVector?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) AppColors.primary else Color.Transparent,
        border = if (!isSelected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(12.dp), tint = if (isSelected) Color.White else AppColors.textSecondaryLight)
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else AppColors.textSecondaryLight
            )
        }
    }
}

/** 统计徽章 */
@Composable
fun StatBadge(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("$count", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(title, fontSize = 11.sp, color = AppColors.textSecondaryLight)
    }
}

/** 即将到期小卡片 */
@Composable
fun UpcomingMiniCard(day: MemorialDay) {
    Surface(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(14.dp),
        color = AppColors.secondaryBgLight,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                day.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimaryLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "剩余 ${day.displayDayCount} 天",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.accent
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                day.subtitle,
                fontSize = 11.sp,
                color = AppColors.textSecondaryLight
            )
        }
    }
}

/** 时间块 */
@Composable
fun TimeBlock(value: Int, unit: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("$value", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(unit, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.85f))
    }
}

/** 详情行 */
@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(AppColors.textSecondaryLight.copy(alpha = 0.05f))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = AppColors.textSecondaryLight)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimaryLight)
    }
}

/** 工作日统计 */
@Composable
fun WorkdayStat(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 11.sp, color = Color.Gray.copy(alpha = 0.6f))
    }
}

/** 快捷换算按钮 */
@Composable
fun QuickConvertButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.primary.copy(alpha = 0.08f),
            contentColor = AppColors.primary
        )
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

/** 流式布局（简化版） */
@Composable
fun FlowLayout(
    spacing: Dp = 6.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // 使用 Row + 换行的简化实现
    // Android Compose 原生 FlowRow 在 1.4+ 可用
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        content()
    }
}
