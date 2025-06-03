package com.beinny.teamboard.ui.carddetail

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.beinny.teamboard.R
import com.beinny.teamboard.ui.shared.CardMemberListAdapter
import com.beinny.teamboard.databinding.ActivityCardDetailsBinding
import com.beinny.teamboard.ui.carddetail.dialog.DateTimePickerDialog
import com.beinny.teamboard.ui.carddetail.dialog.LabelColorDialog
import com.beinny.teamboard.ui.carddetail.dialog.MembersListDialog
import com.beinny.teamboard.data.model.CardParams
import com.beinny.teamboard.data.model.SelectedMembers
import com.beinny.teamboard.ui.base.BaseActivity
import com.beinny.teamboard.ui.common.getParcelableArrayListExtraCompat
import com.beinny.teamboard.ui.common.getParcelableExtraCompat
import com.beinny.teamboard.ui.common.repeatOnStarted
import com.beinny.teamboard.ui.common.showConfirmationDialog
import com.beinny.teamboard.ui.common.toggleWith
import com.beinny.teamboard.ui.factory.ViewModelFactory
import com.beinny.teamboard.ui.state.UiState
import com.beinny.teamboard.utils.Constants
import com.beinny.teamboard.utils.Constants.DIALOG_DATE_TIME
import com.beinny.teamboard.utils.Constants.DIALOG_LABEL_COLOR
import com.beinny.teamboard.utils.Constants.DIALOG_MEMBER_LIST
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date

class CardDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityCardDetailsBinding
    private val viewModel: CardDetailsViewModel by viewModels { ViewModelFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getIntentData()

        initBinding()
        setupListeners()
        setupObservers()

        setupSelectedMembersList()

        createTransitionAnimation()
    }

    override fun finish() {
        super.finish()
        exitTransitionAnimation()
    }

    /** Intent key로 부터 데이터 불러오기 */
    private fun getIntentData() {
        viewModel.setCardParams(
            CardParams(
                board = intent.getParcelableExtraCompat(Constants.EXTRA_BOARD_DETAIL) ?: return,
                taskPosition = intent.getIntExtra(Constants.EXTRA_TASK_ITEM_POSITION, -1),
                cardPosition = intent.getIntExtra(Constants.EXTRA_CARD_ITEM_POSITION, -1),
                members = intent.getParcelableArrayListExtraCompat(Constants.EXTRA_BOARD_MEMBERS) ?: arrayListOf()
            )
        )
    }

    private fun initBinding() {
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        setContentView(binding.root)
    }

    private fun setupListeners() {
        /** 완료 버튼 리스너 */
        binding.btnUpdateCardDetails.setOnClickListener {
            val name = binding.etNameCardDetails.text.toString()
            if (name.isNotBlank()) {
                viewModel.updateCard(name)
            } else {
                Toast.makeText(this@CardDetailsActivity, "작업 명을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        /** 라벨 색상 선택 리스너 */
        binding.tvSelectLabelColor.setOnClickListener {
            showLabelColorDialog()
        }

        /** 카드 멤버 등록 리스너 */
        binding.tvSelectMembers.setOnClickListener {
            showMemberSelectionDialog()
        }

        /** 마감일 선택 리스너 */
        binding.tvSelectDueDate.setOnClickListener {
            showDateTimePickerDialog()
        }
    }

    private fun setupObservers() {
        // 액션바
        repeatOnStarted {
            viewModel.cardParams.collectLatest {
                setupActionBar(binding.toolbar, it?.card?.name)
                binding.cardName = it?.card?.name
            }
        }
        // 라벨
        repeatOnStarted {
            viewModel.selectedColor.collectLatest { color ->
                if (color.isNotBlank()) {
                    binding.tvSelectLabelColor.text = ""
                    binding.tvSelectLabelColor.setBackgroundColor(Color.parseColor(color))
                }
            }
        }
        // 마감일
        repeatOnStarted {
            viewModel.selectedDate.collectLatest { millis ->
                if (millis > 0) {
                    val formatted = SimpleDateFormat(Constants.DATE_FORMAT).format(Date(millis))
                    binding.tvSelectDueDate.text = formatted
                }
            }
        }
        // 완료 후 종료
        repeatOnStarted {
            viewModel.uiState.collectLatest { state ->
                if (state is UiState.Success) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }

    /** 라벨 색상을 선택하는 대화상자 출력 */
    private fun showLabelColorDialog() {
        val dialog = LabelColorDialog(viewModel.getColorOptions(), getString(R.string.str_select_label_color)) { selectedColor ->
            viewModel.setSelectedColor(selectedColor)
        }
        dialog.show(supportFragmentManager, DIALOG_LABEL_COLOR)
    }

    /** DateTimePicker 대화상자 출력 */
    private fun showDateTimePickerDialog() {
        val dialog = DateTimePickerDialog.newInstance(viewModel.selectedDate.value)
        dialog.show(supportFragmentManager, DIALOG_DATE_TIME)
    }

    /** 멤버를 선택하는 대화 상자 출력 */
    private fun showMemberSelectionDialog() {
        val params = viewModel.cardParams.value ?: return
        val card = params.board.taskList[params.taskPosition].cards[params.cardPosition]
        val members = params.members

        // 보드의 현재 멤버 목록 - 카드의 현재 멤버 목록 순회
        members.forEach { user ->
            user.selected = card.assignedTo.contains(user.id) // 카드의 멤버이면 체크
        }

        // 보드의 멤버 목록을 출력하는 대화상자 생성
        MembersListDialog(members, getString(R.string.str_select_member)) { user, action ->
            // 선택 & 선택해제 동작
            if (action == Constants.SELECT) {
                if (!card.assignedTo.contains(user.id)) card.assignedTo.add(user.id)
            } else {
                card.assignedTo.remove(user.id)
            }
            setupSelectedMembersList()
        }.show(supportFragmentManager, DIALOG_MEMBER_LIST)
    }

    /** 현재 선택된 멤버들을 recycler view에 갱신 */
    private fun setupSelectedMembersList() {
        val params = viewModel.cardParams.value ?: return
        val card = params.board.taskList[params.taskPosition].cards[params.cardPosition]
        // 현재 선택된 멤버 목록 인스턴스
        val selectedMembers = ArrayList(
            params.members.filter { card.assignedTo.contains(it.id) }
            .map { SelectedMembers(it.id, it.image) }
        )

        // 선택된 멤버가 1명 이상일 경우
        if (selectedMembers.isNotEmpty()) {
            selectedMembers.add(SelectedMembers("", "")) // '+' 버튼용 더미 (맨 뒤에 출력할 항목)
            binding.tvSelectMembers.toggleWith(binding.rvSelectedMembersList)
            binding.rvSelectedMembersList.layoutManager = GridLayoutManager(this, 6)
            binding.rvSelectedMembersList.adapter = CardMemberListAdapter(this, selectedMembers, true) {
                showMemberSelectionDialog()
            }
        } else {
            binding.rvSelectedMembersList.toggleWith(binding.tvSelectMembers)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /** 카드 삭제 경고 대화상자 출력 */
    private fun alertDialogForDeleteCard() {
        showConfirmationDialog(
            message = getString(R.string.confirmation_message_to_delete_card),
            positiveButtonText = getString(R.string.delete),
            negativeButtonText = getString(R.string.cancel),
            onConfirm = { viewModel.deleteCard() }
        )
    }
}