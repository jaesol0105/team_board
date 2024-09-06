package com.beinny.teamboard.ui.common

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("app:createdBy")
fun applyMemoCountFormat(view: TextView, createdBy:String?){
    view.text = "Created By : " + createdBy //TODO :문자열처리
}