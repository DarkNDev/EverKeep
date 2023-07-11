package com.darkndev.everkeep.ui.note_edit

import android.Manifest
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.darkndev.everkeep.R
import com.darkndev.everkeep.databinding.FragmentNoteEditBinding
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.NOTE_STRING_ID
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.REQUEST_STRING
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.RESULT_MESSAGE_STRING
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.RESULT_STRING
import com.darkndev.everkeep.ui.note_scribble.NoteScribbleFragmentDirections
import com.darkndev.everkeep.utils.getDatePickerDate
import com.darkndev.everkeep.utils.getFormatTime
import com.darkndev.everkeep.utils.getTimePickerTime
import com.darkndev.everkeep.utils.sdkVersion33AndAbove
import com.darkndev.everkeep.utils.user_preferences.RequestType
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteEditFragment : Fragment(R.layout.fragment_note_edit), MenuProvider,
    Toolbar.OnMenuItemClickListener {

    private var _binding: FragmentNoteEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteEditViewModel by viewModels()

    private lateinit var pin: MenuItem
    private lateinit var archive: MenuItem
    private lateinit var remind: MenuItem

    companion object {
        const val NOTE_MESSAGE = "note_message"
        const val NOTE_REQUEST = "note_request"
    }

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.notificationPermission = isGranted
            } else {
                val action =
                    NoteScribbleFragmentDirections.actionGlobalPermissionDialog(
                        RequestType.POST_NOTIFICATION_PERMISSION
                    )
                findNavController().navigate(action)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentNoteEditBinding.bind(view)

        binding.apply {
            val menuHost: MenuHost = requireActivity()

            menuHost.addMenuProvider(
                this@NoteEditFragment,
                viewLifecycleOwner,
                Lifecycle.State.RESUMED
            )

            viewModelBinding = viewModel
            lifecycleOwner = viewLifecycleOwner

            bottomAppBar.apply {
                setOnMenuItemClickListener(this@NoteEditFragment)

                setNavigationOnClickListener {
                    viewModel.bottomAppBarNavigationClicked()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.apply {
                titleText.debounce(300).collectLatest {
                    afterTitleChanged(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.apply {
                contentText.debounce(300).collectLatest {
                    afterNoteChanged(it)
                }
            }
        }
        setFragmentResultListener(NOTE_REQUEST) { _, bundle ->
            viewModel.setResult(bundle.getString(NOTE_MESSAGE)!!)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addEditNoteEvent.collectLatest { event ->
                when (event) {
                    is NoteEditViewModel.AddEditItemEvent.NavigateToHomeAfterArchived -> {
                        setFragmentResult(
                            REQUEST_STRING,
                            bundleOf(
                                RESULT_STRING to event.resultCode,
                                RESULT_MESSAGE_STRING to event.resultMessage,
                                NOTE_STRING_ID to event.itemId
                            )
                        )
                        findNavController().popBackStack(R.id.homeFragment, false)
                    }

                    is NoteEditViewModel.AddEditItemEvent.ShowMessage -> {
                        Snackbar.make(view, event.message, Snackbar.LENGTH_LONG)
                            .setAnchorView(binding.bottomAppBar).show()
                    }

                    is NoteEditViewModel.AddEditItemEvent.OpenBottomSheetStatusDialog -> {
                        val action =
                            NoteEditFragmentDirections.actionNoteEditFragmentToNoteStatusFragment(
                                event.note
                            )
                        findNavController().navigate(action)
                    }

                    is NoteEditViewModel.AddEditItemEvent.OpenScribbleFragment -> {
                        val action =
                            NoteEditFragmentDirections.actionNoteEditFragmentToNoteScribbleFragment(
                                event.note
                            )
                        findNavController().navigate(action)
                    }

                    is NoteEditViewModel.AddEditItemEvent.OpenBottomSheetSettingsDialog -> {
                        val action =
                            NoteEditFragmentDirections.actionNoteEditFragmentToNoteSettingsFragment(
                                event.note
                            )
                        findNavController().navigate(action)
                    }

                    is NoteEditViewModel.AddEditItemEvent.OpenReminderFragment -> {
                        val action =
                            NoteEditFragmentDirections.actionNoteEditFragmentToNoteReminderFragment(
                                event.note
                            )
                        findNavController().navigate(action)
                    }

                    is NoteEditViewModel.AddEditItemEvent.OpenSettings -> {
                        val action =
                            NoteEditFragmentDirections.actionGlobalPermissionDialog(event.requestType)
                        findNavController().navigate(action)
                    }

                    is NoteEditViewModel.AddEditItemEvent.CheckNotificationPermission -> {
                        sdkVersion33AndAbove {
                            permissionsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }
            }
        }

        viewModel.noteLiveData.observe(viewLifecycleOwner) {
            binding.apply {
                modifiedText.text = getFormatTime(it.modified)
                labelChip.text = it.label
                if (it.reminderTime == null) {
                    reminderChip.visibility = View.GONE
                    reminderChip.text = StringBuilder("Reminder")
                    reminderChip.setChipIconResource(R.drawable.notify_add)
                } else {
                    reminderChip.visibility = View.VISIBLE
                    reminderChip.text =
                        StringBuilder(getDatePickerDate(it.reminderTime.toLocalDate())!!)
                            .append(", ")
                            .append(getTimePickerTime(it.reminderTime.toLocalTime())!!)
                    if (it.reminderActive && it.reminderRepeat != 0L) {
                        reminderChip.setChipIconResource(R.drawable.repeat)
                    } else if (it.reminderActive) {
                        reminderChip.setChipIconResource(R.drawable.notify_active)
                    } else {
                        reminderChip.setChipIconResource(R.drawable.notify_cancel)
                    }
                }
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_note, menu)
        pin = menu.findItem(R.id.action_pin)
        archive = menu.findItem(R.id.action_archive)
        remind = menu.findItem(R.id.action_remind)

        viewModel.noteLiveData.observe(viewLifecycleOwner) {
            if (it.reminderTime == null) remind.setIcon(R.drawable.notify_add) else remind.setIcon(R.drawable.notify_active)
            if (it.archived) archive.setIcon(R.drawable.unarchive) else archive.setIcon(R.drawable.archive_outline)
            if (it.pinned) pin.setIcon(R.drawable.round_push_pin) else pin.setIcon(R.drawable.outline_push_pin)
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.action_pin -> {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.pinClicked(!viewModel.noteLiveData.value?.pinned!!)
            }
            true
        }

        R.id.action_remind -> {
            viewModel.remindClicked()
            true
        }

        R.id.action_archive -> {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.archiveClicked(!viewModel.noteLiveData.value?.archived!!)
            }
            true
        }

        else -> false
    }

    override fun onMenuItemClick(item: MenuItem) = when (item.itemId) {
        R.id.action_status -> {
            viewModel.bottomAppBarStatusClicked()
            true
        }

        R.id.action_scribble -> {
            viewModel.bottomAppBarMenuClicked()
            true
        }

        else -> false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}