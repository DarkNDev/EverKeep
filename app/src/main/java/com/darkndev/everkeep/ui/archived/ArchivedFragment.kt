package com.darkndev.everkeep.ui.archived

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import com.darkndev.everkeep.R
import com.darkndev.everkeep.databinding.FragmentArchivedBinding
import com.darkndev.everkeep.recyclerview.adapters.NoteAdapter
import com.darkndev.everkeep.recyclerview.decoration.ItemOffsetDecoration
import com.darkndev.everkeep.recyclerview.selection.NoteActionMode
import com.darkndev.everkeep.recyclerview.selection.SelectionDetailsLookup
import com.darkndev.everkeep.recyclerview.selection.SelectionKeyProvider
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.DELETION_CODE
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.UNARCHIVE_CODE
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val ITEM_SELECT = "com.darkndev.everkeep.ui.archived.ITEM_SELECT"

@AndroidEntryPoint
class ArchivedFragment : Fragment(R.layout.fragment_archived),
    MenuProvider {

    private var _binding: FragmentArchivedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArchivedViewModel by viewModels()

    private lateinit var noteAdapter: NoteAdapter

    private lateinit var tracker: SelectionTracker<Long>

    private lateinit var callbackActionMode: NoteActionMode
    private var actionMode: ActionMode? = null

    private lateinit var drawerLayout: DrawerLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentArchivedBinding.bind(view)

        drawerLayout = requireActivity().findViewById(R.id.drawer_layout)

        noteAdapter = NoteAdapter {
            val action =
                ArchivedFragmentDirections.actionArchivedFragmentToNoteEditFragment(it)
            findNavController().navigate(action)
        }

        callbackActionMode =
            NoteActionMode(R.menu.menu_archived_action_mode, onActionItemClicked = {
                when (it) {
                    R.id.action_unarchive -> {
                        viewModel.changeUnArchiveStatus(tracker.selection.toList(), false)
                        true
                    }

                    R.id.action_delete -> {
                        viewModel.changeDeleteStatus(tracker.selection.toList(), true)
                        true
                    }

                    else -> false
                }
            }, onDestroyActionMode = {
                tracker.clearSelection()
                actionMode = null
            })

        binding.apply {
            val menuHost: MenuHost = requireActivity()

            menuHost.addMenuProvider(
                this@ArchivedFragment,
                viewLifecycleOwner,
                Lifecycle.State.RESUMED
            )

            viewModelBinding = viewModel
            lifecycleOwner = viewLifecycleOwner

            recyclerViewArchived.apply {
                addItemDecoration(ItemOffsetDecoration(8))
                adapter = noteAdapter
            }

            tracker = SelectionTracker.Builder(
                ITEM_SELECT,
                binding.recyclerViewArchived,
                SelectionKeyProvider(binding.recyclerViewArchived),
                SelectionDetailsLookup(binding.recyclerViewArchived),
                StorageStrategy.createLongStorage()
            ).build()

            noteAdapter.setSelection(tracker)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.itemEvent.collectLatest { event ->
                when (event) {

                    is ArchivedViewModel.ArchivedEvent.ShowMessage -> {
                        Snackbar.make(view, event.message, Snackbar.LENGTH_SHORT).show()
                    }

                    is ArchivedViewModel.ArchivedEvent.ShowUndoNotesMessage -> {
                        Snackbar.make(view, event.message, Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                if (event.requestCode == UNARCHIVE_CODE) viewModel.changeUnArchiveStatus(
                                    event.noteIds,
                                    true
                                ) else if (event.requestCode == DELETION_CODE) viewModel.changeDeleteStatus(
                                    event.noteIds,
                                    false
                                )
                            }.show()
                    }
                }
            }
        }

        selectionTrackerObserver(view)
        loadData()
    }

    private fun selectionTrackerObserver(view: View) {
        tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onItemStateChanged(key: Long, selected: Boolean) {
                if (actionMode == null && tracker.hasSelection()) {
                    actionMode = view.startActionMode(callbackActionMode)
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                } else if (actionMode != null && tracker.selection.isEmpty) {
                    actionMode?.finish()
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
                actionMode?.title = "${tracker.selection.size()} Selected"
            }
        })
    }

    private fun loadData() {
        viewModel.apply {
            itemsArchived.observe(viewLifecycleOwner) {
                noteAdapter.submitList(it)
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_archived, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {

        R.id.action_unarchive_all -> {
            val idsList = ArrayList<Long>()
            noteAdapter.currentList.forEach {
                idsList.add(it.id)
            }
            if (idsList.isNotEmpty()) viewModel.changeUnArchiveStatus(
                idsList,
                false
            ) else Toast.makeText(view?.context, "Empty", Toast.LENGTH_SHORT).show()
            true
        }

        R.id.action_delete_all -> {
            val idsList = ArrayList<Long>()
            noteAdapter.currentList.forEach {
                idsList.add(it.id)
            }
            if (idsList.isNotEmpty()) viewModel.changeDeleteStatus(
                idsList,
                true
            ) else Toast.makeText(view?.context, "Empty", Toast.LENGTH_SHORT).show()
            true
        }

        else -> false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}