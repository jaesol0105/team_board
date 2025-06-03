package com.beinny.teamboard.ui.main

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class ActivityResultHelper(
    lifecycleOwner: ComponentActivity,
    private val onCreateBoardResult: (Intent?) -> Unit,
    private val onMyProfileResult: (Intent?) -> Unit
) {
    val createBoardLauncher = lifecycleOwner.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onCreateBoardResult(result.data)
        }
    }

    val myProfileLauncher = lifecycleOwner.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onMyProfileResult(result.data)
        }
    }
}