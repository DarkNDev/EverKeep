package com.darkndev.everkeep.ui.note_settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.darkndev.everkeep.R
import com.darkndev.everkeep.databinding.FragmentNoteSettingsBinding
import com.darkndev.everkeep.models.NoteSetting
import com.darkndev.everkeep.recyclerview.adapters.NoteSettingsAdapter
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.NOTE_STRING_ID
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.REQUEST_STRING
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.RESULT_MESSAGE_STRING
import com.darkndev.everkeep.ui.home.HomeFragment.Companion.RESULT_STRING
import com.darkndev.everkeep.ui.note_edit.NoteEditFragment.Companion.NOTE_MESSAGE
import com.darkndev.everkeep.ui.note_edit.NoteEditFragment.Companion.NOTE_REQUEST
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteSettingsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentNoteSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteSettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteSettingsBinding.inflate(inflater, container, false)

        val items = listOf(
            NoteSetting(R.drawable.delete, "Delete"),
            NoteSetting(R.drawable.content_copy, "Make a copy"),
            NoteSetting(R.drawable.share, "Share")
        )

        val settingAdapter = NoteSettingsAdapter(items) {
            when (it) {
                R.drawable.delete -> {
                    viewModel.onDeleteClicked()
                }

                R.drawable.content_copy -> {
                    viewModel.onCopyClicked()
                }

                R.drawable.share -> {
                    viewModel.onShareClicked()
                }
            }
        }

        binding.recyclerViewMenu.adapter = settingAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.noteSettingsEvent.collectLatest { event ->
                when (event) {
                    is NoteSettingsViewModel.NoteSettingsEvent.NavigateToHomeAfterDeleted -> {
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

                    is NoteSettingsViewModel.NoteSettingsEvent.CopyClicked -> {
                        dismiss()
                        setFragmentResult(NOTE_REQUEST, bundleOf(NOTE_MESSAGE to event.message))
                    }

                    is NoteSettingsViewModel.NoteSettingsEvent.ShareNote -> {
                        dismiss()
                        startActivity(Intent.createChooser(Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, event.item.content)
                            type = "text/plain"
                        }, null))
                    }
                }
            }
        }

        return binding.root
    }
}