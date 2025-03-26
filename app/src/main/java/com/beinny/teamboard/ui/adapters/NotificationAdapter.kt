package com.beinny.teamboard.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beinny.teamboard.data.local.NotificationEntity
import com.beinny.teamboard.databinding.ItemBoardHeaderBinding
import com.beinny.teamboard.databinding.ItemNotificationBinding

open class NotificationAdapter(
    private var list: ArrayList<NotificationEntity>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var headerBinding: ItemBoardHeaderBinding
    private lateinit var binding: ItemNotificationBinding

    private val TYPE_HEADER = 0

    override fun getItemCount(): Int = list.size + 1

    /** [아이템에 고유한 타입(포지션)을 부여 - 중복 이미지 오류 해결] */
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                headerBinding = ItemBoardHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(headerBinding)
            }
            else -> {
                binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                NotificationHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_HEADER -> {
                (holder as HeaderViewHolder).bind("알림")
            }
            else -> {
                (holder as NotificationHolder).bind(list[position-1])
            }
        }
    }

    inner class HeaderViewHolder(private val binding: ItemBoardHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title:String){
            binding.headerTitle.text = title
        }
    }

    inner class NotificationHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: NotificationEntity) {
            binding.notification = notification
            /*
            binding.root.setBackgroundColor(
                if (notification.isRead) Color.LTGRAY else Color.WHITE
            )
            */
        }
    }
}
