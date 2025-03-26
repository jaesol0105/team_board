package com.beinny.teamboard.ui.carddetails

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.beinny.teamboard.R
import com.beinny.teamboard.ui.adapters.CardMemberListAdapter
import com.beinny.teamboard.databinding.ActivityCardDetailsBinding
import com.beinny.teamboard.ui.dialogs.DateTimePickerDialog
import com.beinny.teamboard.ui.dialogs.LabelColorDialog
import com.beinny.teamboard.ui.dialogs.MembersListDialog
import com.beinny.teamboard.firebase.FirestoreClass
import com.beinny.teamboard.models.Board
import com.beinny.teamboard.models.Card
import com.beinny.teamboard.models.SelectedMembers
import com.beinny.teamboard.models.Task
import com.beinny.teamboard.models.User
import com.beinny.teamboard.ui.base.BaseActivity
import com.beinny.teamboard.ui.common.ViewModelFactory
import com.beinny.teamboard.utils.Constants
import com.beinny.teamboard.utils.Constants.DATE_FORMAT
import com.beinny.teamboard.utils.Constants.DIALOG_DATE
import java.text.SimpleDateFormat
import java.util.Date

class CardDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityCardDetailsBinding
    private val viewModel: CardDetailsViewModel by viewModels { ViewModelFactory(this) }

    private lateinit var mBoardDetails: Board // 보드 디테일 글로벌 변수
    private var mTaskListPosition: Int = -1 // task item position
    private var mCardPosition: Int = -1 // card item position
    private var mSelectedColor: String = "" // 카드 라벨 색상 선택을 위한 글로벌 변수
    private lateinit var mMembersDetailList: ArrayList<User> // 멤버 목록 글로벌 변수
    private var mSelectedDueDateMilliSeconds : Long = 0 // 마감 기한 글로벌 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // 날짜 선택
        viewModel.selectedDate.observe(
            this,
            Observer { date ->
                mSelectedDueDateMilliSeconds = date
                binding.tvSelectDueDate.text = SimpleDateFormat(DATE_FORMAT).format(Date(mSelectedDueDateMilliSeconds))
            }
        )

        getIntentData()
        setupActionBar()

        binding.etNameCardDetails.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        binding.etNameCardDetails.setSelection(binding.etNameCardDetails.text.toString().length) // 커서 위치

        /** [완료 버튼 리스너] */
        binding.btnUpdateCardDetails.setOnClickListener {
            if(binding.etNameCardDetails.text.toString().isNotEmpty()) {
                updateCardDetails()
            }else{
                Toast.makeText(this@CardDetailsActivity, "작업 명을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        /** [라벨 색상 선택 리스너 (textview)] */
        binding.tvSelectLabelColor.setOnClickListener {
            labelColorsListDialog()
        }

        /** [라벨 뷰 설정] */
        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if (mSelectedColor.isNotEmpty()) {
            setColor()
        }

        /** [멤버 선택 리스너 (textview)] */
        binding.tvSelectMembers.setOnClickListener {
            membersListDialog()
        }

        setupSelectedMembersList()

        mSelectedDueDateMilliSeconds =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate

        if (mSelectedDueDateMilliSeconds > 0) {
            //val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            //val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            //binding.tvSelectDueDate.text = selectedDate

            binding.tvSelectDueDate.text = SimpleDateFormat(DATE_FORMAT).format(Date(mSelectedDueDateMilliSeconds))
        }

        binding.tvSelectDueDate.setOnClickListener {
            showDatePicker()
        }

        createTransitionAnimation()
    }

    override fun finish() {
        super.finish()
        exitTransitionAnimation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /** [액션바 설정] */
    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarCardDetailsActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_arrow_30)
            actionBar.title = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name
        }

        binding.toolbarCardDetailsActivity.setNavigationOnClickListener { onBackPressed() }
    }

    /** [인텐트에서 전달 받은 데이터 불러오기] */
    private fun getIntentData() {
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Constants.BOARD_DETAIL, Board::class.java) ?: return
            } else {
                intent.getParcelableExtra(Constants.BOARD_DETAIL) ?: return
            }
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    /** [완료 - 카드 변경 내용에 대해 데이터베이스 갱신] */
    private fun updateCardDetails() {
        val card = Card(
            binding.etNameCardDetails.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds
        )

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }

    /** [카드 삭제 - 경고 대화상자 출력] */
    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    /** [카드 삭제 - 현재 카드 삭제 & 데이터베이스 갱신] */
    private fun deleteCard() {
        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)
        taskList[mTaskListPosition].cards = cardsList

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }

    /** [라벨 색상 - 색상을 선택하는 대화상자 출력] */
    private fun labelColorsListDialog() {
        val colorsList: ArrayList<String> = colorsList()

        val colorListDialog = LabelColorDialog(colorsList, resources.getString(R.string.str_select_label_color)) { color ->
            mSelectedColor = color
            setColor()
        }
        colorListDialog.show(supportFragmentManager, "ColorListDialog")
    }

    /** [라벨 색상 리스트] */
    private fun colorsList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()

        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }

    /** [라벨 색상을 선택한 색상으로 변경] */
    private fun setColor() {
        binding.tvSelectLabelColor.text = ""
        binding.tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    /** [멤버 등록 - 멤버를 선택하는 대화 상자 출력] */
    private fun membersListDialog() {
        // 카드의 현재 멤버 목록
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        if (cardAssignedMembersList.size > 0) {
            for (i in mMembersDetailList.indices) { // 보드의 현재 멤버 목록 순회
                for (j in cardAssignedMembersList) {
                    if (mMembersDetailList[i].id == j) {
                        mMembersDetailList[i].selected = true // 카드의 현재 멤버들에 체크 표시
                    }
                }
            }
        } else {
            for (i in mMembersDetailList.indices) {
                mMembersDetailList[i].selected = false
            }
        }

        // 현재 보드의 멤버 목록을 출력하는 대화상자 생성
        val memberListDialog = MembersListDialog(mMembersDetailList, resources.getString(R.string.str_select_member)) { user, action ->
            if (action == Constants.SELECT) {
                if (!mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.contains(user.id)) {
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.add(user.id)
                }
            } else {
                mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.remove(user.id)

                for (i in mMembersDetailList.indices) {
                    if (mMembersDetailList[i].id == user.id) {
                        mMembersDetailList[i].selected = false
                    }
                }
            }
            setupSelectedMembersList()
        }
        memberListDialog.show(supportFragmentManager, "MembersListDialog")
    }

    /** [멤버 출력 - 선택된 멤버들을 recycler view에 출력] */
    private fun setupSelectedMembersList() {
        // 카드에 등록된 멤버 목록
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        // 선택된 멤버 목록의 인스턴스
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in mMembersDetailList.indices) {
            for (j in cardAssignedMembersList) {
                if (mMembersDetailList[i].id == j) {
                    // 선택된 멤버들의 id, image 를 인스턴스에 추가
                    val selectedMember = SelectedMembers(
                        mMembersDetailList[i].id,
                        mMembersDetailList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        // 선택된 멤버가 1명 이상일 경우
        if (selectedMembersList.size > 0) {
            // 맨 뒤에 출력할 항목 ('+' 버튼)
            selectedMembersList.add(SelectedMembers("", ""))

            // 선택된 멤버들의 목록을 recyclerView에 출력
            binding.tvSelectMembers.visibility = View.GONE
            binding.rvSelectedMembersList.visibility = View.VISIBLE
            binding.rvSelectedMembersList.layoutManager = GridLayoutManager(this@CardDetailsActivity, 6)

            val adapter = CardMemberListAdapter(this@CardDetailsActivity, selectedMembersList, true)
            binding.rvSelectedMembersList.adapter = adapter
            adapter.setOnClickListener(object :
                CardMemberListAdapter.OnClickListener {
                    override fun onClick() {
                        membersListDialog()
                    }
            })
        } else {
            // 선택된 멤버가 없을 경우 멤버 선택 안내 텍스트 출력
            binding.tvSelectMembers.visibility = View.VISIBLE
            binding.rvSelectedMembersList.visibility = View.GONE
        }
    }

    /** [마감기한 - DateTimePicker 대화상자 출력] */
    private fun showDatePicker() {
        val dialog = DateTimePickerDialog.newInstance(mSelectedDueDateMilliSeconds)
        dialog.show(supportFragmentManager, DIALOG_DATE)
    }

    /** [변경 사항 저장 완료 & 액티비티 종료] */
    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}