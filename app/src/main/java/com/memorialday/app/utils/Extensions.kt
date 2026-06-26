// 纪念日 - 扩展工具
// 对应 iOS: ThemeManager.swift Color(hex:) 扩展

package com.memorialday.app.utils

import androidx.compose.ui.graphics.Color

fun Color.Companion.fromHex(hex: String): Color {
    var hexSanitized = hex.trimStart('#')
    var int: Long = 0
    try {
        int = hexSanitized.toLong(16)
    } catch (_: NumberFormatException) {
        return Color.Black
    }

    return when (hexSanitized.length) {
        3 -> {
            val r = ((int shr 8) and 0xF) * 17
            val g = ((int shr 4) and 0xF) * 17
            val b = (int and 0xF) * 17
            Color(r / 255f, g / 255f, b / 255f)
        }
        6 -> {
            val r = (int shr 16) and 0xFF
            val g = (int shr 8) and 0xFF
            val b = int and 0xFF
            Color(r / 255f, g / 255f, b / 255f)
        }
        8 -> {
            val a = (int shr 24) and 0xFF
            val r = (int shr 16) and 0xFF
            val g = (int shr 8) and 0xFF
            val b = int and 0xFF
            Color(r / 255f, g / 255f, b / 255f, a / 255f)
        }
        else -> Color.Black
    }
}

fun Color.toHex(): String {
    val r = (this.red * 255).toInt()
    val g = (this.green * 255).toInt()
    val b = (this.blue * 255).toInt()
    return String.format("#%02X%02X%02X", r, g, b)
}
