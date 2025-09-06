package com.beinny.teamboard.ui.tasklist

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beinny.teamboard.R
import com.beinny.teamboard.data.model.Task
import com.beinny.teamboard.data.model.User
import com.beinny.teamboard.databinding.ItemTaskBinding
import com.beinny.teamboard.ui.common.showConfirmationDialog
import com.beinny.teamboard.ui.common.toggleWith

open class TaskAdapter (
    private val context: Context,
    private var tasks: ArrayList<Task>,
    private val members: List<User>,
    private val listener: TaskListListener
) : RecyclerView.Adapter<TaskAdapter.TaskHolder>() {
    private lateinit var binding: ItemTaskBinding

    inner class TaskHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task, position: Int) {
            binding.task = task

            setupVisibility(position)
            setupListeners(task, position)
            setupAdapter(task, position)
        }

        private fun setupVisibility(position: Int) {
            // 마지막에 '새 리스트 추가' 뷰
            if (position == tasks.size - 1) {
                binding.layoutTask.toggleWith(binding.btnAddTask)
            } else {
                binding.btnAddTask.toggleWith(binding.layoutTask)
            }
        }

        private fun setupListeners(task: Task, position: Int) {
            // 새 테스크 추가
            binding.btnAddTask.setOnClickListener {
                binding.btnAddTask.toggleWith(binding.cvAddTask)
            }
            binding.btnCancelAddTask.setOnClickListener {
                binding.cvAddTask.toggleWith(binding.btnAddTask)
            }
            binding.btnSubmitAddTask.setOnClickListener {
                val name = binding.etTaskName.text.toString()
                if (name.isNotBlank()) {
                    listener.onCreateTaskList(name)
                } else {
                    Toast.makeText(context, context.getString(R.string.please_input_task_name), Toast.LENGTH_SHORT).show()
                }
            }

            // 테스크 이름 수정
            binding.btnEditTaskTitle.setOnClickListener {
                binding.etTaskNameEdit.setText(task.title)
                binding.layoutTaskTitle.toggleWith(binding.cvEditTask)
            }
            binding.btnCancelEditTask.setOnClickListener {
                binding.cvEditTask.toggleWith(binding.layoutTaskTitle)
            }
            binding.btnSubmitEditTask.setOnClickListener {
                val name = binding.etTaskNameEdit.text.toString()
                if (name.isNotBlank()) {
                    listener.onEditTaskList(position, name, task)
                } else {
                    Toast.makeText(context, context.getString(R.string.please_input_task_name), Toast.LENGTH_SHORT).show()
                }
            }

            // 테스크 삭제
            binding.btnDeleteTask.setOnClickListener {
                alertDialogForDeleteList(position, task.title)
            }

            // 새 카드 추가
            binding.btnAddCard.setOnClickListener {
                binding.btnAddCard.toggleWith(binding.cvAddCard)
            }
            binding.btnCancelAddCard.setOnClickListener {
                binding.cvAddCard.toggleWith(binding.btnAddCard)
            }
            binding.btnSubmitAddCard.setOnClickListener {
                val name = binding.etCardName.text.toString()
                if (name.isNotBlank()) {
                    listener.onAddCard(position, name)
                } else {
                    Toast.makeText(context, context.getString(R.string.please_input_card_name), Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun setupAdapter(task: Task, position: Int) {
            val adapter = CardAdapter(context, task.cards, members) { cardPosition ->
                listener.onCardClick(position, cardPosition)
            }

            binding.rvCardList.apply {
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
                this.adapter = adapter
                /*
                if (itemDecorationCount == 0) // 중복생성 방지
                    addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                */
            }

            val touchHelper = ItemTouchHelper(
                CardItemTouchHelperCallback(
                    cards = task.cards,
                    adapter = adapter,
                    onReordered = { reorderedCards ->
                        listener.onCardsReordered(position, reorderedCards)
                    }
                )
            )
            touchHelper.attachToRecyclerView(binding.rvCardList)
        }
    }

    override fun getItemCount(): Int = tasks.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        binding = ItemTaskBinding.inflate(LayoutInflater.from(context),parent,false)

        // item_task 들은 스크린의 70% 크기의 넓이 & 동적인 높이를 갖는다.
        val layoutParams = LinearLayout.LayoutParams((parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0) // 마진 설정
        binding.root.layoutParams = layoutParams

        return TaskHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        holder.bind(tasks[position],position)
    }

    /** 작업 목록 삭제에 대한 경고 대화상자 출력 */
    private fun alertDialogForDeleteList(position: Int, title: String) {
        context.showConfirmationDialog(
            message = context.getString(R.string.task_warning_message_delete_task),
            positiveButtonText = context.getString(R.string.delete),
            negativeButtonText = context.getString(R.string.cancel),
            onConfirm = { listener.onDeleteTaskList(position) }
        )
    }

    private fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}