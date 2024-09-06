package com.beinny.teamboard.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.beinny.teamboard.adapters.MemberAdapter
import com.beinny.teamboard.databinding.DialogListBinding
import com.beinny.teamboard.models.User

abstract class MembersListDialog(
    context: Context,
    private var list: ArrayList<User>,
    private val title: String = ""
) : Dialog(context) {
    private var adapter: MemberAdapter? = null
    private lateinit var binding : DialogListBinding

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

        if (list.size > 0) {

            binding.rvList.layoutManager = LinearLayoutManager(context)
            adapter = MemberAdapter(context, list)
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

    protected abstract fun onItemSelected(user: User, action:String)
}