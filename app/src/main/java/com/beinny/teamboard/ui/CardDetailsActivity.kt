package com.beinny.teamboard.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.beinny.teamboard.R
import com.beinny.teamboard.adapters.CardMemberListAdapter
import com.beinny.teamboard.databinding.ActivityCardDetailsBinding
import com.beinny.teamboard.dialogs.LabelColorDialog
import com.beinny.teamboard.dialogs.MembersListDialog
import com.beinny.teamboard.firebase.FirestoreClass
import com.beinny.teamboard.models.Board
import com.beinny.teamboard.models.Card
import com.beinny.teamboard.models.SelectedMembers
import com.beinny.teamboard.models.Task
import com.beinny.teamboard.models.User
import com.beinny.teamboard.utils.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CardDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityCardDetailsBinding
    private lateinit var mBoardDetails: Board // 보드 디테일 글로벌 변수
    private var mTaskListPosition: Int = -1 // task item position
    private var mCardPosition: Int = -1 // card item position
    private var mSelectedColor: String = "" // 카드 라벨 색상 선택을 위한 글로벌 변수
    private lateinit var mMembersDetailList: ArrayList<User> // 멤버 목록 글로벌 변수
    private var mSelectedDueDateMilliSeconds: Long = 0 // 마감 기한 글로벌 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentData()
        setupActionBar()

        binding.etNameCardDetails.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        binding.etNameCardDetails.setSelection(binding.etNameCardDetails.text.toString().length) // 커서 위치

        binding.btnUpdateCardDetails.setOnClickListener {
            if(binding.etNameCardDetails.text.toString().isNotEmpty()) {
                updateCardDetails()
            }else{
                // TODO : 업데이트버튼 이름 1글자 이상일때만 활성화 + 색깔
                Toast.makeText(this@CardDetailsActivity, "카드 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvSelectLabelColor.setOnClickListener {
            labelColorsListDialog()
        }

        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if (mSelectedColor.isNotEmpty()) {
            setColor()
        }

        binding.tvSelectMembers.setOnClickListener {
            membersListDialog()
        }

        // 카드에 등록된 멤버들을 보여주는 recyclerView 출력
        setupSelectedMembersList()

        mSelectedDueDateMilliSeconds =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate
        if (mSelectedDueDateMilliSeconds > 0) {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            binding.tvSelectDueDate.text = selectedDate
        }

        binding.tvSelectDueDate.setOnClickListener {
            showDataPicker()
        }

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
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_24)
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

    /** [카드 세부정보 업데이트] */
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

    /** [업데이트 완료 & 액티비티 종료] */
    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    /** [카드 삭제 경고 대화상자 출력] */
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

    /** [현재 작업목록에서 카드 삭제] */
    private fun deleteCard() {
        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)
        taskList[mTaskListPosition].cards = cardsList

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }

    /** [라벨 색상을 선택하는 대화상자 출력] */
    private fun labelColorsListDialog() {
        val colorsList: ArrayList<String> = colorsList()

        val listDialog = object : LabelColorDialog(
            this@CardDetailsActivity,
            colorsList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor
        ) {
            // abstract function 구현
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    /** [라벨 색상 리스트 반환] */
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

    /** [등록할 멤버를 선택하는 대화 상자 출력] */
    private fun membersListDialog() {
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        // 현재 카드에 배정된 멤버들은 체크 표시
        if (cardAssignedMembersList.size > 0) {
            for (i in mMembersDetailList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mMembersDetailList[i].id == j) {
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        } else {
            for (i in mMembersDetailList.indices) {
                mMembersDetailList[i].selected = false
            }
        }

        val listDialog = object : MembersListDialog(
            this@CardDetailsActivity,
            mMembersDetailList,
            resources.getString(R.string.str_select_member)
        ) {
            override fun onItemSelected(user: User, action: String) {
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
        }
        listDialog.show()
    }

    /** [카드의 멤버들을 recycler view에 출력] */
    private fun setupSelectedMembersList() {
        // 카드에 등록된 멤버 목록
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        // 선택된 멤버 목록 인스턴스
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in mMembersDetailList.indices) {
            for (j in cardAssignedMembersList) {
                if (mMembersDetailList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        mMembersDetailList[i].id,
                        mMembersDetailList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        // 카드에 등록된 멤버가 1명 이상일 경우
        if (selectedMembersList.size > 0) {
            // 맨 뒤에 출력할 항목 ('+' 버튼)
            selectedMembersList.add(SelectedMembers("", ""))

            binding.tvSelectMembers.visibility = View.GONE
            binding.rvSelectedMembersList.visibility = View.VISIBLE

            binding.rvSelectedMembersList.layoutManager = GridLayoutManager(this@CardDetailsActivity, 6)

            val adapter = CardMemberListAdapter(this@CardDetailsActivity, selectedMembersList, true)
            binding.rvSelectedMembersList.adapter = adapter
            adapter.setOnClickListener(object :
                CardMemberListAdapter.OnClickListener {
                    override fun onClick() {
                        membersListDialog() // 멤버 선택
                    }
            })
        } else {
            binding.tvSelectMembers.visibility = View.VISIBLE
            binding.rvSelectedMembersList.visibility = View.GONE
        }
    }

    /** [마감기한을 설정하는 DatePicker 대화상자 출력] */
    private fun showDataPicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                val sMonthOfYear = if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"
                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                binding.tvSelectDueDate.text = selectedDate

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)

                mSelectedDueDateMilliSeconds = theDate!!.time },
            year,
            month,
            day
        )
        dpd.show()
    }
}