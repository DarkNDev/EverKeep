package com.darkndev.everkeep.ui.note_recycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.darkndev.everkeep.database.NoteDao
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.DELETION_CODE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteRecycleViewModel @Inject constructor(
    private val noteDao: NoteDao
) : ViewModel() {

    //deleted items
    val items = noteDao.getItems(true).asLiveData()

    fun deleteAllNotes(idsList: List<Long>) = viewModelScope.launch {
        noteRecycleEventChannel.send(
            NoteRecycleEvent.NavigateToConfirmDialog(
                DELETION_CODE,
                idsList,
                "Empty Recycle Bin",
                "All notes will be permanently Deleted.",
                "Empty bin",
                "Cancel"
            )
        )
    }

    fun deleteNotes(idsList: List<Long>) = viewModelScope.launch {
        noteRecycleEventChannel.send(
            NoteRecycleEvent.NavigateToConfirmDialog(
                DELETION_CODE,
                idsList,
                "Confirm Deletion",
                "Delete ${if (idsList.size == 1) "note" else "notes"} permanently?",
                "Delete",
                "Cancel"
            )
        )
    }

    fun restoreNotes(idsList: List<Long>, value: Boolean = false) = viewModelScope.launch {
        noteDao.updateRecycleAll(idsList.toLongArray(), value)
        if (!value) noteRecycleEventChannel.send(
            NoteRecycleEvent.ShowMessage(idsList, "Note Restored")
        )
    }

    //Events
    private val noteRecycleEventChannel = Channel<NoteRecycleEvent>()
    val noteRecycleEvent = noteRecycleEventChannel.receiveAsFlow()

    sealed class NoteRecycleEvent {
        data class NavigateToConfirmDialog(
            val requestCode: Int,
            val idsList: List<Long>,
            val title: String,
            val message: String,
            val positiveText: String,
            val negativeText: String
        ) :
            NoteRecycleEvent()

        data class ShowMessage(val idsList: List<Long>, val message: String) : NoteRecycleEvent()
    }
}