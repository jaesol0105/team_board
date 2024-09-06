package com.beinny.teamboard.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ItemBoardBinding
import com.beinny.teamboard.databinding.ItemCardBinding
import com.beinny.teamboard.models.Board
import com.beinny.teamboard.models.Card
import com.beinny.teamboard.models.SelectedMembers
import com.beinny.teamboard.ui.TaskListActivity
import com.bumptech.glide.Glide

open class CardAdapter(
    private val context: Context,
    private var list: ArrayList<Card>
) : RecyclerView.Adapter<CardAdapter.CardHolder>() {
    private var onClickListener: OnClickListener? = null
    private lateinit var binding: ItemCardBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHolder {
        binding = ItemCardBinding.inflate(LayoutInflater.from(context), parent, false)
        return CardHolder(binding)
    }

    override fun onBindViewHolder(holder: CardHolder, position: Int) {
        holder.bind(list[position], context)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int)
    }

    inner class CardHolder(private val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        fun bind(card: Card, context: Context) {
            if(card.labelColor.isNotEmpty()){
                binding.viewLabelColor.visibility = View.VISIBLE
                binding.viewLabelColor.setBackgroundColor(Color.parseColor(card.labelColor))
            } else {
                binding.viewLabelColor.visibility = View.GONE
            }
            binding.tvCardName.text = card.name

            // 멤버 목록
            if ((context as TaskListActivity).mAssignedMembersDetailList.size > 0) {
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

                for (i in context.mAssignedMembersDetailList.indices) {
                    for (j in card.assignedTo) {
                        if (context.mAssignedMembersDetailList[i].id == j) {
                            val selectedMember = SelectedMembers(
                                context.mAssignedMembersDetailList[i].id,
                                context.mAssignedMembersDetailList[i].image
                            )

                            selectedMembersList.add(selectedMember)
                        }
                    }
                }

                if (selectedMembersList.size > 0) {
                    // 카드의 멤버가 본인 1명밖에 없을경우, 출력하지 않음
                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == card.createdBy) {
                        binding.rvCardSelectedMembersList.visibility = View.GONE
                    } else {
                        binding.rvCardSelectedMembersList.visibility = View.VISIBLE
                        binding.rvCardSelectedMembersList.layoutManager = GridLayoutManager(context, 4)
                        val adapter = CardMemberListAdapter(context, selectedMembersList, false)
                        binding.rvCardSelectedMembersList.adapter = adapter
                        adapter.setOnClickListener(object :
                            CardMemberListAdapter.OnClickListener {
                            override fun onClick() {
                                if (onClickListener != null) {
                                    onClickListener!!.onClick(position)
                                }
                            }
                        })
                    }
                } else {
                    binding.rvCardSelectedMembersList.visibility = View.GONE
                }
            }
        }

        override fun onClick(v: View?) {
            if (onClickListener != null) {
                onClickListener!!.onClick(position)
            }
        }
    }
}
