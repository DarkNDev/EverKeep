package com.darkndev.everkeep.ui.permission_dialog

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.darkndev.everkeep.EverKeep.Companion.REMINDER_NOTIFICATION_CHANNEL_1
import com.darkndev.everkeep.utils.sdkVersion26AndAbove
import com.darkndev.everkeep.utils.sdkVersion31AndAbove
import com.darkndev.everkeep.utils.user_preferences.RequestType.NOTIFICATION_CHANNEL_ENABLE
import com.darkndev.everkeep.utils.user_preferences.RequestType.NOTIFICATION_ENABLE
import com.darkndev.everkeep.utils.user_preferences.RequestType.POST_NOTIFICATION_PERMISSION
import com.darkndev.everkeep.utils.user_preferences.RequestType.SCHEDULE_EXACT_ALARM_ENABLE
import com.darkndev.everkeep.utils.user_preferences.RequestType.SHOW_ABOUT
import com.darkndev.everkeep.utils.user_preferences.RequestType.WRITE_EXTERNAL_STORAGE_PERMISSION
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionDialog : DialogFragment() {

    private val viewModel: PermissionViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(viewModel.getTitle())
            .setMessage(viewModel.getMessage())
            .setPositiveButton(viewModel.positiveMessage()) { _, _ ->
                when (viewModel.requestType) {
                    WRITE_EXTERNAL_STORAGE_PERMISSION -> {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.parse("package:" + context?.packageName)
                        })
                        /*Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context?.packageName, null)
                        ).also(::startActivity)*/
                    }

                    SCHEDULE_EXACT_ALARM_ENABLE -> {
                        sdkVersion31AndAbove {
                            startActivity(Intent().apply {
                                action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                            })
                        }
                    }

                    POST_NOTIFICATION_PERMISSION -> {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.parse("package:" + context?.packageName)
                        })
                    }

                    NOTIFICATION_ENABLE -> {
                        sdkVersion26AndAbove {
                            startActivity(Intent().apply {
                                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName)
                            })
                        } ?: startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.parse("package:" + context?.packageName)
                        })
                    }

                    NOTIFICATION_CHANNEL_ENABLE -> {
                        sdkVersion26AndAbove {
                            startActivity(Intent().apply {
                                action = Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS
                                putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName)
                                putExtra(Settings.EXTRA_CHANNEL_ID, REMINDER_NOTIFICATION_CHANNEL_1)
                            })
                        }
                    }

                    SHOW_ABOUT -> {

                    }
                }
            }.create()
}