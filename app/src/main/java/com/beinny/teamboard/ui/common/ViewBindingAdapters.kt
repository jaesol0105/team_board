package com.beinny.teamboard.ui.common

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("app:visibility")
fun setVisibility(view: View, isVisible: Boolean) {
    view.visibility = if (isVisible) View.VISIBLE else View.GONE
}

@BindingAdapter("app:visibilityOff")
fun setVisibilityOff(view: View, isVisible: Boolean) {
    view.visibility = if (isVisible) View.GONE else View.VISIBLE
}

@BindingAdapter("app:invisibleOff")
fun setInvisibleOff(view: View, isVisible: Boolean) {
    view.visibility = if (isVisible) View.INVISIBLE else View.VISIBLE
}

@BindingAdapter("app:visibility")
fun setVisibility(view: View, boardsListSize: Int = 0) {
    when (view) {
        is RecyclerView -> {
            view.visibility = if (boardsListSize > 0) View.VISIBLE else View.GONE
        }
        is TextView -> {
            view.visibility = if (boardsListSize > 0) View.GONE else View.VISIBLE
        }
    }
}