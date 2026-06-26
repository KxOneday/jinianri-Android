// 纪念日 - 主 Activity
// 对应 iOS: ContentView.swift + MemorialDayApp.swift (WindowGroup)

package com.memorialday.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorialday.app.ui.lockscreen.LockScreen
import com.memorialday.app.ui.theme.AppColors
import com.memorialday.app.ui.MainScreen
import com.memorialday.app.viewmodels.SettingsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsVM: SettingsViewModel = viewModel()
            val requirePassword by settingsVM.requirePassword.collectAsState()
            var isUnlocked by remember { mutableStateOf(!requirePassword) }
            var showLockScreen by remember { mutableStateOf(requirePassword) }

            // 对应 iOS: WindowGroup → ContentView
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = AppColors.backgroundLight
            ) {
                if (showLockScreen) {
                    LockScreen(
                        isUnlocked = isUnlocked,
                        onUnlocked = {
                            isUnlocked = true
                            showLockScreen = false
                        }
                    )
                } else {
                    MainScreen(
                        onLockRequested = {
                            if (requirePassword) {
                                isUnlocked = false
                                showLockScreen = true
                            }
                        }
                    )
                }
            }

            // 对应 iOS: scenePhase 监听（后台切回时锁定）
            DisposableEffect(Unit) {
                onDispose { }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // 对应 iOS: scenePhase = .background → 锁屏
        // 由 MainScreen 处理锁屏逻辑
    }
}
