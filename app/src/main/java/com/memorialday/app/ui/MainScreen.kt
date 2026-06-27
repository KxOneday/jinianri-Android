// 纪念日 - 主界面（TabView）
// 对应 iOS: ContentView.swift

package com.memorialday.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorialday.app.mooncalendar.MoonCalendarScreen
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
        TabItem("时间计算器", Icons.Filled.History),
        TabItem("月历", Icons.Filled.FavoriteBorder),
        TabItem("设置", Icons.Filled.Settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = AppColors.cardLight,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isMoonCalendar = index == 2
                    val selectedColor = if (isMoonCalendar) Color(0xFFFF6B9D) else Color(0xFF4A90D9)
                    
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            unselectedIconColor = AppColors.textSecondaryLight,
                            unselectedTextColor = AppColors.textSecondaryLight,
                            indicatorColor = selectedColor.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> HomeScreen(modifier = Modifier.padding(paddingValues))
            1 -> DateCalculatorScreen(modifier = Modifier.padding(paddingValues))
            2 -> MoonCalendarScreen(modifier = Modifier.padding(paddingValues))
            3 -> SettingsScreen(modifier = Modifier.padding(paddingValues))
        }
    }
}
