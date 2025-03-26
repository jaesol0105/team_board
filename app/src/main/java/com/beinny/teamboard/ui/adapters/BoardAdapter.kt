package com.beinny.teamboard.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beinny.teamboard.databinding.ItemBoardBinding
import com.beinny.teamboard.databinding.ItemBoardHeaderBinding
import com.beinny.teamboard.models.Board

open class BoardAdapter(
    private var list: ArrayList<Board>,
    private val listener: CallBacks
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var binding: ItemBoardBinding
    private lateinit var headerBinding: ItemBoardHeaderBinding
    private var onClickListener: OnClickListener? = null

    private val TYPE_HEADER = 0

    /** [북마크 등록 및 해제 : BoardListFragment()에서 구현] */
    interface CallBacks {
        fun bookmarkIconClick(board: Board)
    }

    override fun getItemCount(): Int = list.size + 1 // 헤더 포함해서 1 추가

    /** [아이템에 고유한 타입(포지션)을 부여 - 중복 이미지 오류 해결] */
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                headerBinding = ItemBoardHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(headerBinding)
            }
            else -> {
                binding = ItemBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BoardHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // TODO : payload 사용을 위한 구조임, 더 깔끔한 방법은? 일단 동작 안함
        onBindViewHolder(holder, position, emptyList())
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        when (getItemViewType(position)) {
            TYPE_HEADER -> {
                (holder as HeaderViewHolder).bind("보드목록")
            }
            else -> {
                /** payloads가 있으면 특정 부분만 업데이트 */
                if (payloads.isNotEmpty()) {
                    if (payloads.contains("bookmark_changed")) {
                        (holder as BoardHolder).updateBookmarkUI(list[position - 1].bookmarked)
                    }
                } else {
                    (holder as BoardHolder).bind(list[position - 1], position - 1)
                }
            }
        }
    }

    /** [북마크 아이콘 UI 갱신] */
    fun BoardHolder.updateBookmarkUI(isBookmarked: Boolean) {
        binding.ivItemBoardBookmarkOn.visibility = if (isBookmarked) View.VISIBLE else View.GONE
        binding.ivItemBoardBookmarkOff.visibility = if (isBookmarked) View.GONE else View.VISIBLE
        binding.executePendingBindings()
    }

    inner class HeaderViewHolder(private val binding: ItemBoardHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title:String) {
            binding.headerTitle.text = title
        }
    }

    inner class BoardHolder(private val binding: ItemBoardBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        fun bind(board: Board, position: Int) {
            binding.board = board

            /** 마지막 item 구분선 제거 */
            if (position == list.size - 1)
                binding.viewDivider.visibility = View.GONE

            // TODO : 일단은 보드 이미지에 리스너 연결, 나중에 몸통으로 바꿔주기
            binding.ivItemBoardImage.setOnClickListener(this)

            /** 북마크 아이콘 리스너 */
            binding.ivItemBoardBookmarkOn.setOnClickListener {
                clickBookmarkAndUiUpdate(position, board)
            }
            binding.ivItemBoardBookmarkOff.setOnClickListener {
                clickBookmarkAndUiUpdate(position, board)
            }
        }

        override fun onClick(v: View?) {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, binding.board!!)
            }

        }

        private fun clickBookmarkAndUiUpdate(position: Int, board: Board){
            listener.bookmarkIconClick(board)
            list[position].bookmarked = !list[position].bookmarked
            //notifyItemChanged(position+1, "bookmark_changed") // 특정 뷰만 갱신
            notifyItemChanged(position+1)
        }
    }

    /** [외부에서 onClickListner를 전달 받는다] */
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }
}
