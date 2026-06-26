// 纪念日 - 本地通知服务
// 对应 iOS: NotificationService.swift

package com.memorialday.app.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.memorialday.app.MemorialDayApp
import com.memorialday.app.R
import com.memorialday.app.models.MemorialDay
import com.memorialday.app.models.ReminderSettings
import java.util.*

object NotificationService {

    const val CHANNEL_ID = "memorial_reminders"
    private const val NOTIFICATION_ID_PREFIX = 1000

    /** 请求通知权限 */
    fun requestPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 权限由 Activity 层请求
            true
        } else {
            true
        }
    }

    /** 为一条事件调度提醒 */
    fun scheduleNotification(day: MemorialDay, context: Context) {
        if (!day.reminderSettings.isEnabled) {
            cancelNotifications(day.id, context)
            return
        }

        if (day.isYearlyRepeat) {
            scheduleYearlyReminder(day, day.reminderSettings, context)
        } else {
            scheduleSingleReminder(day, day.reminderSettings, context)
        }
    }

    /** 单次提醒 */
    private fun scheduleSingleReminder(day: MemorialDay, settings: ReminderSettings, context: Context) {
        val fireDate = settings.fireDate(day.targetDate, false) ?: return
        if (fireDate.before(Date())) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", day.title)
            putExtra("body", settings.customNote.ifEmpty { day.title })
            putExtra("dayId", day.id.toString())
            putExtra("notificationId", day.id.hashCode() and 0x7FFFFFFF)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, day.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, fireDate.time, pendingIntent)
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, fireDate.time, pendingIntent)
            }
        } catch (_: Exception) {}
    }

    /** 每年循环提醒 */
    private fun scheduleYearlyReminder(day: MemorialDay, settings: ReminderSettings, context: Context) {
        val calendar = Calendar.getInstance()
        val today = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }

        val targetCal = Calendar.getInstance().apply { time = day.targetDate }
        val month = targetCal.get(Calendar.MONTH)
        val dayNum = targetCal.get(Calendar.DAY_OF_MONTH)
        val thisYear = today.get(Calendar.YEAR)

        var nextBirthday = Calendar.getInstance().apply {
            set(thisYear, month, dayNum, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (nextBirthday.before(today)) {
            nextBirthday.set(Calendar.YEAR, thisYear + 1)
        }

        nextBirthday.add(Calendar.DAY_OF_MONTH, -settings.advanceDays)
        nextBirthday.set(Calendar.HOUR_OF_DAY, settings.reminderHour)
        nextBirthday.set(Calendar.MINUTE, settings.reminderMinute)

        val fireYear = nextBirthday.get(Calendar.YEAR)
        if (fireYear > 2999) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", "纪念日")
            putExtra("body", settings.customNote.ifEmpty { day.title })
            putExtra("dayId", day.id.toString())
            putExtra("notificationId", day.id.hashCode() and 0x7FFFFFFF)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, "yearly_${day.id}".hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // 设置每年重复的闹钟
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextBirthday.timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextBirthday.timeInMillis, pendingIntent)
            }
        } catch (_: Exception) {}
    }

    /** 取消通知 */
    fun cancelNotifications(dayId: UUID, context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, dayId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        val pendingIntentYearly = PendingIntent.getBroadcast(
            context, "yearly_${dayId}".hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntentYearly)
    }

    fun cancelAllNotifications(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // 取消所有 - 通过重新调度完成
        // 简化实现：由外部调用 refreshAllNotifications
    }

    /** 刷新所有通知 */
    fun refreshAllNotifications(context: Context) {
        cancelAllNotifications(context)
        for (day in StorageService.allDays) {
            scheduleNotification(day, context)
        }
    }
}

/** 通知接收器 */
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "纪念日"
        val body = intent.getStringExtra("body") ?: ""
        val notificationId = intent.getIntExtra("notificationId", 1)

        val notification = NotificationCompat.Builder(context, NotificationService.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: Exception) {}
    }
}

/** 开机启动接收器 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationService.refreshAllNotifications(context)
        }
    }
}
