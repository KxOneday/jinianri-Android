// 纪念日 - 主界面（TabView）
// 对应 iOS: ContentView.swift

package com.memorialday.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorialday.app.ui.home.HomeScreen
import com.memorialday.app.ui.calculator.DateCalculatorScreen
import com.memorialday.app.ui.settings.SettingsScreen
import com.memorialday.app.ui.theme.AppColors

data class TabItem(val title: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLockRequested: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        TabItem("首页", Icons.Filled.Home),
        TabItem("时间计算器", Icons.Filled.CalendarMonth),
        TabItem("设置", Icons.Filled.Settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = AppColors.cardLight,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppColors.accent,
                            selectedTextColor = AppColors.accent,
                            unselectedIconColor = AppColors.textSecondaryLight,
                            unselectedTextColor = AppColors.textSecondaryLight,
                            indicatorColor = AppColors.accent.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> HomeScreen(modifier = Modifier.padding(paddingValues))
            1 -> DateCalculatorScreen(modifier = Modifier.padding(paddingValues))
            2 -> SettingsScreen(modifier = Modifier.padding(paddingValues))
        }
    }
}
