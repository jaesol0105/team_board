package com.beinny.teamboard.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.beinny.teamboard.adapters.LabelColorAdapter
import com.beinny.teamboard.databinding.DialogListBinding

abstract class LabelColorDialog(
    context: Context,
    private var list: ArrayList<String>,
    private val title: String = "",
    private val mSelectedColor: String = ""
) : Dialog(context) {
    private lateinit var binding : DialogListBinding
    private var adapter: LabelColorAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())

        binding = DialogListBinding.inflate(LayoutInflater.from(context))

        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(binding)
    }

    private fun setUpRecyclerView(binding: DialogListBinding) {
        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorAdapter(context, list, mSelectedColor)
        binding.rvList.adapter = adapter

        adapter!!.onItemClickListener = object : LabelColorAdapter.OnItemClickListener {

            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }

    protected abstract fun onItemSelected(color: String)
}