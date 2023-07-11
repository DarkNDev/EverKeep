package com.darkndev.everkeep.ui.permission_dialog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.darkndev.everkeep.utils.user_preferences.RequestType
import com.darkndev.everkeep.utils.user_preferences.RequestType.NOTIFICATION_CHANNEL_ENABLE
import com.darkndev.everkeep.utils.user_preferences.RequestType.NOTIFICATION_ENABLE
import com.darkndev.everkeep.utils.user_preferences.RequestType.POST_NOTIFICATION_PERMISSION
import com.darkndev.everkeep.utils.user_preferences.RequestType.SCHEDULE_EXACT_ALARM_ENABLE
import com.darkndev.everkeep.utils.user_preferences.RequestType.SHOW_ABOUT
import com.darkndev.everkeep.utils.user_preferences.RequestType.WRITE_EXTERNAL_STORAGE_PERMISSION
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    state: SavedStateHandle
) : ViewModel() {

    //state
    val requestType = state.get<RequestType>("REQUEST_TYPE")!!

    fun getTitle() = when (requestType) {
        WRITE_EXTERNAL_STORAGE_PERMISSION -> "Permission"
        SCHEDULE_EXACT_ALARM_ENABLE -> "Settings"
        POST_NOTIFICATION_PERMISSION -> "Permission"
        NOTIFICATION_ENABLE -> "Settings"
        NOTIFICATION_CHANNEL_ENABLE -> "Settings"
        SHOW_ABOUT -> "About EverKeep"
    }

    fun getMessage() = when (requestType) {
        WRITE_EXTERNAL_STORAGE_PERMISSION -> "It seems you have declined permission needed to save the scribble on your device. You can go to the app settings to grant it."
        SCHEDULE_EXACT_ALARM_ENABLE -> "It seems you have disabled Alarms & reminders for this app which is needed to set reminder. You can go to the settings to enable it."
        POST_NOTIFICATION_PERMISSION -> "It seems you have declined permission needed to show reminder notification on your device. You can go to the app settings to grant it."
        NOTIFICATION_ENABLE -> "It seems you have disabled Notification for this app which is needed to set reminder. You can go to the settings to enable it."
        NOTIFICATION_CHANNEL_ENABLE -> "It seems you have disabled Notification Channel for this app which is needed to set reminder. You can go to the settings to enable it."
        SHOW_ABOUT -> "EverKeep is a simple note taking app with scribble and reminder features."
    }

    fun positiveMessage() = when (requestType) {
        WRITE_EXTERNAL_STORAGE_PERMISSION -> "Grant Permission"
        SCHEDULE_EXACT_ALARM_ENABLE -> "Enable"
        POST_NOTIFICATION_PERMISSION -> "Grant Permission"
        NOTIFICATION_ENABLE -> "Enable"
        NOTIFICATION_CHANNEL_ENABLE -> "Enable"
        SHOW_ABOUT -> "Ok"
    }
}