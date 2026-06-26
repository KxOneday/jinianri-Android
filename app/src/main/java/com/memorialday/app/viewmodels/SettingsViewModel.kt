// 纪念日 - 设置视图模型
// 对应 iOS: SettingsViewModel.swift

package com.memorialday.app.viewmodels

import androidx.lifecycle.ViewModel
import com.memorialday.app.models.AppSettings
import com.memorialday.app.models.ThemeMode
import com.memorialday.app.services.NotificationService
import com.memorialday.app.services.StorageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {

    companion object {
        @Volatile
        private var INSTANCE: SettingsViewModel? = null

        fun getInstance(): SettingsViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsViewModel().also { INSTANCE = it }
            }
        }
    }

    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _enableNumberAnimation = MutableStateFlow(true)
    val enableNumberAnimation: StateFlow<Boolean> = _enableNumberAnimation.asStateFlow()

    private val _widgetRefreshInterval = MutableStateFlow(15)
    val widgetRefreshInterval: StateFlow<Int> = _widgetRefreshInterval.asStateFlow()

    private val _selectedFontName = MutableStateFlow<String?>(null)
    val selectedFontName: StateFlow<String?> = _selectedFontName.asStateFlow()

    private val _selectedFontDisplayName = MutableStateFlow("系统字体")
    val selectedFontDisplayName: StateFlow<String> = _selectedFontDisplayName.asStateFlow()

    private val _requirePassword = MutableStateFlow(false)
    val requirePassword: StateFlow<Boolean> = _requirePassword.asStateFlow()

    private val _appPassword = MutableStateFlow("")
    val appPassword: StateFlow<String> = _appPassword.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    // 浅色模式常量
    val isDarkMode: Boolean get() = false

    private val storage = StorageService

    init {
        loadSettings()
    }

    fun loadSettings() {
        val s = storage.settings
        _themeMode.value = s.themeMode
        _enableNumberAnimation.value = s.enableNumberAnimation
        _widgetRefreshInterval.value = s.widgetRefreshInterval
        _selectedFontName.value = s.selectedFont.fontName
        _selectedFontDisplayName.value = s.selectedFont.displayName
        _requirePassword.value = s.requirePassword
        _appPassword.value = s.appPassword
        _notificationsEnabled.value = s.notificationsEnabled
    }

    fun saveSettings() {
        val s = storage.settings
        storage.settings = s.copy(
            themeMode = _themeMode.value,
            enableNumberAnimation = _enableNumberAnimation.value,
            widgetRefreshInterval = _widgetRefreshInterval.value,
            selectedFont = com.memorialday.app.models.FontSetting(
                fontName = _selectedFontName.value,
                displayName = _selectedFontDisplayName.value,
                isSystem = _selectedFontName.value == null
            ),
            requirePassword = _requirePassword.value,
            appPassword = _appPassword.value,
            notificationsEnabled = _notificationsEnabled.value
        )
        storage.saveSettings()
    }

    fun setTheme(mode: ThemeMode) {
        _themeMode.value = mode
        saveSettings()
    }

    fun setPassword(password: String) {
        _appPassword.value = password
        _requirePassword.value = password.isNotEmpty()
        saveSettings()
    }

    fun validatePassword(input: String): Boolean {
        if (!_requirePassword.value) return true
        return input == _appPassword.value
    }

    fun setRequirePassword(required: Boolean) {
        _requirePassword.value = required
        if (!required) {
            _appPassword.value = ""
        }
        saveSettings()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        saveSettings()
        if (!enabled) {
            NotificationService.cancelAllNotifications(com.memorialday.app.MemorialDayApp.instance)
        } else {
            NotificationService.refreshAllNotifications(com.memorialday.app.MemorialDayApp.instance)
        }
    }
}
