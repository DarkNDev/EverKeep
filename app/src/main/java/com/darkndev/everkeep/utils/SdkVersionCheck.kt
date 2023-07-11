package com.darkndev.everkeep.utils

import android.os.Build

inline fun <T> sdkVersion26AndAbove(onSdkVersion26: () -> T): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        onSdkVersion26()
    } else null
}

inline fun <T> sdkVersion29AndAbove(onSdkVersion29: () -> T): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        onSdkVersion29()
    } else null
}

inline fun <T> sdkVersion30AndAbove(onSdkVersion30: () -> T): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        onSdkVersion30()
    } else null
}

inline fun <T> sdkVersion31AndAbove(onSdkVersion31: () -> T): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        onSdkVersion31()
    } else null
}

inline fun <T> sdkVersion33AndAbove(onSdkVersion33: () -> T): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        onSdkVersion33()
    } else null
}
