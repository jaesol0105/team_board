package com.beinny.teamboard.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.beinny.teamboard.R
import com.beinny.teamboard.ui.adapters.LabelColorAdapter
import com.beinny.teamboard.databinding.DialogListBinding
import com.beinny.teamboard.models.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LabelColorDialog(
    private var list: ArrayList<String>,
    private val title: String = "",
    private val mSelectedColor: String = "",
    private val onItemSelected: (String) -> Unit
) : BottomSheetDialogFragment() {
    private lateinit var binding : DialogListBinding
    private var adapter: LabelColorAdapter? = null

    /** [투명 배경 스타일 적용] */
    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        adapter = LabelColorAdapter(requireContext(), list, mSelectedColor)
        binding.rvList.adapter = adapter

        adapter!!.onItemClickListener = object : LabelColorAdapter.OnItemClickListener {
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }

}