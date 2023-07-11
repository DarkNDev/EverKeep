package com.darkndev.everkeep.features.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.darkndev.everkeep.EverKeep.Companion.SYSTEM_NOTIFICATION_CHANNEL_2
import com.darkndev.everkeep.MainActivity
import com.darkndev.everkeep.R
import com.darkndev.everkeep.database.NoteDao
import com.darkndev.everkeep.di.EverKeepScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RebootReceiver : BroadcastReceiver() {

    @EverKeepScope
    @Inject
    lateinit var everKeepScope: CoroutineScope

    @Inject
    lateinit var noteDao: NoteDao

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            everKeepScope.launch {
                val notesList = noteDao.getItems().first()
                notesList.forEach {
                    noteDao.updateReminder(it.id, false, it.reminderTime, it.reminderRepeat)
                }
            }
            showNotification(context)
        }
    }

    private fun showNotification(context: Context) {
        val notificationManager =
            context.getSystemService(NotificationManager::class.java)

        val pendingIntent = NavDeepLinkBuilder(context)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.homeFragment)
            .createPendingIntent()

        val title = "Reminders Cancelled"
        val content =
            "Reminders may have been cancelled if any set due to System Reboot. Please set them once again."

        val notification = NotificationCompat.Builder(context, SYSTEM_NOTIFICATION_CHANNEL_2)
            .setCategory(NotificationCompat.CATEGORY_SYSTEM)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText(content)
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            //.setOnlyAlertOnce(true)
            //.setColor(ContextCompat.getColor(context, R.color.seed))
            .build()

        notificationManager.notify(0, notification)
    }
}