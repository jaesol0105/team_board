package com.beinny.teamboard.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.beinny.teamboard.R
import com.beinny.teamboard.ui.adapters.MemberAdapter
import com.beinny.teamboard.databinding.DialogListBinding
import com.beinny.teamboard.models.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MembersListDialog(
    private var list: ArrayList<User>,
    private val title: String = "",
    private val onItemSelected: (User, String) -> Unit
) : BottomSheetDialogFragment() {
    private var adapter: MemberAdapter? = null
    private lateinit var binding : DialogListBinding

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
        if (list.size > 0) {
            binding.rvList.layoutManager = LinearLayoutManager(requireContext())
            adapter = MemberAdapter(requireContext(), list)
            binding.rvList.adapter = adapter

            adapter!!.setOnClickListener(object :
                MemberAdapter.OnClickListener {
                override fun onClick(position: Int, user: User, action:String) {
                    dismiss()
                    onItemSelected(user, action)
                }
            })
        }
    }
}