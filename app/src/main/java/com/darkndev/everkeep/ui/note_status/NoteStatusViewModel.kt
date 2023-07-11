package com.darkndev.everkeep.ui.note_status

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkndev.everkeep.database.LabelDao
import com.darkndev.everkeep.database.NoteDao
import com.darkndev.everkeep.models.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteStatusViewModel @Inject constructor(
    private val noteDao: NoteDao,
    labelDao: LabelDao,
    state: SavedStateHandle
) : ViewModel() {

    val labelList = labelDao.getAllFlowLabels()

    //save states
    private val note = state.get<Note>("NOTE")!!
    val priority = MutableLiveData(note.priority)
    val label = MutableLiveData(note.label)

    fun afterPriorityChanged(value: Int) = viewModelScope.launch {
        priority.value = value
        noteDao.updatePriority(note.id, value)
    }

    fun afterLabelChanged(labelText: String) = viewModelScope.launch {
        label.value = labelText
        noteDao.updateLabel(note.id, labelText)
    }
}