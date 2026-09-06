package com.example.myapplication158.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.CalendarContract
import com.example.myapplication158.data.WorkOrder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WorkOrderReminderManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminders(workOrder: WorkOrder) {
        cancelReminders(workOrder.id)

        if (workOrder.isMuted || workOrder.status == WorkOrder.STATUS_COMPLETED || workOrder.status == WorkOrder.STATUS_CANCELED) {
            return
        }

        val jobCalendar = parseDateTime(workOrder.targetDate, workOrder.targetTime) ?: return
        val now = System.currentTimeMillis()

        // 1. Day Before Reminder (at 19:00 / 7 PM)
        val dayBeforeCal = (jobCalendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (dayBeforeCal.timeInMillis > now) {
            val title = "תזכורת עבודה למחר 📅"
            val msg = "עבודה מתוכננת למחר (${workOrder.targetDate}): ${workOrder.jobDescription} | מיקום: ${workOrder.location}"
            setAlarm(workOrder.id * 10 + 1, dayBeforeCal.timeInMillis, workOrder.id, title, msg)
        }

        // 2. 5 Hours Before Reminder
        val fiveHoursBeforeCal = (jobCalendar.clone() as Calendar).apply {
            add(Calendar.HOUR_OF_DAY, -5)
        }

        if (fiveHoursBeforeCal.timeInMillis > now) {
            val title = "תזכורת עבודה בעוד 5 שעות ⏰"
            val msg = "מועד הגעה: ${workOrder.targetTime} | ${workOrder.jobDescription} | נייד: ${workOrder.clientPhone}"
            setAlarm(workOrder.id * 10 + 2, fiveHoursBeforeCal.timeInMillis, workOrder.id, title, msg)
        }
    }

    fun cancelReminders(workOrderId: Int) {
        val intent1 = Intent(context, WorkOrderReminderReceiver::class.java)
        val pendingIntent1 = PendingIntent.getBroadcast(
            context,
            workOrderId * 10 + 1,
            intent1,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent1?.let { alarmManager.cancel(it) }

        val intent2 = Intent(context, WorkOrderReminderReceiver::class.java)
        val pendingIntent2 = PendingIntent.getBroadcast(
            context,
            workOrderId * 10 + 2,
            intent2,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent2?.let { alarmManager.cancel(it) }
    }

    private fun setAlarm(requestCode: Int, timeInMillis: Long, workOrderId: Int, title: String, message: String) {
        val intent = Intent(context, WorkOrderReminderReceiver::class.java).apply {
            putExtra("WORK_ORDER_ID", workOrderId)
            putExtra("WORK_ORDER_TITLE", title)
            putExtra("WORK_ORDER_MESSAGE", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } catch (_: Exception) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }

    fun addToNativeCalendar(workOrder: WorkOrder) {
        val cal = parseDateTime(workOrder.targetDate, workOrder.targetTime) ?: Calendar.getInstance()
        val startMillis = cal.timeInMillis
        val endMillis = startMillis + (60 * 60 * 1000)

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.Events.TITLE, "עבודת גז: ${workOrder.jobDescription}")
            putExtra(CalendarContract.Events.EVENT_LOCATION, workOrder.location)
            putExtra(CalendarContract.Events.DESCRIPTION, "טלפון לקוח: ${workOrder.clientPhone}\nמחיר: ${workOrder.quotedPrice} ₪\nמהות עבודה: ${workOrder.jobDescription}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }

    private fun parseDateTime(dateStr: String, timeStr: String): Calendar? {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fullStr = "$dateStr ${timeStr.ifEmpty { "09:00" }}"
            val date: Date = sdf.parse(fullStr) ?: return null
            Calendar.getInstance().apply { time = date }
        } catch (e: Exception) {
            try {
                val sdf2 = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val fullStr2 = "$dateStr ${timeStr.ifEmpty { "09:00" }}"
                val date2: Date = sdf2.parse(fullStr2) ?: return null
                Calendar.getInstance().apply { time = date2 }
            } catch (e2: Exception) {
                null
            }
        }
    }
}
