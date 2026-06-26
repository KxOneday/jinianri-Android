// 纪念日 - 应用全局设置模型
// 对应 iOS: AppSettings.swift

package com.memorialday.app.models

/** 主题模式 */
enum class ThemeMode(val rawValue: String) {
    LIGHT("浅色");
}

/** 全局字体设置 */
data class FontSetting(
    val fontName: String? = null,
    val displayName: String = "系统字体",
    val isSystem: Boolean = true
) {
    companion object {
        val SYSTEM = FontSetting(fontName = null, displayName = "系统字体", isSystem = true)
        val builtInFonts: List<FontSetting> = listOf(SYSTEM)
    }
}

/** 应用全局设置 */
data class AppSettings(
    var themeMode: ThemeMode = ThemeMode.LIGHT,
    var enableNumberAnimation: Boolean = true,
    var widgetRefreshInterval: Int = 15,
    var selectedFont: FontSetting = FontSetting.SYSTEM,
    var requirePassword: Boolean = false,
    var appPassword: String = "",
    var notificationsEnabled: Boolean = true
)
