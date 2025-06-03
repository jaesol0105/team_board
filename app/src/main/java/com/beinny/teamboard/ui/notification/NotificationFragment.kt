package com.beinny.teamboard.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.beinny.teamboard.R
import com.beinny.teamboard.data.model.NotificationEntity
import com.beinny.teamboard.databinding.FragmentNotificationBinding
import com.beinny.teamboard.ui.common.repeatOnStarted
import com.beinny.teamboard.ui.common.toggleWith
import com.beinny.teamboard.ui.factory.ViewModelFactory
import com.beinny.teamboard.ui.shared.BoardListViewModel

class NotificationFragment : Fragment() {
    private lateinit var binding: FragmentNotificationBinding
    private val viewModel: BoardListViewModel by activityViewModels { ViewModelFactory(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

        repeatOnStarted {
            viewModel.notificationsList.collect { notifications ->
                if(notifications.isNotEmpty()){
                    binding.tvNoNotification.toggleWith(binding.rvNotification)

                    val adapter = NotificationAdapter(ArrayList(notifications), getString(R.string.notification_list_subtitle)) { notification ->
                        viewModel.deleteNotification(notification)
                    }
                    binding.rvNotification.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        this.adapter = adapter
                    }
                } else {
                    binding.rvNotification.toggleWith(binding.tvNoNotification)
                }
            }
        }
        viewModel.markAllNotificationsAsRead()
    }
}