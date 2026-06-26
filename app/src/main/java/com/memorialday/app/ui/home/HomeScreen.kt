// 纪念日 - 主页视图
// 对应 iOS: HomeView.swift

package com.memorialday.app.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorialday.app.models.MemorialDay
import com.memorialday.app.ui.components.FilterChip
import com.memorialday.app.ui.components.StatBadge
import com.memorialday.app.ui.components.UpcomingMiniCard
import com.memorialday.app.ui.detail.CardDetailScreen
import com.memorialday.app.ui.detail.CardEditorScreen
import com.memorialday.app.ui.templates.TemplatePickerScreen
import com.memorialday.app.ui.theme.AppColors
import com.memorialday.app.ui.theme.AppSpacing
import com.memorialday.app.viewmodels.MemorialDayViewModel
import com.memorialday.app.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val viewModel = remember { MemorialDayViewModel.getInstance() }
    val settingsVM = remember { SettingsViewModel.getInstance() }
    val days by viewModel.days.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategoryID by viewModel.selectedCategoryID.collectAsState()
    val upcomingDays by viewModel.upcomingDays.collectAsState()
    val selectedDays by viewModel.selectedDays.collectAsState()

    var showNewEditor by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showTemplates by remember { mutableStateOf(false) }
    var selectedDayForDetail by remember { mutableStateOf<MemorialDay?>(null) }
    var isSelectMode by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.refreshDisplay() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.backgroundLight)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部导航栏
            TopBar(
                isSelectMode = isSelectMode,
                selectedCount = selectedDays.size,
                totalCount = days.size,
                onSelectAll = { viewModel.selectAll() },
                onDeselectAll = { viewModel.deselectAll() },
                onExitSelect = {
                    isSelectMode = false
                    viewModel.deselectAll()
                },
                onDeleteSelected = { viewModel.deleteSelectedDays() },
                onSearchToggle = { showSearch = !showSearch },
                onTemplates = { showTemplates = true },
                onEnterSelect = { isSelectMode = true }
            )

            // 搜索栏
            AnimatedVisibility(visible = showSearch) {
                SearchBar(
                    searchText = searchText,
                    onSearchChange = {
                        searchText = it
                        viewModel.setSearchText(it)
                    }
                )
            }

            // 筛选条件栏
            FilterBar(
                categories = categories,
                selectedCategoryID = selectedCategoryID,
                onCategorySelected = { viewModel.applyCategoryFilter(it) }
            )

            // 主列表
            if (days.isEmpty()) {
                EmptyState(
                    onTemplates = { showTemplates = true },
                    onCreateNew = { showNewEditor = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // 快速统计
                    item {
                        QuickStatsBar(days = days)
                    }

                    // 即将到期
                    if (upcomingDays.isNotEmpty()) {
                        item {
                            UpcomingSection(upcomingDays = upcomingDays)
                        }
                    }

                    // 纪念日卡片列表
                    items(days, key = { it.id }) { day ->
                        MemorialCardView(
                            day = day,
                            isSelectMode = isSelectMode,
                            onCardTap = { selectedDayForDetail = day },
                            modifier = Modifier.padding(horizontal = AppSpacing.lg.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // 新建按钮
                    item {
                        NewDayButton(
                            onClick = { showNewEditor = true },
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                }
            }

            // 浮动按钮
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                FloatingButtons(
                    onTemplates = { showTemplates = true },
                    onCreateNew = { showNewEditor = true }
                )
            }
        }
    }

    // Sheets
    if (showNewEditor) {
        CardEditorScreen(onDismiss = { showNewEditor = false })
    }
    if (showTemplates) {
        TemplatePickerScreen(onDismiss = { showTemplates = false })
    }
    selectedDayForDetail?.let { day ->
        CardDetailScreen(day = day, onDismiss = { selectedDayForDetail = null })
    }
}

// MARK: - 顶部导航栏

@Composable
private fun TopBar(
    isSelectMode: Boolean,
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onExitSelect: () -> Unit,
    onDeleteSelected: () -> Unit,
    onSearchToggle: () -> Unit,
    onTemplates: () -> Unit,
    onEnterSelect: () -> Unit
) {
    val settingsVM = remember { SettingsViewModel.getInstance() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg.dp)
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectMode) {
            // 全选
            TextButton(onClick = {
                if (selectedCount == totalCount && totalCount > 0) onDeselectAll() else onSelectAll()
            }) {
                Icon(
                    imageVector = if (selectedCount == totalCount && totalCount > 0)
                        Icons.Filled.CheckCircle else Icons.Filled.Circle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AppColors.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (selectedCount == totalCount && totalCount > 0) "取消全选" else "全选",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.primary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text("已选 $selectedCount 项", fontSize = 13.sp, color = AppColors.textSecondaryLight)

            Spacer(modifier = Modifier.weight(1f))

            TopBarButton(icon = Icons.Filled.Close, onClick = onExitSelect)

            if (selectedCount > 0) {
                IconButton(onClick = onDeleteSelected) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "删除",
                        tint = AppColors.error
                    )
                }
            }
        } else {
            Text(
                "纪念日",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimaryLight
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TopBarButton(icon = Icons.Filled.Search, onClick = onSearchToggle)
                TopBarButton(icon = Icons.Filled.GridView, onClick = onTemplates)
                TopBarButton(icon = Icons.Filled.CheckCircle, onClick = onEnterSelect)
            }
        }
    }
}

@Composable
private fun TopBarButton(icon: ImageVector, onClick: () -> Unit) {
    val settingsVM = remember { SettingsViewModel.getInstance() }

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(AppColors.secondaryBgLight)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = AppColors.textPrimaryLight
        )
    }
}

// MARK: - 搜索栏

@Composable
private fun SearchBar(searchText: String, onSearchChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.secondaryBgLight)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Search, null, tint = AppColors.textSecondaryLight)
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = searchText,
            onValueChange = onSearchChange,
            placeholder = { Text("搜索名称、备注或标签...", fontSize = 16.sp) },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        if (searchText.isNotEmpty()) {
            IconButton(onClick = { onSearchChange("") }) {
                Icon(Icons.Filled.Close, null, tint = AppColors.textTertiaryLight)
            }
        }
    }
}

// MARK: - 筛选栏

@Composable
private fun FilterBar(
    categories: List<com.memorialday.app.models.Category>,
    selectedCategoryID: java.util.UUID?,
    onCategorySelected: (java.util.UUID?) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = AppSpacing.lg.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                label = "全部",
                icon = Icons.Filled.GridView,
                isSelected = selectedCategoryID == null,
                onClick = { onCategorySelected(null) }
            )
        }
        items(categories) { category ->
            FilterChip(
                label = category.name,
                icon = null,
                isSelected = selectedCategoryID == category.id,
                onClick = { onCategorySelected(category.id) }
            )
        }
    }
}

// MARK: - 空状态

@Composable
private fun EmptyState(onTemplates: () -> Unit, onCreateNew: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = AppColors.primaryLight
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("还没有纪念日", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimaryLight)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "点击下方按钮或使用模板\n记录你生命中的重要日子",
            fontSize = 16.sp,
            color = AppColors.textSecondaryLight,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onTemplates,
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Filled.GridView, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("从模板创建", color = AppColors.primary)
        }
    }
}

// MARK: - 快速统计

@Composable
private fun QuickStatsBar(days: List<MemorialDay>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatBadge(title = "全部", count = days.size, color = AppColors.primary, modifier = Modifier.weight(1f))
        StatBadge(title = "倒数", count = days.count { it.dayType.name == "COUNTDOWN" }, color = AppColors.info, modifier = Modifier.weight(1f))
        StatBadge(title = "正数", count = days.count { it.dayType.name == "COUNTUP" }, color = AppColors.success, modifier = Modifier.weight(1f))
    }
}

// MARK: - 即将到期

@Composable
private fun UpcomingSection(upcomingDays: List<MemorialDay>) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Notifications, null, modifier = Modifier.size(14.dp), tint = AppColors.warning)
            Spacer(modifier = Modifier.width(6.dp))
            Text("即将到期", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimaryLight)
            Spacer(modifier = Modifier.weight(1f))
            Text("共 ${upcomingDays.size} 项", fontSize = 13.sp, color = AppColors.textSecondaryLight)
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = AppSpacing.lg.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(upcomingDays) { day ->
                UpcomingMiniCard(day = day)
            }
        }
    }
}

// MARK: - 新建按钮

@Composable
private fun NewDayButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().padding(horizontal = 40.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(listOf(AppColors.accent, AppColors.primaryLight)),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 28.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.AddCircle, null, modifier = Modifier.size(18.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("新建纪念日", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }
    }
}

// MARK: - 浮动按钮

@Composable
private fun FloatingButtons(
    onTemplates: () -> Unit,
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 模板按钮
        FloatingActionButton(
            onClick = onTemplates,
            containerColor = AppColors.primary,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Filled.GridView, null, modifier = Modifier.size(18.dp), tint = Color.White)
        }

        Spacer(modifier = Modifier.weight(1f))

        // 新建按钮
        FloatingActionButton(
            onClick = onCreateNew,
            modifier = Modifier.size(56.dp),
            containerColor = Color.Transparent,
            contentColor = Color.White
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(listOf(AppColors.accent, AppColors.primaryLight))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(22.dp), tint = Color.White)
            }
        }
    }
}
