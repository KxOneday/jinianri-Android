// 纪念日 - 主题与样式管理器
// 对应 iOS: ThemeManager.swift + AppColors / AppSpacing / AppRadius

package com.memorialday.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.memorialday.app.utils.fromHex

/** 颜色系统 - 完全对照 iOS AppColors */
object AppColors {
    // 主色
    val primary = Color.fromHex("#8B9DC3")
    val primaryLight = Color.fromHex("#B8C5D6")
    val accent = Color.fromHex("#F0A8A8")
    val accentLight = Color.fromHex("#F5C8C8")

    // 背景色
    val backgroundLight = Color.fromHex("#FAF8F5")
    val backgroundDark = Color.fromHex("#1C1C1E")
    val cardLight = Color.fromHex("#FFFFFF")
    val cardDark = Color.fromHex("#2C2C2E")
    val secondaryBgLight = Color.fromHex("#F2EFEC")
    val secondaryBgDark = Color.fromHex("#3A3A3C")

    // 文字色
    val textPrimaryLight = Color.fromHex("#2C2C2C")
    val textPrimaryDark = Color.fromHex("#F5F5F5")
    val textSecondaryLight = Color.fromHex("#8E8E93")
    val textSecondaryDark = Color.fromHex("#98989D")
    val textTertiaryLight = Color.fromHex("#C7C7CC")
    val textTertiaryDark = Color.fromHex("#636366")

    // 功能色
    val success = Color.fromHex("#7BC8A4")
    val warning = Color.fromHex("#F0C27A")
    val error = Color.fromHex("#E88D8D")
    val info = Color.fromHex("#8BB8E8")
}

/** 尺寸系统 */
object AppSpacing {
    val xs = 4
    val sm = 8
    val md = 14
    val lg = 20
    val xl = 28
    val xxl = 40
}

/** 圆角系统 */
object AppRadius {
    val sm = 8
    val md = 14
    val lg = 20
    val xl = 28
}
