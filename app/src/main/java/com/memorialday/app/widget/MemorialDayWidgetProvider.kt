// 纪念日 - 桌面小组件
// 对应 iOS: MemorialDayWidget.swift

package com.memorialday.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.memorialday.app.R
import com.memorialday.app.models.MemorialDay
import com.memorialday.app.services.StorageService

/** 纪念日桌面小组件 Provider */
class MemorialDayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val days = StorageService.allDays
        val upcomingDays = days.filter { !it.isArchived }.sortedBy { it.daysUntilNextOccurrence }

        // 构建小组件视图
        val views = RemoteViews(context.packageName, R.layout.widget_memorial_day)

        if (upcomingDays.isNotEmpty()) {
            val day = upcomingDays.first()
            views.setTextViewText(R.id.widget_title, day.title)
            views.setTextViewText(R.id.widget_days, "${day.displayDayCount}")
            views.setTextViewText(R.id.widget_subtitle, day.subtitle)
        } else {
            views.setTextViewText(R.id.widget_title, "纪念日")
            views.setTextViewText(R.id.widget_days, "--")
            views.setTextViewText(R.id.widget_subtitle, "点击添加纪念日")
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onEnabled(context: Context) {
        // 小组件首次添加
    }

    override fun onDisabled(context: Context) {
        // 最后一个小组件被移除
    }
}
