// 纪念日 - 模板选择视图
// 对应 iOS: TemplatePickerView.swift

package com.memorialday.app.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorialday.app.models.MemorialTemplate
import com.memorialday.app.ui.detail.CardEditorScreen
import com.memorialday.app.ui.theme.AppColors
import com.memorialday.app.utils.fromHex
import com.memorialday.app.viewmodels.MemorialDayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePickerScreen(onDismiss: () -> Unit) {
    val viewModel = remember { MemorialDayViewModel.getInstance() }
    val templates = remember { MemorialTemplate.builtInTemplates }
    var createdDay by remember { mutableStateOf<com.memorialday.app.models.MemorialDay?>(null) }

    val groupedTemplates = remember(templates) {
        templates.groupBy { it.categoryName }.toList().sortedBy { it.first }
    }

    if (createdDay != null) {
        CardEditorScreen(editDay = createdDay, onDismiss = {
            createdDay = null
            onDismiss()
        })
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    TextButton(onClick = onDismiss) {
                        Text("关闭", color = AppColors.textSecondaryLight)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppColors.backgroundLight)
                .padding(vertical = 16.dp)
                .padding(bottom = 100.dp)
        ) {
            // 顶部说明
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "选择模板快速创建",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimaryLight
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "选择一个模板，快速创建纪念日后再调整细节",
                        fontSize = 14.sp,
                        color = AppColors.textSecondaryLight
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // 全部模板（网格）- 用 Column 替代 LazyVerticalGrid 避免嵌套滚动
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    val rows = templates.chunked(2)
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { template ->
                                TemplateCard(
                                    template = template,
                                    onClick = { createdDay = viewModel.createFromTemplate(template) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // 按分类分组
            groupedTemplates.forEach { (category, templateList) ->
                item { Spacer(modifier = Modifier.height(20.dp)) }
                item {
                    Text(
                        category,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimaryLight,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                item { Spacer(modifier = Modifier.height(10.dp)) }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(templateList.size) { index ->
                            val template = templateList[index]
                            TemplateCard(
                                template = template,
                                onClick = { createdDay = viewModel.createFromTemplate(template) },
                                modifier = Modifier.width(150.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun TemplateCard(template: MemorialTemplate, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cardColor = when (template.categoryName) {
        "生日" -> Color.fromHex("#FF6B6B")
        "恋爱" -> Color.fromHex("#FF69B4")
        "纪念日" -> Color.fromHex("#FFD700")
        "考试" -> Color.fromHex("#9B59B6")
        "还款" -> Color.fromHex("#E74C3C")
        "节日" -> Color.fromHex("#FFA500")
        "工作" -> Color.fromHex("#4A90D9")
        "旅行" -> Color.fromHex("#1ABC9C")
        "健康" -> Color.fromHex("#2ECC71")
        "其他" -> Color.fromHex("#95A5A6")
        else -> Color.fromHex("#8B9DC3")
    }

    val icon = when (template.iconName) {
        "card_giftcard" -> Icons.Filled.CardGiftcard
        "favorite" -> Icons.Filled.Favorite
        "auto_awesome" -> Icons.Filled.AutoAwesome
        "menu_book" -> Icons.Filled.MenuBook
        "credit_card" -> Icons.Filled.CreditCard
        "celebration" -> Icons.Filled.Celebration
        "work" -> Icons.Filled.Work
        "flight" -> Icons.Filled.Flight
        "star" -> Icons.Filled.Star
        "favorite_border" -> Icons.Filled.FavoriteBorder
        "attach_money" -> Icons.Filled.AttachMoney
        else -> Icons.Filled.Star
    }

    Surface(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.secondaryBgLight,
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(28.dp), tint = cardColor)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                template.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimaryLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "立即创建",
                fontSize = 11.sp,
                color = cardColor
            )
        }
    }
}
