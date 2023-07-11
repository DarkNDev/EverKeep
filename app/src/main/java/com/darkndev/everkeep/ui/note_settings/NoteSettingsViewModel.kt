package com.darkndev.everkeep.ui.note_settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkndev.everkeep.database.NoteDao
import com.darkndev.everkeep.models.Note
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.DELETION_CODE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteSettingsViewModel @Inject constructor(
    val noteDao: NoteDao,
    val state: SavedStateHandle
) : ViewModel() {

    //save states
    val note = state.get<Note>("NOTE")!!

    fun onDeleteClicked() = viewModelScope.launch {
        noteDao.updateRecycleAll(longArrayOf(note.id), true)
        noteSettingsEventChannel.send(
            NoteSettingsEvent.NavigateToHomeAfterDeleted(
                DELETION_CODE,
                "Note Deleted",
                note.id
            )
        )
    }

    fun onCopyClicked() = viewModelScope.launch {
        noteDao.insertItem(
            note.copy(
                id = 0,
                pinned = false,
                archived = false,
                deleted = false,
                reminderActive = false,
                reminderRepeat = 0L,
                reminderTime = null
            )
        )
        noteSettingsEventChannel.send(NoteSettingsEvent.CopyClicked("Note copy Created"))
    }

    fun onShareClicked() = viewModelScope.launch {
        noteSettingsEventChannel.send(NoteSettingsEvent.ShareNote(note))
    }

    //Events
    private val noteSettingsEventChannel = Channel<NoteSettingsEvent>()
    val noteSettingsEvent = noteSettingsEventChannel.receiveAsFlow()

    sealed class NoteSettingsEvent {
        data class NavigateToHomeAfterDeleted(
            val resultCode: Int,
            val resultMessage: String,
            val itemId: Long
        ) :
            NoteSettingsEvent()

        data class ShareNote(val item: Note) : NoteSettingsEvent()
        data class CopyClicked(val message: String) : NoteSettingsEvent()
    }
}