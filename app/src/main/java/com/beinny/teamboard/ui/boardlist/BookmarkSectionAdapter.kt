package com.beinny.teamboard.ui.boardlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beinny.teamboard.data.model.Board
import com.beinny.teamboard.databinding.ItemBookmarkSectionBinding
import com.beinny.teamboard.data.model.BookmarkedBoards

class BookmarkSectionAdapter(
    private var onClick: (board: Board) -> Unit
) : ListAdapter<BookmarkedBoards, BookmarkSectionAdapter.BookmarkSectionViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkSectionViewHolder {
        val binding = ItemBookmarkSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookmarkSectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkSectionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookmarkSectionViewHolder(private val binding: ItemBookmarkSectionBinding) : RecyclerView.ViewHolder(binding.root) {
        private val nestedAdapter = BookmarkedBoardsAdapter( onClick )

        init {
            binding.rvCategorySection.adapter = nestedAdapter
        }

        fun bind(boardList: BookmarkedBoards) {
            nestedAdapter.submitList(boardList.bookmarkedBoards)
        }
    }
}

class DiffCallback : DiffUtil.ItemCallback<BookmarkedBoards>() {
    override fun areItemsTheSame(oldItem: BookmarkedBoards, newItem: BookmarkedBoards): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: BookmarkedBoards, newItem: BookmarkedBoards): Boolean {
        return oldItem == newItem
    }
}