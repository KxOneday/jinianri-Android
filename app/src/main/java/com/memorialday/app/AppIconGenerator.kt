package com.memorialday.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.drawable.toBitmap

object AppIconGenerator {
    fun generateIcon(context: Context, size: Int = 192): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 背景
        paint.color = 0xFFF5F0EB.toInt()
        val bgRect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawRoundRect(bgRect, size * 0.2f, size * 0.2f, paint)

        // 心形
        paint.color = 0xFFE88D8D.toInt()
        val cx = size / 2f
        val cy = size * 0.4f
        val r = size * 0.18f
        canvas.drawCircle(cx - r * 0.4f, cy, r, paint)
        canvas.drawCircle(cx + r * 0.4f, cy, r, paint)

        val path = android.graphics.Path().apply {
            moveTo(cx - r * 0.85f, cy + r * 0.2f)
            cubicTo(cx - r * 0.85f, cy + r * 0.5f, cx, cy + r * 0.9f, cx, cy + r * 1.1f)
            cubicTo(cx, cy + r * 0.9f, cx + r * 0.85f, cy + r * 0.5f, cx + r * 0.85f, cy + r * 0.2f)
            close()
        }
        canvas.drawPath(path, paint)

        // 底部文字
        paint.color = 0xFF8B9DC3.toInt()
        paint.textSize = size * 0.12f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("纪念日", size / 2f, size * 0.8f, paint)

        return bitmap
    }
}
