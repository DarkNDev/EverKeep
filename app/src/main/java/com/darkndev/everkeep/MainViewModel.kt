package com.darkndev.everkeep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.darkndev.everkeep.database.LabelDao
import com.darkndev.everkeep.database.NoteDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val noteDao: NoteDao,
    private val labelDao: LabelDao
) : ViewModel() {

    //labels
    val labels = labelDao.getAllFlowLabels().asLiveData()

    //search
    val searchPrefix = MutableStateFlow(listOf<Int>())
    val searchText = MutableStateFlow("")
    val itemsQuery = combine(
        searchPrefix.debounce(300),
        searchText.debounce(300)
    ) { prefix, text ->
        val prefixText = prefix.map {
            labelDao.getLabel(it.toLong())?.label
        }
        Pair(prefixText, text)
    }.flatMapLatest { (prefixText, text) ->
        noteDao.getQueryItems(text).map { list ->
            list.filter { item ->
                if (text.isBlank()) {
                    false
                } else if (prefixText.isEmpty()) {
                    true
                } else {
                    prefixText.contains(item.label)
                }
            }.filterNot {
                it.archived
            }
        }
    }.asLiveData()
}