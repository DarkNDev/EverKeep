package com.darkndev.everkeep.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.darkndev.everkeep.database.NoteDao
import com.darkndev.everkeep.database.PreferencesManager
import com.darkndev.everkeep.models.Note
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.ARCHIVE_CODE
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.DELETION_CODE
import com.darkndev.everkeep.utils.user_preferences.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteDao: NoteDao,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    //user preferences
    private val userPreferences = preferencesManager.preferencesFlow
    private val items = userPreferences.flatMapLatest { filterPreferences ->
        noteDao.getItems().map {
            when (filterPreferences.sortOrder) {
                SortOrder.ITEM_PRIORITY -> it.sortedBy { item ->
                    item.priority
                }.asReversed()

                SortOrder.ITEM_TITLE -> it.sortedBy { item ->
                    item.title
                }
            }
        }
    }

    val itemsPinned = items.map { list ->
        list.filter {
            it.pinned && !it.archived
        }
    }.asLiveData()

    val itemsOthers = items.map { list ->
        list.filter {
            !it.pinned && !it.archived
        }
    }.asLiveData()

    fun onSortOrderChanged(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    //room operations
    fun insertNewNote() = viewModelScope.launch {
        val id = noteDao.insertItem(Note())
        homeEventChannel.send(HomeEvent.NavigateWithItem(noteDao.getItem(id)!!))
    }

    fun changeDeleteStatus(itemIds: List<Long>, deleted: Boolean) = viewModelScope.launch {
        noteDao.updateRecycleAll(itemIds.toLongArray(), deleted)
        val message = if (itemIds.size == 1) "Note Deleted" else "Notes Deleted"
        if (deleted) {
            homeEventChannel.send(
                HomeEvent.ShowUndoNotesMessage(
                    DELETION_CODE,
                    message,
                    itemIds
                )
            )
        }
    }

    fun changeArchiveStatus(itemIds: List<Long>, archived: Boolean) = viewModelScope.launch {
        noteDao.updateArchivedAll(itemIds.toLongArray(), archived)
        val message = if (itemIds.size == 1) "Note Archived" else "Notes Archived"
        if (archived) {
            homeEventChannel.send(
                HomeEvent.ShowUndoNotesMessage(
                    ARCHIVE_CODE,
                    message,
                    itemIds
                )
            )
        }
    }

    fun onItemClick(note: Note) = viewModelScope.launch {
        homeEventChannel.send(HomeEvent.NavigateWithItem(note))
    }

    //Results
    fun onResult(resultCode: Int, resultMessage: String?, noteId: Long) = viewModelScope.launch {
        homeEventChannel.send(
            HomeEvent.ShowUndoNotesMessage(
                resultCode,
                resultMessage ?: "Error",
                listOf(noteId)
            )
        )
    }

    //Events
    private val homeEventChannel = Channel<HomeEvent>()
    val itemEvent = homeEventChannel.receiveAsFlow()

    sealed class HomeEvent {
        data class ShowUndoNotesMessage(
            val requestCode: Int,
            val message: String,
            val noteIds: List<Long>
        ) : HomeEvent()

        data class NavigateWithItem(val note: Note) : HomeEvent()
        data class ShowMessage(val message: String) : HomeEvent()
    }
}