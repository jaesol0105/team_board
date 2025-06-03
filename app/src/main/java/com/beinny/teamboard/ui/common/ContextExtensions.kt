package com.beinny.teamboard.ui.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import androidx.activity.result.ActivityResultLauncher

inline fun <reified T : Activity> Activity.launch(
    flags: Int = 0,
    finishCurrent: Boolean = false,
    noinline configure: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java).apply {
        if (flags != 0) addFlags(flags)
        configure()
    }
    startActivity(intent)
    if (finishCurrent) finish()
}

inline fun <reified T : Activity> Context.launchIntent(
    launcher: ActivityResultLauncher<Intent>,
    noinline configure: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java).apply(configure)
    launcher.launch(intent)
}

inline fun <T : Activity> Context.launchIntent(
    launcher: ActivityResultLauncher<Intent>,
    target: Class<T>,
    configure: Intent.() -> Unit
) {
    val intent = Intent(this, target).apply(configure)
    launcher.launch(intent)
}

inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getParcelableExtra(key, T::class.java)
    else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key)
    }
}

inline fun <reified T : Parcelable> Intent.getParcelableArrayListExtraCompat(key: String): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableArrayListExtra(key)
    }
}