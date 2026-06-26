// 纪念日 - 海报生成服务
// 对应 iOS: ImageService.swift

package com.memorialday.app.services

import android.graphics.*
import android.graphics.Paint.Align
import com.memorialday.app.models.MemorialDay
import com.memorialday.app.models.DayType

/** 海报生成服务 */
object ImageService {

    /** 生成纪念日海报卡片图片 */
    fun generatePoster(day: MemorialDay, width: Int = 1080, height: Int = 1920): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 背景
        val bgColor = parseHexColor(day.backgroundColorHex)
        if (day.backgroundEndColorHex != null && day.showGradient) {
            val endColor = parseHexColor(day.backgroundEndColorHex!!)
            val gradient = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                bgColor, endColor, Shader.TileMode.CLAMP
            )
            val paint = Paint().apply { shader = gradient }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        } else {
            canvas.drawColor(bgColor)
        }

        // 半透明遮罩
        canvas.drawColor(Color.argb(38, 255, 255, 255))

        val textColor = parseHexColor(day.textColorHex)

        // 绘制天数
        val dayText = "${day.displayDayCount}"
        val dayPaint = Paint().apply {
            color = textColor
            textSize = 160f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Align.CENTER
        }
        canvas.drawText(dayText, width / 2f, height * 0.18f + 160f, dayPaint)

        // 绘制单位
        val unitText = if (day.dayType == DayType.COUNTDOWN) "天 后" else "天 前"
        val unitPaint = Paint().apply {
            color = textColor
            alpha = 204  // 0.8
            textSize = 32f
            isAntiAlias = true
            textAlign = Align.CENTER
        }
        canvas.drawText(unitText, width / 2f, height * 0.18f + 160f + 60f, unitPaint)

        // 绘制标题
        val titlePaint = Paint().apply {
            color = textColor
            alpha = 230  // 0.9
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Align.CENTER
        }
        canvas.drawText(day.title, width / 2f, height * 0.18f + 160f + 60f + 100f, titlePaint)

        // 绘制日期
        val datePaint = Paint().apply {
            color = textColor
            alpha = 179  // 0.7
            textSize = 24f
            isAntiAlias = true
            textAlign = Align.CENTER
        }
        canvas.drawText(day.subtitle, width / 2f, height * 0.18f + 160f + 60f + 100f + 60f, datePaint)

        // 底部水印
        val watermarkPaint = Paint().apply {
            color = textColor
            alpha = 77  // 0.3
            textSize = 18f
            isAntiAlias = true
            textAlign = Align.CENTER
        }
        canvas.drawText("纪念日 · 记录每个重要时刻", width / 2f, height - 80f, watermarkPaint)

        return bitmap
    }

    /** 解析十六进制颜色字符串为 Color Int */
    private fun parseHexColor(hex: String): Int {
        var h = hex.trimStart('#')
        if (h.length == 6) h = "FF$h"
        return try {
            h.toLong(16).toInt()
        } catch (_: Exception) {
            Color.WHITE
        }
    }
}
