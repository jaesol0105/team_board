package com.beinny.teamboard.ui.common.binding

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("app:visibility")
fun setVisibility(view: View, isVisible: Boolean) {
    view.visibility = if (isVisible) View.VISIBLE else View.GONE
}

@BindingAdapter("app:hideIf")
fun setVisibilityOff(view: View, isVisible: Boolean) {
    view.visibility = if (isVisible) View.GONE else View.VISIBLE
}

@BindingAdapter("app:fadeIf")
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

@BindingAdapter("app:labelColor")
fun View.setLabelColor(colorString: String?) {
    if (!colorString.isNullOrEmpty()) {
        visibility = View.VISIBLE
        setBackgroundColor(Color.parseColor(colorString))
    } else {
        visibility = View.GONE
    }
}