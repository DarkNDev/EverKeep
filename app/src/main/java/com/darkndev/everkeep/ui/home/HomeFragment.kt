package com.darkndev.everkeep.ui.home

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.ItemTouchHelper
import com.darkndev.everkeep.R
import com.darkndev.everkeep.databinding.FragmentHomeBinding
import com.darkndev.everkeep.models.Note
import com.darkndev.everkeep.recyclerview.adapters.NoteAdapter
import com.darkndev.everkeep.recyclerview.decoration.ItemOffsetDecoration
import com.darkndev.everkeep.recyclerview.selection.NoteActionMode
import com.darkndev.everkeep.recyclerview.selection.NoteSwipeCallback
import com.darkndev.everkeep.recyclerview.selection.SelectionDetailsLookup
import com.darkndev.everkeep.recyclerview.selection.SelectionKeyProvider
import com.darkndev.everkeep.utils.user_preferences.SortOrder
import com.google.android.material.search.SearchView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val ITEM_SELECT = "com.darkndev.everkeep.ui.home.ITEM_SELECT"
private const val ITEM_SELECT_PINNED =
    "com.darkndev.everkeep.ui.home.ITEM_SELECT_PINNED"

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), MenuProvider {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var noteSwipeCallback: NoteSwipeCallback

    private lateinit var tracker: SelectionTracker<Long>
    private lateinit var trackerPinned: SelectionTracker<Long>

    private lateinit var callbackActionMode: NoteActionMode
    private var actionMode: ActionMode? = null

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var searchView: SearchView

    companion object {
        const val REQUEST_STRING = "com.darkndev.everkeep.REQUEST_STRING"
        const val RESULT_STRING = "com.darkndev.everkeep.RESULT_STRING"
        const val RESULT_MESSAGE_STRING = "com.darkndev.everkeep.RESULT_MESSAGE_STRING"
        const val NOTE_STRING_ID = "com.darkndev.everkeep.NOTE_STRING_ID"

        const val DELETION_CODE = 200
        const val ARCHIVE_CODE = 201
        const val UNARCHIVE_CODE = 202
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentHomeBinding.bind(view)

        drawerLayout = requireActivity().findViewById(R.id.drawer_layout)
        searchView = requireActivity().findViewById(R.id.search_view)

        val noteAdapter = NoteAdapter {
            onNoteClick(it)
        }

        val noteAdapterPinned = NoteAdapter {
            onNoteClick(it)
        }

        callbackActionMode = NoteActionMode(R.menu.menu_context, onActionItemClicked = {
            when (it) {
                R.id.action_archive -> {
                    archiveSelected()
                    tracker.clearSelection()
                    trackerPinned.clearSelection()
                    true
                }

                R.id.action_delete -> {
                    deleteSelected()
                    tracker.clearSelection()
                    trackerPinned.clearSelection()
                    true
                }

                else -> false
            }
        }, onDestroyActionMode = {
            tracker.clearSelection()
            trackerPinned.clearSelection()
            actionMode = null
        })

        binding.apply {
            val menuHost: MenuHost = requireActivity()

            menuHost.addMenuProvider(
                this@HomeFragment,
                viewLifecycleOwner,
                Lifecycle.State.RESUMED
            )

            viewModelBinding = viewModel
            lifecycleOwner = viewLifecycleOwner

            recyclerView.apply {
                addItemDecoration(ItemOffsetDecoration(8))
                adapter = noteAdapter
            }

            recyclerViewPinned.apply {
                addItemDecoration(ItemOffsetDecoration(8))
                adapter = noteAdapterPinned
            }

            noteSwipeCallback = NoteSwipeCallback(onSwiped = {
                viewModel.changeArchiveStatus(listOf(it), true)
                if (tracker.hasSelection() || trackerPinned.hasSelection()) {
                    tracker.clearSelection()
                    trackerPinned.clearSelection()
                }
            }, isSwipeEnabled = { !(tracker.hasSelection() || trackerPinned.hasSelection()) })

            ItemTouchHelper(noteSwipeCallback).attachToRecyclerView(recyclerView)
            ItemTouchHelper(noteSwipeCallback).attachToRecyclerView(recyclerViewPinned)

            tracker = SelectionTracker.Builder(
                ITEM_SELECT,
                recyclerView,
                SelectionKeyProvider(recyclerView),
                SelectionDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage()
            ).build()

            trackerPinned = SelectionTracker.Builder(
                ITEM_SELECT_PINNED,
                recyclerViewPinned,
                SelectionKeyProvider(recyclerViewPinned),
                SelectionDetailsLookup(recyclerViewPinned),
                StorageStrategy.createLongStorage()
            ).build()

            noteAdapter.setSelection(tracker)
            noteAdapterPinned.setSelection(trackerPinned)

            scrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (scrollY > oldScrollY && addItemFab.isExtended) {
                    addItemFab.shrink()
                }
                if (scrollY < oldScrollY && !addItemFab.isExtended) {
                    addItemFab.extend()
                }
                if (scrollY == 0) {
                    addItemFab.extend()
                }
            }
        }

        setFragmentResultListener(REQUEST_STRING) { _, bundle ->
            viewModel.onResult(
                bundle.getInt(RESULT_STRING),
                bundle.getString(RESULT_MESSAGE_STRING),
                bundle.getLong(NOTE_STRING_ID)
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.itemEvent.collectLatest { event ->
                when (event) {
                    is HomeViewModel.HomeEvent.ShowUndoNotesMessage -> {
                        Snackbar.make(binding.coordinator, event.message, Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                when (event.requestCode) {
                                    ARCHIVE_CODE -> viewModel.changeArchiveStatus(
                                        event.noteIds,
                                        false
                                    )

                                    DELETION_CODE -> viewModel.changeDeleteStatus(
                                        event.noteIds,
                                        false
                                    )

                                    UNARCHIVE_CODE -> viewModel.changeArchiveStatus(
                                        event.noteIds,
                                        true
                                    )
                                }
                            }.show()
                    }

                    is HomeViewModel.HomeEvent.NavigateWithItem -> {
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToNoteEditFragment(event.note)
                        findNavController().navigate(action)
                    }

                    is HomeViewModel.HomeEvent.ShowMessage -> {
                        Snackbar.make(binding.coordinator, event.message, Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        viewModel.apply {
            itemsPinned.observe(viewLifecycleOwner) {
                noteAdapterPinned.submitList(it)
            }
            itemsOthers.observe(viewLifecycleOwner) {
                noteAdapter.submitList(it)
            }
        }

        selectionTrackerObserver(view)
    }

    private fun onNoteClick(note: Note) {
        if (tracker.hasSelection()) {
            trackerPinned.select(note.id)
            return
        } else if (trackerPinned.hasSelection()) {
            tracker.select(note.id)
            return
        } else {
            viewModel.onItemClick(note)
        }
    }

    private fun selectionTrackerObserver(view: View) {
        tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onItemStateChanged(key: Long, selected: Boolean) {
                contextModeState(view)
            }
        })

        trackerPinned.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onItemStateChanged(key: Long, selected: Boolean) {
                contextModeState(view)
            }
        })
    }

    private fun contextModeState(view: View) {
        binding.apply {
            if (actionMode == null && (tracker.hasSelection() || trackerPinned.hasSelection())) {
                actionMode = view.startActionMode(callbackActionMode)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                addItemFab.hide()
            } else if (actionMode != null && (tracker.selection.isEmpty && trackerPinned.selection.isEmpty)) {
                actionMode?.finish()
                addItemFab.show()
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
            actionMode?.title =
                "${tracker.selection.size() + trackerPinned.selection.size()} Selected"
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_search, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {

        R.id.action_sort_priority -> {
            viewModel.onSortOrderChanged(SortOrder.ITEM_PRIORITY)
            true
        }

        R.id.action_sort_title -> {
            viewModel.onSortOrderChanged(SortOrder.ITEM_TITLE)
            true
        }

        R.id.action_search -> {
            searchView.show()
            true
        }

        else -> false
    }

    private fun deleteSelected() {
        val itemIds = tracker.selection + trackerPinned.selection
        viewModel.changeDeleteStatus(itemIds, true)
    }

    private fun archiveSelected() {
        val itemIds = tracker.selection + trackerPinned.selection
        viewModel.changeArchiveStatus(itemIds, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}