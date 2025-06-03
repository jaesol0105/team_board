package com.beinny.teamboard.ui.carddetail.dialog

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beinny.teamboard.databinding.ItemLabelColorBinding
import com.beinny.teamboard.ui.common.showIf

class LabelColorAdapter(
    private val context: Context,
    private var list: List<String>,
    private val mSelectedColor: String,
    private val onLabelColorClick: (color : String) -> Unit
) : RecyclerView.Adapter<LabelColorAdapter.LabelColorHolder>() {

    private lateinit var binding : ItemLabelColorBinding

    inner class LabelColorHolder(private val binding: ItemLabelColorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(color: String) {
            binding.viewMain.setBackgroundColor(Color.parseColor(color))
            binding.ivSelectedColor.showIf(color == mSelectedColor)

            itemView.setOnClickListener {
                onLabelColorClick(color)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelColorHolder {
        binding = ItemLabelColorBinding.inflate(LayoutInflater.from(context),parent,false)
        return LabelColorHolder(binding)
    }

    override fun onBindViewHolder(holder: LabelColorHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size
}
