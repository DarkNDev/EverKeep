package com.darkndev.everkeep.features.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.darkndev.everkeep.models.Note

class ReminderScheduler(
    private val context: Context
) : ReminderListener {

    companion object {
        const val NOTE_ID = "com.darkndev.everkeep.models.Note.NOTE_ID"
        const val REMINDER_REPEATING = "com.darkndev.everkeep.models.Note.REMINDER_REPEATING"
    }

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(note: Note) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(NOTE_ID, note.id)
            putExtra(REMINDER_REPEATING, note.reminderRepeat != 0L)
        }
        if (note.reminderRepeat == 0L) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                note.reminderTime!!.toEpochSecond() * 1000,
                PendingIntent.getBroadcast(
                    context,
                    note.id.toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        } else {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                note.reminderTime!!.toEpochSecond() * 1000,
                note.reminderRepeat,
                PendingIntent.getBroadcast(
                    context,
                    note.id.toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
    }

    override fun cancel(note: Note) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                note.id.toInt(),
                Intent(context, ReminderReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}