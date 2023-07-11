package com.darkndev.everkeep.ui.note_reminder

import android.app.AlarmManager
import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.darkndev.everkeep.database.NoteDao
import com.darkndev.everkeep.di.EverKeepScope
import com.darkndev.everkeep.features.reminder.ReminderScheduler
import com.darkndev.everkeep.models.Note
import com.darkndev.everkeep.utils.AFTERNOON
import com.darkndev.everkeep.utils.DAILY
import com.darkndev.everkeep.utils.DOES_NOT_REPEAT
import com.darkndev.everkeep.utils.EVENING
import com.darkndev.everkeep.utils.HOURLY
import com.darkndev.everkeep.utils.MONTHLY
import com.darkndev.everkeep.utils.MORNING
import com.darkndev.everkeep.utils.NIGHT
import com.darkndev.everkeep.utils.SELECT_DATE
import com.darkndev.everkeep.utils.SELECT_TIME
import com.darkndev.everkeep.utils.TODAY
import com.darkndev.everkeep.utils.TOMORROW
import com.darkndev.everkeep.utils.WEEKLY
import com.darkndev.everkeep.utils.YEARLY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class NoteReminderViewModel @Inject constructor(
    private val noteDao: NoteDao,
    @EverKeepScope private val everKeepScope: CoroutineScope,
    state: SavedStateHandle,
    application: Application
) : ViewModel() {

    //constants
    val dateListMap = mapOf(
        Pair(TODAY, LocalDate.now()),
        Pair(TOMORROW, LocalDate.now().plusDays(1)),
        Pair(SELECT_DATE, LocalDate.now())
    )

    val timeListMap = mapOf(
        Pair(MORNING, LocalTime.of(8, 0)),
        Pair(AFTERNOON, LocalTime.of(13, 0)),
        Pair(EVENING, LocalTime.of(18, 0)),
        Pair(NIGHT, LocalTime.of(22, 0)),
        Pair(SELECT_TIME, LocalTime.now())
    )

    val repeatListMap = mapOf(
        Pair(DOES_NOT_REPEAT, 0L),
        Pair(HOURLY, AlarmManager.INTERVAL_HOUR),
        Pair(DAILY, AlarmManager.INTERVAL_DAY),
        Pair(WEEKLY, AlarmManager.INTERVAL_DAY * 7),
        Pair(MONTHLY, AlarmManager.INTERVAL_DAY * 30),
        Pair(YEARLY, AlarmManager.INTERVAL_DAY * 365),
    )

    //save states
    val note = state.get<Note>("NOTE")!!
    val date = MutableLiveData(note.reminderTime?.toLocalDate() ?: dateListMap[TOMORROW])
    val time = MutableLiveData(note.reminderTime?.toLocalTime() ?: timeListMap[MORNING])
    val repeat = MutableLiveData(note.reminderRepeat)
    private val scheduler = ReminderScheduler(application)

    fun scheduleReminder(): String {
        val scheduleTime = ZonedDateTime.of(date.value, time.value, ZoneId.systemDefault())
        return if (scheduleTime.isAfter(ZonedDateTime.now())) {
            everKeepScope.launch {
                noteDao.updateReminder(note.id, true, scheduleTime, repeat.value!!)
            }
            scheduler.schedule(
                note.copy(
                    reminderActive = true,
                    reminderTime = scheduleTime,
                    reminderRepeat = repeat.value!!
                )
            )
            "Reminder Scheduled"
        } else {
            everKeepScope.launch {
                noteDao.updateReminder(note.id, false, scheduleTime)
            }
            scheduler.cancel(note.copy(reminderActive = true, reminderTime = scheduleTime))
            "Please check the Time Set!!"
        }
    }

    fun cancelReminder(): String? {
        if (note.reminderTime != null) {
            val scheduleTime = ZonedDateTime.of(date.value, time.value, ZoneId.systemDefault())
            everKeepScope.launch {
                noteDao.updateReminder(note.id, false, scheduleTime, repeat.value!!)
            }
            scheduler.cancel(
                note.copy(
                    reminderActive = false,
                    reminderTime = scheduleTime,
                    reminderRepeat = repeat.value!!
                )
            )
            return "Reminder Canceled"
        }
        return null
    }

    fun deleteReminder(): String? {
        if (note.reminderTime != null) {
            everKeepScope.launch {
                noteDao.updateReminder(note.id, false, null, 0L)
            }
            scheduler.cancel(
                note.copy(
                    reminderActive = false,
                    reminderTime = null,
                    reminderRepeat = 0L
                )
            )
            return "Reminder Deleted"
        }
        return null
    }
}