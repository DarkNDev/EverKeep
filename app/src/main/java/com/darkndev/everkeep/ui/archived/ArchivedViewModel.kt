package com.darkndev.everkeep.ui.archived

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.darkndev.everkeep.database.NoteDao
import com.darkndev.everkeep.database.PreferencesManager
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.DELETION_CODE
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.UNARCHIVE_CODE
import com.darkndev.everkeep.utils.user_preferences.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchivedViewModel @Inject constructor(
    private val noteDao: NoteDao,
    preferencesManager: PreferencesManager
) : ViewModel() {

    //archived items
    private val userPreferences = preferencesManager.preferencesFlow
    val itemsArchived = userPreferences.flatMapLatest { filterPreferences ->
        noteDao.getItems().map {
            when (filterPreferences.sortOrder) {
                SortOrder.ITEM_PRIORITY -> it.sortedBy { item ->
                    item.priority
                }.asReversed()

                SortOrder.ITEM_TITLE -> it.sortedBy { item ->
                    item.title
                }
            }
            it.filter { item ->
                item.archived
            }
        }
    }.asLiveData()

    //room operations

    fun changeDeleteStatus(itemIds: List<Long>, deleted: Boolean) = viewModelScope.launch {
        noteDao.updateRecycleAll(itemIds.toLongArray(), deleted)
        val message = if (itemIds.size == 1) "Note Deleted" else "Notes Deleted"
        if (deleted) archivedEventChannel.send(
            ArchivedEvent.ShowUndoNotesMessage(
                DELETION_CODE,
                message,
                itemIds
            )
        )
    }

    fun changeUnArchiveStatus(itemIds: List<Long>, archived: Boolean) = viewModelScope.launch {
        noteDao.updateArchivedAll(itemIds.toLongArray(), archived)
        val message = if (itemIds.size == 1) "Note Unarchived" else "Notes Unarchived"
        if (!archived) archivedEventChannel.send(
            ArchivedEvent.ShowUndoNotesMessage(
                UNARCHIVE_CODE,
                message,
                itemIds
            )
        )
    }

    //Events
    private val archivedEventChannel = Channel<ArchivedEvent>()
    val itemEvent = archivedEventChannel.receiveAsFlow()

    sealed class ArchivedEvent {
        data class ShowUndoNotesMessage(
            val requestCode: Int,
            val message: String,
            val noteIds: List<Long>
        ) :
            ArchivedEvent()
        data class ShowMessage(val message: String) : ArchivedEvent()
    }
}