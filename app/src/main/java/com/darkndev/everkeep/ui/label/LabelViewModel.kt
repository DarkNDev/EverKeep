package com.darkndev.everkeep.ui.label

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.darkndev.everkeep.database.LabelDao
import com.darkndev.everkeep.models.Label
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LabelViewModel @Inject constructor(
    private val labelDao: LabelDao
) : ViewModel() {

    private val labelsList = labelDao.getAllFlowLabels()
    val labels = labelsList.asLiveData()

    //room transactions
    fun deleteLabel(label: Label) = viewModelScope.launch {
        labelDao.deleteLabel(label)
        labelEventChannel.send(LabelEvent.ShowActionMessage("Label Deleted", label))
    }

    fun undoDeleteLabel(label: Label) = viewModelScope.launch {
        labelDao.insertLabel(label)
    }

    fun updateLabel(text: String) = viewModelScope.launch {
        val count = labelsList.first().count { it.label == text }

        if (count == 0) {
            labelDao.insertLabel(Label(label = text))
            labelEventChannel.send(LabelEvent.ShowMessage("Label Added"))
        } else {
            labelEventChannel.send(LabelEvent.ShowMessage("Label already Present"))
        }
    }

    //Events
    private val labelEventChannel = Channel<LabelEvent>()
    val labelEvent = labelEventChannel.receiveAsFlow()

    sealed class LabelEvent {

        data class ShowMessage(val message: String) : LabelEvent()
        data class ShowActionMessage(val message: String, val label: Label) : LabelEvent()
    }

}