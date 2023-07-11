package com.darkndev.everkeep.ui.confirm_dialog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.darkndev.everkeep.database.NoteDao
import com.darkndev.everkeep.di.EverKeepScope
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.DELETION_CODE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmViewModel @Inject constructor(
    state: SavedStateHandle,
    @EverKeepScope private val everKeepScope: CoroutineScope,
    private val noteDao: NoteDao
) : ViewModel() {

    private val itemIds = state.get<LongArray>("NOTE_IDS")!!

    private val requestCode = state.get<Int>("REQUEST_CODE")

    val titleText = state.get<String>("TITLE")
    val messageText = state.get<String>("MESSAGE")
    val positiveText = state.get<String>("POSITIVE")
    val negativeText = state.get<String>("NEGATIVE")

    fun onConfirmation() = everKeepScope.launch {
        when (requestCode) {
            DELETION_CODE -> {
                noteDao.deleteItems(itemIds)
            }
        }
    }
}