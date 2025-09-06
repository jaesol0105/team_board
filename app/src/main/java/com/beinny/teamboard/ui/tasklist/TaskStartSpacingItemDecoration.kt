package com.beinny.teamboard.ui.tasklist

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class TaskStartSpacingItemDecoration(
    private val startSpacingPx: Int
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == 0) {
            outRect.left = startSpacingPx  // 첫 번째 아이템만 왼쪽 간격
        } else {
            outRect.left = 0
        }
    }
}