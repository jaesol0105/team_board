package com.beinny.teamboard.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beinny.teamboard.databinding.ItemCardSelectedMemberBinding
import com.beinny.teamboard.models.SelectedMembers

/** @param assignMembers CardDetailsActivity에선 true, CardAdapter에선 false로 생성한다.
TaskListActivity에선 멤버를 추가할 수 없게 하기 위함 */
open class CardMemberListAdapter(
    private val context: Context,
    private var list: ArrayList<SelectedMembers>,
    private val assignMembers: Boolean
) : RecyclerView.Adapter<CardMemberListAdapter.CardSelectedMemberHolder>() {

    private lateinit var binding: ItemCardSelectedMemberBinding
    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardSelectedMemberHolder {
        binding = ItemCardSelectedMemberBinding.inflate(LayoutInflater.from(context),parent,false)
        return CardSelectedMemberHolder(binding)
    }

    override fun onBindViewHolder(holder: CardSelectedMemberHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick()
    }

    inner class CardSelectedMemberHolder(private val binding: ItemCardSelectedMemberBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        fun bind(selectedMembers: SelectedMembers) {
            binding.selectedMembers = selectedMembers

            if (position == list.size - 1 && assignMembers) {
                binding.ivAddMember.visibility = View.VISIBLE
            } else {
                if (assignMembers)
                    binding.ivSelectedMemberImage.visibility = View.VISIBLE
                else
                    binding.ivSelectedMemberSmallImage.visibility = View.VISIBLE
            }
        }

        override fun onClick(v: View?) {
            if (onClickListener != null) {
                onClickListener!!.onClick()
            }
        }
    }
}