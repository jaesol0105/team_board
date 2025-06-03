package com.beinny.teamboard.ui.common

import android.content.Context
import androidx.appcompat.app.AlertDialog

fun Context.showConfirmationDialog(
    message: String,
    iconResId: Int = android.R.drawable.ic_dialog_alert,
    positiveButtonText: String,
    negativeButtonText: String,
    onConfirm: () -> Unit
) {
    AlertDialog.Builder(this).apply {
        setMessage(message)
        setIcon(iconResId)
        setCancelable(false)
        setPositiveButton(positiveButtonText) { dialog, _ ->
            dialog.dismiss()
            onConfirm()
        }
        setNegativeButton(negativeButtonText) { dialog, _ ->
            dialog.dismiss()
        }
    }.create().show()
}