package com.darkndev.everkeep.features.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.navigation.NavDeepLinkBuilder
import com.darkndev.everkeep.EverKeep.Companion.REMINDER_NOTIFICATION_CHANNEL_1
import com.darkndev.everkeep.MainActivity
import com.darkndev.everkeep.R
import com.darkndev.everkeep.database.NoteDao
import com.darkndev.everkeep.di.EverKeepScope
import com.darkndev.everkeep.features.reminder.ReminderScheduler.Companion.NOTE_ID
import com.darkndev.everkeep.features.reminder.ReminderScheduler.Companion.REMINDER_REPEATING
import com.darkndev.everkeep.models.Note
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @EverKeepScope
    @Inject
    lateinit var everKeepScope: CoroutineScope

    @Inject
    lateinit var noteDao: NoteDao

    override fun onReceive(context: Context?, intent: Intent?) {
        val noteId = intent?.getLongExtra(NOTE_ID, 0) ?: return
        val repeating = intent.getBooleanExtra(REMINDER_REPEATING, false)
        everKeepScope.launch {
            val note = noteDao.getItem(noteId)
            if (note != null && !note.deleted) {
                showNotification(context!!, note)
                if (!repeating) {
                    noteDao.updateReminder(
                        noteId,
                        false,
                        note.reminderTime,
                        note.reminderRepeat
                    )
                }
            }
        }
    }

    private fun showNotification(context: Context, note: Note) {
        val notificationManager =
            context.getSystemService(NotificationManager::class.java)

        val pendingIntent = NavDeepLinkBuilder(context)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.homeFragment)
            //.setArguments(bundleOf("NOTE" to note))
            .createPendingIntent()

        val notification = NotificationCompat.Builder(context, REMINDER_NOTIFICATION_CHANNEL_1)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentTitle(note.title)
            .setContentText(note.content)
            .setSubText(note.label)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setStyle(
                BigTextStyle()
                    .setBigContentTitle(note.title)
                    .bigText(note.content)
                    .setSummaryText(note.label)
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            //.setOnlyAlertOnce(true)
            //.setColor(ContextCompat.getColor(context, R.color.seed))
            .build()

        notificationManager.notify(note.id.toInt(), notification)
    }
}