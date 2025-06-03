package com.beinny.teamboard.ui.tasklist

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.beinny.teamboard.data.model.Card
import java.util.*
import kotlin.collections.ArrayList

class CardItemTouchHelperCallback(
    private val cards: ArrayList<Card>,
    private val adapter: RecyclerView.Adapter<*>,
    private val onReordered: (ArrayList<Card>) -> Unit
) : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

    private var fromPos = -1
    private var toPos = -1

    /** 사용자가 아이템을 드래그하여 이동할 때 호출
     * @param dragged 드래그 중인 아이템의 뷰 홀더
     * @param target 드래그한 아이템이 놓이게 될 목표 위치의 뷰 홀더 */
    override fun onMove(
        recyclerView: RecyclerView,
        dragged: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        fromPos = dragged.bindingAdapterPosition // 드래그 중인 아이템의 현재 위치
        toPos = target.bindingAdapterPosition // 드래그한 아이템이 이동할 목표 위치

        if (fromPos != toPos) {
            Collections.swap(cards, fromPos, toPos) // 리스트에서 드래그한 위치와 목표 위치의 아이템을 교체한다
            adapter.notifyItemMoved(fromPos, toPos) // 어댑터에게 변경 사항 알리기 (UI 갱신)
        }

        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        // 드래그 동작이 실제로 발생했는가? & 위치가 변경되었는가?
        if (fromPos != -1 && toPos != -1 && fromPos != toPos) {
            onReordered(cards)
        }
        // 포지션 초기화
        fromPos = -1
        toPos = -1
    }
}
