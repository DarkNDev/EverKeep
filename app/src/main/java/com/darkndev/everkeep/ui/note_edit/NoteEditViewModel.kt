package com.darkndev.everkeep.ui.note_edit

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.darkndev.everkeep.EverKeep.Companion.REMINDER_NOTIFICATION_CHANNEL_1
import com.darkndev.everkeep.database.NoteDao
import com.darkndev.everkeep.models.Note
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.ARCHIVE_CODE
import com.darkndev.everkeep.utils.checkPermission
import com.darkndev.everkeep.utils.sdkVersion26AndAbove
import com.darkndev.everkeep.utils.sdkVersion31AndAbove
import com.darkndev.everkeep.utils.sdkVersion33AndAbove
import com.darkndev.everkeep.utils.user_preferences.RequestType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val noteDao: NoteDao,
    state: SavedStateHandle,
    private val application: Application
) : ViewModel() {

    var notificationPermission = false
    private var alarmPermission = false

    //saved states
    private val note = state.get<Note>("NOTE")!!
    val noteLiveData = noteDao.getItemFlow(note.id).asLiveData()

    var titleText = MutableStateFlow(note.title)
    var contentText = MutableStateFlow(note.content)

    //user requests
    fun afterTitleChanged(text: String) = viewModelScope.launch {
        if (noteLiveData.value?.title != text) {
            noteDao.updateTitle(note.id, text)
        }
    }

    fun afterNoteChanged(text: String) = viewModelScope.launch {
        if (noteLiveData.value?.content != text) {
            noteDao.updateContent(note.id, text)
        }
    }

    fun pinClicked(click: Boolean) = viewModelScope.launch {
        noteDao.updatePinned(note.id, click)
    }

    fun archiveClicked(click: Boolean) = viewModelScope.launch {
        noteDao.updateArchivedAll(longArrayOf(note.id), click)
        if (click) {
            addEditItemEventChannel.send(
                AddEditItemEvent.NavigateToHomeAfterArchived(ARCHIVE_CODE, "Note Archived", note.id)
            )
        } else {
            addEditItemEventChannel.send(AddEditItemEvent.ShowMessage("Note Unarchived"))
        }
    }


    //navigation
    fun bottomAppBarNavigationClicked() = viewModelScope.launch {
        addEditItemEventChannel.send(
            AddEditItemEvent.OpenBottomSheetSettingsDialog(
                noteDao.getItem(
                    note.id
                )!!
            )
        )
    }

    fun bottomAppBarMenuClicked() = viewModelScope.launch {
        addEditItemEventChannel.send(AddEditItemEvent.OpenScribbleFragment(noteDao.getItem(note.id)!!))
    }

    fun bottomAppBarStatusClicked() = viewModelScope.launch {
        addEditItemEventChannel.send(
            AddEditItemEvent.OpenBottomSheetStatusDialog(
                noteDao.getItem(
                    note.id
                )!!
            )
        )
    }

    fun remindClicked() = viewModelScope.launch {
        val alarmManager = application.getSystemService(AlarmManager::class.java)
        val notificationManager = application.getSystemService(NotificationManager::class.java)
        alarmPermission = sdkVersion31AndAbove {
            alarmManager.canScheduleExactAlarms()
        } ?: true
        notificationPermission = sdkVersion33AndAbove {
            checkPermission(application, Manifest.permission.POST_NOTIFICATIONS)
        } ?: true
        if (!alarmPermission) {
            addEditItemEventChannel.send(
                AddEditItemEvent.OpenSettings(
                    RequestType.SCHEDULE_EXACT_ALARM_ENABLE
                )
            )
        } else if (!notificationPermission) {
            addEditItemEventChannel.send(AddEditItemEvent.CheckNotificationPermission)
        } else if (!notificationManager.areNotificationsEnabled()) {
            addEditItemEventChannel.send(
                AddEditItemEvent.OpenSettings(
                    RequestType.NOTIFICATION_ENABLE
                )
            )
        } else if (sdkVersion26AndAbove {
                notificationManager.getNotificationChannel(REMINDER_NOTIFICATION_CHANNEL_1)
                    .let {
                        it != null && it.importance == NotificationManager.IMPORTANCE_NONE
                    }
            } == true
        ) {
            addEditItemEventChannel.send(
                AddEditItemEvent.OpenSettings(
                    RequestType.NOTIFICATION_CHANNEL_ENABLE
                )
            )
        } else {
            addEditItemEventChannel.send(AddEditItemEvent.OpenReminderFragment(noteDao.getItem(note.id)!!))
        }
    }


    //show result message
    fun setResult(message: String) = viewModelScope.launch {
        addEditItemEventChannel.send(AddEditItemEvent.ShowMessage(message))
    }


    //Events
    private val addEditItemEventChannel = Channel<AddEditItemEvent>()
    val addEditNoteEvent = addEditItemEventChannel.receiveAsFlow()

    sealed class AddEditItemEvent {
        data class ShowMessage(val message: String) : AddEditItemEvent()
        data class NavigateToHomeAfterArchived(
            val resultCode: Int,
            val resultMessage: String,
            val itemId: Long
        ) : AddEditItemEvent()

        data class OpenBottomSheetStatusDialog(val note: Note) : AddEditItemEvent()
        data class OpenBottomSheetSettingsDialog(val note: Note) : AddEditItemEvent()
        data class OpenScribbleFragment(val note: Note) : AddEditItemEvent()
        data class OpenReminderFragment(val note: Note) : AddEditItemEvent()
        data class OpenSettings(val requestType: RequestType) : AddEditItemEvent()
        object CheckNotificationPermission : AddEditItemEvent()
    }
}