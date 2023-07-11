package com.darkndev.everkeep.ui.note_scribble

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.darkndev.everkeep.R
import com.darkndev.everkeep.databinding.FragmentNoteScribbleBinding
import com.darkndev.everkeep.ui.note_edit.NoteEditFragment.Companion.NOTE_MESSAGE
import com.darkndev.everkeep.ui.note_edit.NoteEditFragment.Companion.NOTE_REQUEST
import com.darkndev.everkeep.utils.user_preferences.RequestType
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteScribbleFragment : Fragment(R.layout.fragment_note_scribble),
    MenuProvider, Toolbar.OnMenuItemClickListener {

    private var _binding: FragmentNoteScribbleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteScribbleViewModel by viewModels()

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.writePermissionGranted = isGranted
            } else {
                val action =
                    NoteScribbleFragmentDirections.actionGlobalPermissionDialog(
                        RequestType.WRITE_EXTERNAL_STORAGE_PERMISSION
                    )
                findNavController().navigate(action)
            }
        }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentNoteScribbleBinding.bind(view)

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val bottomSheetBehaviorScribble =
            BottomSheetBehavior.from(binding.strokeBottomSheet)

        binding.apply {
            val menuHost: MenuHost = requireActivity()

            menuHost.addMenuProvider(
                this@NoteScribbleFragment,
                viewLifecycleOwner,
                Lifecycle.State.RESUMED
            )

            if (viewModel.imageArray.isNotEmpty()) {
                scribbleView.setBitmap(
                    BitmapFactory.decodeByteArray(
                        viewModel.imageArray,
                        0,
                        viewModel.imageArray.size
                    )
                )
            } else {
                scribbleView.initialiseBitmap = true
            }

            scribbleView.afterDraw {
                viewModel.updateScribble(it)
            }

            bottomAppBar.apply {
                setNavigationOnClickListener {
                    bottomSheetBehaviorScribble.state = BottomSheetBehavior.STATE_EXPANDED
                }
                setOnMenuItemClickListener(this@NoteScribbleFragment)
            }

            bottomSheetBehaviorScribble.state = BottomSheetBehavior.STATE_HIDDEN

            dragHandle.setOnClickListener {
                bottomSheetBehaviorScribble.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.scribbleEvent.collectLatest { event ->
                when (event) {
                    is NoteScribbleViewModel.ScribbleEvent.NavigateWithMessage -> {
                        setFragmentResult(
                            NOTE_REQUEST,
                            bundleOf(NOTE_MESSAGE to event.message)
                        )
                        findNavController().popBackStack()
                    }

                    is NoteScribbleViewModel.ScribbleEvent.CheckWritePermission -> {
                        permissionsLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }

                    is NoteScribbleViewModel.ScribbleEvent.ShowMessage -> {
                        Snackbar.make(
                            view,
                            event.message,
                            Snackbar.LENGTH_SHORT
                        ).setAnchorView(binding.bottomAppBar).show()
                    }

                    is NoteScribbleViewModel.ScribbleEvent.ShareScribble -> {
                        startActivity(Intent.createChooser(Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, event.scribbleUri)
                            type = "image/jpeg"
                        }, null))
                    }
                }
            }
        }

        val callback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                bottomSheetBehaviorScribble.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        bottomSheetBehaviorScribble.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    callback.isEnabled = true
                }
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    callback.isEnabled = false
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        strokeSettings()
    }

    private fun strokeSettings() {
        binding.apply {
            colorButtonGroup.setOnSelectListener {
                scribbleView.strokeColor = it.selectedBgColor
            }
            colorButtonGroup.selectButton(binding.blackColor)

            strokeWidthSlider.addOnChangeListener { _, value, _ ->
                scribbleView.strokeWidth = value
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_note_scribble, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.action_delete -> {
            viewModel.deleteScribble()
            true
        }

        R.id.action_save -> {
            viewModel.checkWritePermissionAndSaveScribble(binding.scribbleView.getBitmap())
            true
        }

        R.id.action_share -> {
            viewModel.shareScribble(binding.scribbleView.getBitmap())
            true
        }

        else -> false
    }

    override fun onMenuItemClick(item: MenuItem) = when (item.itemId) {
        R.id.action_undo -> {
            binding.scribbleView.undoStroke()
            true
        }

        R.id.action_redo -> {
            binding.scribbleView.redoStroke()
            true
        }

        R.id.action_clear -> {
            binding.apply {
                scribbleView.clearCanvas()
                Snackbar.make(
                    requireView(),
                    "Scribble Cleared",
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(bottomAppBar).show()
            }
            true
        }

        else -> false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}