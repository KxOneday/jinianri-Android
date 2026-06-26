// 纪念日 - 锁定屏幕
// 对应 iOS: ContentView.swift LockScreenView

package com.memorialday.app.ui.lockscreen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorialday.app.ui.theme.AppColors
import com.memorialday.app.viewmodels.SettingsViewModel
import kotlinx.coroutines.delay

@Composable
fun LockScreen(
    isUnlocked: Boolean,
    onUnlocked: () -> Unit
) {
    val settingsVM = remember { SettingsViewModel.getInstance() }
    var pinCode by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var shakeOffset by remember { mutableStateOf(0f) }

    val pinLength = 4

    // 震动动画
    LaunchedEffect(showError) {
        if (showError) {
            // 模拟 iOS 的抖动序列
            val animSpec = spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
            // 简单抖动效果
            shakeOffset = 20f; delay(50)
            shakeOffset = -15f; delay(50)
            shakeOffset = 10f; delay(50)
            shakeOffset = -5f; delay(50)
            shakeOffset = 0f
            delay(500)
            showError = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.backgroundLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.3f))

            // 图标
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = AppColors.primaryLight
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                "纪念日已锁定",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimaryLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "输入密码解锁",
                fontSize = 15.sp,
                color = AppColors.textSecondaryLight
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 密码输入点
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.offset(x = shakeOffset.dp)
            ) {
                for (i in 0 until pinLength) {
                    val filled = i < pinCode.length
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (showError) AppColors.error
                                else if (filled) AppColors.accent
                                else Color.Transparent
                            )
                            .then(
                                if (!filled || showError)
                                    Modifier.background(Color.Transparent, CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        showError -> AppColors.error
                                        filled -> AppColors.accent
                                        else -> Color.Transparent
                                    }
                                )
                        )
                    }
                    // 外圈
                    Surface(
                        modifier = Modifier.size(20.dp),
                        shape = CircleShape,
                        border = ButtonDefaults.outlinedButtonBorder,
                        color = Color.Transparent
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 数字键盘
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {
                items(9) { index ->
                    NumberButton("${index + 1}") { appendPin(index + 1, pinCode, pinLength) { pinCode = it } }
                }
                item { Spacer(modifier = Modifier.size(64.dp)) }
                item { NumberButton("0") { appendPin(0, pinCode, pinLength) { pinCode = it } } }
                item {
                    NumberButton("⌫") {
                        if (pinCode.isNotEmpty()) pinCode = pinCode.dropLast(1)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))
        }
    }

    // 验证
    LaunchedEffect(pinCode) {
        if (pinCode.length == pinLength) {
            if (settingsVM.validatePassword(pinCode)) {
                onUnlocked()
                pinCode = ""
            } else {
                showError = true
                pinCode = ""
            }
        }
    }
}

private fun appendPin(
    number: Int,
    current: String,
    maxLength: Int,
    setter: (String) -> Unit
) {
    if (current.length < maxLength) {
        setter(current + number.toString())
    }
}

@Composable
private fun NumberButton(number: String, onClick: () -> Unit) {
    val settingsVM = remember { SettingsViewModel.getInstance() }
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .shadow(0.dp, CircleShape),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.secondaryBgLight,
            contentColor = AppColors.textPrimaryLight
        )
    ) {
        Text(
            number,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
