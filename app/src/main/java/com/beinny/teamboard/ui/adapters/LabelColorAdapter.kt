package com.beinny.teamboard.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beinny.teamboard.databinding.ItemCardSelectedMemberBinding
import com.beinny.teamboard.databinding.ItemLabelColorBinding
import com.beinny.teamboard.models.SelectedMembers

class LabelColorAdapter(
    private val context: Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String
) : RecyclerView.Adapter<LabelColorAdapter.LabelColorHolder>() {

    private lateinit var binding : ItemLabelColorBinding
    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelColorHolder {
        binding = ItemLabelColorBinding.inflate(LayoutInflater.from(context),parent,false)
        return LabelColorHolder(binding)
    }

    override fun onBindViewHolder(holder: LabelColorHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnItemClickListener {
        fun onClick(position: Int, color: String)
    }

    inner class LabelColorHolder(private val binding: ItemLabelColorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(color: String) {
            binding.viewMain.setBackgroundColor(Color.parseColor(color))

            if (color == mSelectedColor) {
                binding.ivSelectedColor.visibility = View.VISIBLE
            } else {
                binding.ivSelectedColor.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onItemClickListener?.onClick(position, color)
            }
        }
    }
}
