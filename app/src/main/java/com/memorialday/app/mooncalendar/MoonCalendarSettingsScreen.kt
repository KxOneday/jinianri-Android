// 绵绵月历 - 设置页面
package com.memorialday.app.mooncalendar

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

private val Pink = Color(0xFFFF6B9D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoonCalendarSettingsScreen(onBack: () -> Unit) {
    var settings by remember { mutableStateOf(MoonCalendarStorage.getSettings()) }
    val fmt = remember { SimpleDateFormat("M月d日", Locale.CHINESE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("月历设置", fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAF8F5))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFFAF8F5))
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            SettingsRow("经期周期", "${settings.cycleDays} 天", onClick = { })
            HorizontalDivider()
            SettingsRow("经期天数", "${settings.periodDays} 天", onClick = { })
            HorizontalDivider()
            SettingsRow("上次经期开始", fmt.format(settings.lastPeriodStart), onClick = { })
            HorizontalDivider()

            SettingsRow("经期来临提醒", trailing = {
                Switch(checked = settings.periodReminder, onCheckedChange = {
                    settings = settings.copy(periodReminder = it)
                    MoonCalendarStorage.saveSettings(settings)
                }, colors = SwitchDefaults.colors(checkedThumbColor = Pink, checkedTrackColor = Pink.copy(alpha = 0.3f)))
            })
            if (settings.periodReminder) {
                Text("提前 ${settings.periodReminderDays} 天提醒", fontSize = 11.sp, color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
            }
            HorizontalDivider()

            SettingsRow("每日记录提醒", trailing = {
                Switch(checked = settings.dailyReminder, onCheckedChange = {
                    settings = settings.copy(dailyReminder = it)
                    MoonCalendarStorage.saveSettings(settings)
                }, colors = SwitchDefaults.colors(checkedThumbColor = Pink, checkedTrackColor = Pink.copy(alpha = 0.3f)))
            })
            if (settings.dailyReminder) {
                Text("每天 ${settings.dailyReminderHour}:00 提醒", fontSize = 11.sp, color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
            }
            HorizontalDivider()

            SettingsRow("备孕模式", trailing = {
                Switch(checked = settings.pregnancyMode, onCheckedChange = {
                    settings = settings.copy(pregnancyMode = it)
                    MoonCalendarStorage.saveSettings(settings)
                }, colors = SwitchDefaults.colors(checkedThumbColor = Pink, checkedTrackColor = Pink.copy(alpha = 0.3f)))
            })
            HorizontalDivider()

            SettingsRow("隐私密码锁", trailing = {
                Switch(checked = settings.privacyLock, onCheckedChange = {
                    settings = settings.copy(privacyLock = it)
                    MoonCalendarStorage.saveSettings(settings)
                }, colors = SwitchDefaults.colors(checkedThumbColor = Pink, checkedTrackColor = Pink.copy(alpha = 0.3f)))
            })
            HorizontalDivider()

            SettingsRow("导出数据", onClick = { })
            HorizontalDivider()
            SettingsRow("导入数据", onClick = { })
            HorizontalDivider()

            Spacer(Modifier.height(20.dp))
            Text("清空数据", color = Color.Red, fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 12.dp).align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String? = null, trailing: @Composable (() -> Unit)? = null, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, color = Color(0xFF2C2C2C))
            subtitle?.let { Text(it, fontSize = 13.sp, color = Color.Gray) }
        }
        if (trailing != null) {
            trailing()
        } else {
            Text(subtitle ?: "", fontSize = 14.sp, color = Color.Gray)
            Icon(Icons.Filled.ChevronRight, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        }
    }
}

@Composable
private fun HorizontalDivider() {
    Divider(color = Color(0xFFE8E5E0), thickness = 0.5.dp)
}
