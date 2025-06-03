package com.beinny.teamboard.ui.member

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.beinny.teamboard.R
import com.beinny.teamboard.ui.shared.MemberAdapter
import com.beinny.teamboard.databinding.ActivityMembersBinding
import com.beinny.teamboard.databinding.DialogSearchMemberBinding
import com.beinny.teamboard.data.model.Board
import com.beinny.teamboard.data.model.User
import com.beinny.teamboard.ui.base.BaseActivity
import com.beinny.teamboard.ui.common.repeatOnStarted
import com.beinny.teamboard.ui.factory.ViewModelFactory
import com.beinny.teamboard.ui.state.UiState
import com.beinny.teamboard.utils.Constants
import kotlinx.coroutines.flow.collectLatest

class MembersActivity : BaseActivity() {
    private lateinit var binding: ActivityMembersBinding
    private val viewModel: MembersViewModel by viewModels { ViewModelFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getParcelableExtra<Board>(Constants.EXTRA_BOARD_DETAIL)?.let {
            viewModel.setBoard(it)
        }

        initBinding()
        setupActionBar(binding.toolbarMembersActivity,getString(R.string.members_menu_id))
        setupObservers()

        createTransitionAnimation()
    }

    override fun finish() {
        super.finish()
        exitTransitionAnimation()
    }

    private fun initBinding() {
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupObservers() {
        repeatOnStarted {
            viewModel.members.collectLatest { members ->
                setupMembersList(ArrayList(members))
            }
        }

        repeatOnStarted {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> showProgressDialog(getString(R.string.please_wait))
                    is UiState.Success -> {
                        hideProgressDialog()
                    }
                    is UiState.Error -> {
                        hideProgressDialog()
                        showErrorSnackBar(state.message)
                    }
                    else -> Unit
                }
            }
        }
    }

    fun setupMembersList(members: ArrayList<User>) {
        binding.rvMembersList.apply {
            layoutManager = LinearLayoutManager(this@MembersActivity)
            setHasFixedSize(true)
            adapter = MemberAdapter(this@MembersActivity, ArrayList(members)) { _, _ -> }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_member -> {
                showSearchMemberDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /** 이메일을 통해서 사용자를 검색하는 대화상자를 출력 */
    private fun showSearchMemberDialog() {
        val dialog = Dialog(this, R.style.transparentDialog)

        val dialogBinding = DialogSearchMemberBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvAdd.setOnClickListener {
            val email = dialogBinding.etEmailSearchMember.text.toString()

            if (email.isNotEmpty()) {
                dialog.dismiss()
                viewModel.searchMemberByEmail(email)
            } else {
                showErrorSnackBar(getString(R.string.please_input_email))
            }
        }
        dialogBinding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    /** 백 프레스 : 변경사항이 있으면 RESULT_OK */
    override fun onBackPressed() {
        if (viewModel.anyChangesDone) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }
}