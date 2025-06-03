package com.beinny.teamboard.ui.shared

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beinny.teamboard.databinding.ItemMemberBinding
import com.beinny.teamboard.data.model.User
import com.beinny.teamboard.utils.Constants

open class MemberAdapter(
    private val context: Context,
    private var list: ArrayList<User>,
    private val onMemberClick : (user: User, action:String) -> Unit
) : RecyclerView.Adapter<MemberAdapter.MemberHolder>() {

    private lateinit var binding: ItemMemberBinding

    inner class MemberHolder(private val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.user = user

            itemView.setOnClickListener {
                if (user.selected) {
                    onMemberClick(user, Constants.UN_SELECT)
                } else {
                    onMemberClick(user, Constants.SELECT)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberHolder {
        binding = ItemMemberBinding.inflate(LayoutInflater.from(context), parent, false)
        return MemberHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size
}
