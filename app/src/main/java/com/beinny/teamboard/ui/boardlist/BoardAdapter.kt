package com.beinny.teamboard.ui.boardlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beinny.teamboard.databinding.ItemBoardBinding
import com.beinny.teamboard.databinding.ItemBoardHeaderBinding
import com.beinny.teamboard.data.model.Board

open class BoardAdapter(
    private var list: ArrayList<Board>,
    private val headerTitle: String,
    private val listener: BoardListListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private lateinit var binding: ItemBoardBinding
    private lateinit var headerBinding: ItemBoardHeaderBinding

    companion object {
        private const val TYPE_HEADER = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                headerBinding = ItemBoardHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(headerBinding)
            }
            else -> {
                binding = ItemBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BoardViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_HEADER -> (holder as HeaderViewHolder).bind(headerTitle)
            else -> {
                val board = list[position - 1]
                (holder as BoardViewHolder).bind(board, position - 1)
            }
        }
    }

    // 헤더 포함해서 1 추가한다
    override fun getItemCount(): Int = list.size + 1

    // 아이템에 고유한 타입(포지션)을 부여한다 - 중복 이미지 오류 해결
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else position
    }

    inner class HeaderViewHolder(private val binding: ItemBoardHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title:String) {
            binding.headerTitle.text = title
        }
    }

    inner class BoardViewHolder(private val binding: ItemBoardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(board: Board, position: Int) {
            binding.board = board

            // 마지막 아이템 구분선 제거
            if (position == list.size - 1)
                binding.viewDivider.visibility = View.GONE

            // 보드 클릭 리스너
            binding.ivItemBoardImage.setOnClickListener {
                listener.onBoardClick(board)
            }

            // 북마크 리스너
            binding.ivItemBoardBookmarkOn.setOnClickListener {
                onBookmarkClick(position, board)
            }
            binding.ivItemBoardBookmarkOff.setOnClickListener {
                onBookmarkClick(position, board)
            }
        }

        /** 북마크 상태를 변경하고 UI에 반영 */
        private fun onBookmarkClick(position: Int, board: Board){
            board.bookmarked = !board.bookmarked // UI 변경
            notifyItemChanged(position + 1) // 화면 갱신
            listener.onBookmarkClick(board) // 리스너로 전달
        }
    }
}
