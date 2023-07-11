package com.darkndev.everkeep.ui.confirm_dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConfirmDialog : DialogFragment() {

    private val viewModel: ConfirmViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(viewModel.titleText)
            .setMessage(viewModel.messageText)
            .setNegativeButton(viewModel.negativeText, null)
            .setPositiveButton(viewModel.positiveText) { _, _ ->
                viewModel.onConfirmation()
            }.create()
}