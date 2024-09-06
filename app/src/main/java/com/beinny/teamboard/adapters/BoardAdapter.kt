package com.beinny.teamboard.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ItemBoardBinding
import com.beinny.teamboard.models.Board
import com.bumptech.glide.Glide

open class BoardAdapter(
    private val context: Context,
    private var list: ArrayList<Board>
) : RecyclerView.Adapter<BoardAdapter.BoardHolder>() {
    private lateinit var binding: ItemBoardBinding
    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardHolder {
        binding = ItemBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BoardHolder(binding)
    }

    override fun onBindViewHolder(holder: BoardHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class BoardHolder(private val binding: ItemBoardBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        fun bind(board: Board) {
            binding.board = board
        }

        override fun onClick(v: View?) {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, binding.board!!)
            }

        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }

}
