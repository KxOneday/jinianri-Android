// 纪念日 - 设置视图
// 对应 iOS: SettingsView.swift

package com.memorialday.app.ui.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorialday.app.services.NotificationService
import com.memorialday.app.ui.theme.AppColors
import com.memorialday.app.ui.theme.AppRadius
import com.memorialday.app.utils.AppConfig
import com.memorialday.app.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val settingsVM = remember { SettingsViewModel.getInstance() }
    val requirePassword by settingsVM.requirePassword.collectAsState()
    val notificationsEnabled by settingsVM.notificationsEnabled.collectAsState()

    var showPasswordSetup by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.backgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 隐私安全
        SectionCard("隐私安全") {
            // 应用锁
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("应用锁", fontSize = 15.sp, color = AppColors.textPrimaryLight)
                    Text("打开应用时需验证身份", fontSize = 11.sp, color = AppColors.textSecondaryLight)
                }
                Switch(
                    checked = requirePassword,
                    onCheckedChange = {
                        if (it && settingsVM.appPassword.collectAsState().value.isEmpty()) {
                            showPasswordSetup = true
                        }
                        settingsVM.setRequirePassword(it)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = AppColors.accent)
                )
            }

            if (requirePassword) {
                TextButton(onClick = { showPasswordSetup = true }) {
                    Text("修改密码", color = AppColors.primary, fontSize = 14.sp)
                    Spacer()
                    Icon(Icons.Filled.ChevronRight, null, modifier = Modifier.size(12.dp), tint = AppColors.textTertiaryLight)
                }
            }
        }

        // 通知设置
        SectionCard("通知设置") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("允许通知", fontSize = 15.sp, color = AppColors.textPrimaryLight)
                    Text("关闭后将收不到任何提醒", fontSize = 11.sp, color = AppColors.textSecondaryLight)
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { settingsVM.setNotificationsEnabled(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = AppColors.accent)
                )
            }

            TextButton(onClick = { /* 打开系统通知设置 */ }) {
                Text("系统通知设置", color = AppColors.primary, fontSize = 14.sp)
                Spacer()
                Icon(Icons.Filled.OpenInNew, null, modifier = Modifier.size(12.dp), tint = AppColors.textTertiaryLight)
            }

            TextButton(onClick = { /* 发送测试通知 */ }) {
                Icon(Icons.Filled.Notifications, null, modifier = Modifier.size(14.dp), tint = AppColors.warning)
                Spacer(modifier = Modifier.width(10.dp))
                Text("发送测试通知", color = AppColors.warning, fontSize = 14.sp)
            }
        }

        // 关于
        SectionCard("关于") {
            InfoRow("应用名称", AppConfig.APP_NAME)
            InfoRow("版本", "v${AppConfig.APP_VERSION} (Build ${AppConfig.BUILD_NUMBER})")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("数据安全", fontSize = 14.sp, color = AppColors.textSecondaryLight)
                Spacer()
                Icon(Icons.Filled.Lock, null, modifier = Modifier.size(10.dp), tint = AppColors.success)
                Spacer(modifier = Modifier.width(4.dp))
                Text("AES-256 本地加密", fontSize = 12.sp, color = AppColors.success)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("作者", fontSize = 14.sp, color = AppColors.textSecondaryLight)
                Spacer()
                Text("Felix.", fontSize = 14.sp, color = AppColors.primary)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // 密码设置弹窗
    if (showPasswordSetup) {
        AlertDialog(
            onDismissRequest = {
                showPasswordSetup = false
                if (settingsVM.appPassword.collectAsState().value.isEmpty()) {
                    settingsVM.setRequirePassword(false)
                }
            },
            title = { Text("设置密码") },
            text = {
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) passwordInput = it },
                    placeholder = { Text("输入4位数字密码") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (passwordInput.length >= 4) {
                        settingsVM.setPassword(passwordInput)
                        passwordInput = ""
                        showPasswordSetup = false
                    }
                }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = {
                    settingsVM.setRequirePassword(false)
                    passwordInput = ""
                    showPasswordSetup = false
                }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.md.dp),
        color = AppColors.cardLight
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.textPrimaryLight)
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = AppColors.textSecondaryLight)
        Spacer()
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.textPrimaryLight)
    }
}
