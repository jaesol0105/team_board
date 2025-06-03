package com.beinny.teamboard.ui.tasklist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.beinny.teamboard.R
import com.beinny.teamboard.data.model.Card
import com.beinny.teamboard.data.model.Task
import com.beinny.teamboard.databinding.ActivityTaskListBinding
import com.beinny.teamboard.ui.base.BaseActivity
import com.beinny.teamboard.ui.carddetail.CardDetailsActivity
import com.beinny.teamboard.ui.common.launchIntent
import com.beinny.teamboard.ui.common.repeatOnStarted
import com.beinny.teamboard.ui.factory.ViewModelFactory
import com.beinny.teamboard.ui.member.MembersActivity
import com.beinny.teamboard.ui.state.UiState
import com.beinny.teamboard.utils.Constants
import kotlinx.coroutines.flow.collectLatest

class TaskListActivity : BaseActivity(), TaskListListener {
    private lateinit var mBoardDocumentId: String // 보드의 document id

    private lateinit var binding: ActivityTaskListBinding
    private val viewModel: TaskListViewModel by viewModels { ViewModelFactory(applicationContext) }
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBoardDocumentId = intent.getStringExtra(Constants.EXTRA_DOCUMENT_ID) ?: ""

        initBinding()
        setupObservers()
        setupLaunchers()
        viewModel.loadBoard(mBoardDocumentId)

        createTransitionAnimation()
    }

    override fun finish() {
        super.finish()
        exitTransitionAnimation()
    }

    private fun initBinding() {
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupObservers() {
        repeatOnStarted {
            viewModel.boardWithMembers.collectLatest { (board, members) ->
                setupActionBar(binding.toolbarTaskListActivity, board.name)

                // 마지막에 위치할 '새 리스트' 버튼을 추가한다
                val taskList = board.taskList.toMutableList().apply {
                    add(Task(getString(R.string.task_add_list)))
                }
                val taskListAdapter = TaskAdapter(
                    context = this@TaskListActivity,
                    tasks = ArrayList(taskList),
                    members = viewModel.members,
                    listener = this@TaskListActivity)
                binding.rvTaskList.apply{
                    layoutManager = LinearLayoutManager(this@TaskListActivity, LinearLayoutManager.HORIZONTAL, false)
                    setHasFixedSize(true)
                    adapter = taskListAdapter
                }
            }
        }

        repeatOnStarted {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> showProgressDialog(getString(R.string.please_wait))
                    is UiState.Success, UiState.Idle -> hideProgressDialog()
                    is UiState.Error -> {
                        hideProgressDialog()
                        Toast.makeText(this@TaskListActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupLaunchers() {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            /** CardDetailsActivity, MembersActivity 동시에 처리 */
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.loadBoard(mBoardDocumentId)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_members) {
            launchIntent<MembersActivity>(launcher) {
                putExtra(Constants.EXTRA_BOARD_DETAIL, viewModel.board)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /** 새 태스크를 생성 */
    override fun onCreateTaskList(name: String) {
        viewModel.createTaskList(name)
    }

    /** 태스크 이름 변경 */
    override fun onEditTaskList(position: Int, name: String, task: Task) {
        viewModel.editTaskList(position, name, task)
    }

    /** 태스크 삭제 */
    override fun onDeleteTaskList(position: Int) {
        viewModel.deleteTaskList(position)
    }

    /** 새 카드 추가 */
    override fun onAddCard(position: Int, cardName: String) {
        viewModel.addCardToTaskList(position, cardName)
    }

    /** CardDetailsActivity 실행 */
    override fun onCardClick(taskPosition: Int, cardPosition: Int) {
        launchIntent<CardDetailsActivity>(launcher) {
            putExtra(Constants.EXTRA_BOARD_DETAIL, viewModel.board)
            putExtra(Constants.EXTRA_TASK_ITEM_POSITION, taskPosition)
            putExtra(Constants.EXTRA_CARD_ITEM_POSITION, cardPosition)
            putExtra(Constants.EXTRA_BOARD_MEMBERS, ArrayList(viewModel.members))
        }
    }

    /** 카드 순서 재배치 */
    override fun onCardsReordered(taskPosition: Int, cards: ArrayList<Card>) {
        viewModel.reorderCardsInTaskList(taskPosition, cards)
    }
}