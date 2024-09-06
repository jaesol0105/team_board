package com.beinny.teamboard.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beinny.teamboard.R
import com.beinny.teamboard.databinding.ItemTaskBinding
import com.beinny.teamboard.models.Task
import com.beinny.teamboard.ui.TaskListActivity
import java.util.Collections

open class TaskAdapter (
    private val context: Context,
    private var list: ArrayList<Task>
) : RecyclerView.Adapter<TaskAdapter.TaskHolder>() {
    private lateinit var binding: ItemTaskBinding
    private var mPositionDraggedFrom = -1 // recycler view item 드래그 시작 위치
    private var mPositionDraggedTo = -1 // recycler view item 드래그 목표 위치

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskAdapter.TaskHolder {
        binding = ItemTaskBinding.inflate(LayoutInflater.from(context),parent,false)

        // item_task 들은 스크린의 70% 크기의 넓이 & 동적인 높이를 갖는다.
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        // 마진 설정
        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)
        binding.root.layoutParams = layoutParams

        return TaskHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        holder.bind(list[position],position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    /** [작업 목록 삭제에 대한 경고 대화상자 출력] */
    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(context.getString(R.string.warning_message_delete_task))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton(context.getString(R.string.delete)) { dialogInterface, which ->
            dialogInterface.dismiss()
            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }
        builder.setNegativeButton(context.getString(R.string.cancle)) { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    inner class TaskHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task, position: Int) {
            binding.task = task

            if (position == list.size - 1) {
                binding.tvAddTaskListBtn.visibility = View.VISIBLE
                binding.llTaskListItems.visibility = View.GONE
            } else {
                binding.tvAddTaskListBtn.visibility = View.GONE
                binding.llTaskListItems.visibility = View.VISIBLE
            }

            // 새 작업 목록 추가
            binding.tvAddTaskListBtn.setOnClickListener {
                binding.tvAddTaskListBtn.visibility = View.GONE
                binding.cvAddTaskListNameView.visibility = View.VISIBLE
            }

            // 닫기
            binding.ibCloseTaskListName.setOnClickListener {
                binding.tvAddTaskListBtn.visibility = View.VISIBLE
                binding.cvAddTaskListNameView.visibility = View.GONE
            }

            // '작업 목록 명'을 activity로 전달하여 새 작업 목록을 생성한다
            binding.ibDoneTaskListName.setOnClickListener {
                val listName = binding.etTaskListName.text.toString()
                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT).show()
                }
            }

            // '작업 목록 명' 수정
            binding.ibEditTaskListName.setOnClickListener {
                binding.etTaskListNameEditable.setText(task.title)

                binding.llTaskListTitleView.visibility = View.GONE
                binding.cvEditTaskListNameView.visibility = View.VISIBLE
            }

            // 닫기
            binding.ibCloseTaskListNameEditable.setOnClickListener {
                binding.llTaskListTitleView.visibility = View.VISIBLE
                binding.cvEditTaskListNameView.visibility = View.GONE
            }

            // '변경할 작업 목록 명'을 activity로 전달하여 이름을 변경한다
            binding.ibDoneTaskListNameEditable.setOnClickListener {
                val listName = binding.etTaskListNameEditable.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.updateTaskList(position, listName, task)
                    }
                } else {
                    Toast.makeText(context, "Please Enter List Name.", Toast.LENGTH_SHORT).show()
                }
            }

            // 작업 목록 삭제
            binding.ibDeleteTaskList.setOnClickListener {
                alertDialogForDeleteList(position, task.title)
            }

            // 작업 목록에 새 카드 추가
            binding.tvAddCardBtn.setOnClickListener {
                binding.tvAddCardBtn.visibility = View.GONE
                binding.cvAddCardNameView.visibility = View.VISIBLE // 카드 이름 입력하는 뷰 활성화
            }

            // 생성할 카드 명을 activity로 전달하여 새 카드를 생성한다
            binding.ibDoneCardName.setOnClickListener {
                val cardName = binding.etCardName.text.toString()
                if (cardName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.addCardToTaskList(position, cardName)
                    }
                } else {
                    Toast.makeText(context, "Please Enter Card Detail.", Toast.LENGTH_SHORT).show()
                }
            }

            // 닫기
            binding.ibCloseCardName.setOnClickListener {
                binding.tvAddCardBtn.visibility = View.VISIBLE
                binding.cvAddCardNameView.visibility = View.GONE
            }

            binding.rvCardList.layoutManager = LinearLayoutManager(context)
            binding.rvCardList.setHasFixedSize(true)

            val adapter = CardAdapter(context, task.cards)
            binding.rvCardList.adapter = adapter

            // card item 클릭 리스너
            adapter.setOnClickListener(object :
                CardAdapter.OnClickListener {
                override fun onClick(cardPosition: Int) {
                    if (context is TaskListActivity) {
                        context.cardDetails(position, cardPosition)
                    }
                }
            })

            val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            binding.rvCardList.addItemDecoration(dividerItemDecoration)

            // ItemTouchHelper.SimpleCallback : 드래그와 스와이프 동작을 처리하기 위한 콜백 클래스
            val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
                /** 사용자가 아이템을 드래그하여 이동할 때 호출
                 * @param dragged 드래그 중인 아이템의 뷰 홀더
                 * @param target 드래그한 아이템이 놓이게 될 목표 위치의 뷰 홀더 */
                override fun onMove(
                    recyclerView: RecyclerView,
                    dragged: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val draggedPosition = dragged.adapterPosition // 드래그 중인 아이템의 현재 위치를 가져온다
                    val targetPosition = target.adapterPosition // 드래그한 아이템이 이동할 목표 위치를 가져온다

                    // 글로벌 변수를 통해 드래그 시작 위치와 끝 위치를 저장
                    if (mPositionDraggedFrom == -1) { // 처음 드래그가 시작될 때
                        mPositionDraggedFrom = draggedPosition // 시작 위치를 저장
                    }
                    mPositionDraggedTo = targetPosition // 목표 위치를 저장

                    // 리스트에서 드래그한 위치와 목표 위치의 아이템을 교체한다
                    Collections.swap(list[position].cards, draggedPosition, targetPosition)

                    // 어댑터에게 변경 사항 알리기 (UI 갱신)
                    adapter.notifyItemMoved(draggedPosition, targetPosition)

                    // 이미 onMove 메서드에서 데이터 변경과 UI 갱신이 이루어졌으므로 false를 반환
                    return false
                }

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) { }

                /** clearView : 사용자가 드래그 동작을 완료했을 때 호출
                 * 아이템의 위치가 변경된 경우 이를 데이터베이스에 업데이트하고, 전역변수를 -1로 초기화 */
                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)

                    // 드래그 동작이 실제로 발생했는가? & 위치가 변경되었는가?
                    if (mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 && mPositionDraggedFrom != mPositionDraggedTo) {
                        (context as TaskListActivity).updateCardsInTaskList(position, list[position].cards)
                    }

                    // 전역변수를 -1로 초기화
                    mPositionDraggedFrom = -1
                    mPositionDraggedTo = -1
                }
            })

            // ItemTouchHelper를 RecyclerView에 연결
            helper.attachToRecyclerView(binding.rvCardList)
        }
    }
}