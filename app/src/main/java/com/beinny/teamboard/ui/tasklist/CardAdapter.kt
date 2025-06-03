package com.beinny.teamboard.ui.tasklist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beinny.teamboard.data.model.Card
import com.beinny.teamboard.databinding.ItemCardBinding
import com.beinny.teamboard.data.model.SelectedMembers
import com.beinny.teamboard.data.model.User
import com.beinny.teamboard.ui.shared.CardMemberListAdapter

open class CardAdapter(
    private val context: Context,
    private val cards: ArrayList<Card>,
    private val members: List<User>,
    private val onCardClick: (position : Int) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardHolder>() {
    private lateinit var binding: ItemCardBinding

    inner class CardHolder(private val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        fun bind(card: Card, context: Context) {
            binding.card = card
            // 멤버 목록
            if (members.isNotEmpty()) {
                val selectedMembersList = members.filter { card.assignedTo.contains(it.id) }
                    .map { SelectedMembers(it.id, it.image) }

                // 카드의 멤버가 없거나 본인 1명밖에 없을경우, 출력하지 않음
                if (selectedMembersList.size > 1 || (selectedMembersList.size == 1 && selectedMembersList[0].id != card.createdBy)) {
                    binding.rvCardSelectedMembersList.visibility = View.VISIBLE
                    binding.rvCardSelectedMembersList.layoutManager = GridLayoutManager(context, 9)
                    val memberAdapter = CardMemberListAdapter(context, ArrayList(selectedMembersList), false) {
                        onCardClick(bindingAdapterPosition)
                    }
                    binding.rvCardSelectedMembersList.adapter = memberAdapter
                } else {
                    binding.rvCardSelectedMembersList.visibility = View.GONE
                }
            }
        }

        override fun onClick(v: View?) {
            onCardClick(position)
        }
    }

    override fun getItemCount(): Int = cards.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardHolder {
        binding = ItemCardBinding.inflate(LayoutInflater.from(context), parent, false)
        return CardHolder(binding)
    }

    override fun onBindViewHolder(holder: CardHolder, position: Int) {
        holder.bind(cards[position], context)
    }
}