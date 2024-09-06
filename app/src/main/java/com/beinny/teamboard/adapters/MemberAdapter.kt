package com.beinny.teamboard.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ItemLabelColorBinding
import com.beinny.teamboard.databinding.ItemMemberBinding
import com.beinny.teamboard.dialogs.MembersListDialog
import com.beinny.teamboard.models.User
import com.beinny.teamboard.ui.CardDetailsActivity
import com.beinny.teamboard.utils.Constants
import com.bumptech.glide.Glide

open class MemberAdapter(
    private val context: Context,
    private var list: ArrayList<User>
) : RecyclerView.Adapter<MemberAdapter.MemberHolder>() {

    private lateinit var binding: ItemMemberBinding
    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberHolder {
        binding = ItemMemberBinding.inflate(LayoutInflater.from(context), parent, false)
        return MemberHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, user: User, action: String)
    }

    inner class MemberHolder(private val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.user = user

            itemView.setOnClickListener {
                if (user.selected) {
                    onClickListener?.onClick(position, user, Constants.UN_SELECT)
                } else {
                    onClickListener?.onClick(position, user, Constants.SELECT)
                }
            }

        }
    }
}
