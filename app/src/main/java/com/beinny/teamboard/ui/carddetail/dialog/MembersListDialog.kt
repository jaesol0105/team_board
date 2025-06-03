package com.beinny.teamboard.ui.carddetail.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.beinny.teamboard.R
import com.beinny.teamboard.ui.shared.MemberAdapter
import com.beinny.teamboard.databinding.DialogListBinding
import com.beinny.teamboard.data.model.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MembersListDialog(
    private var list: ArrayList<User>,
    private val title: String = "",
    private val onItemSelected: (User, String) -> Unit
) : BottomSheetDialogFragment() {
    private lateinit var binding : DialogListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        /** 투명 배경 스타일 적용 */
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
            val adapter = MemberAdapter(requireContext(), list) { user, action ->
                dismiss()
                onItemSelected(user, action)
            }

            binding.rvList.layoutManager = LinearLayoutManager(requireContext())
            binding.rvList.adapter = adapter
        }
    }
}