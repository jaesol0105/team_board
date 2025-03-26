package com.beinny.teamboard.ui.home.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.beinny.teamboard.databinding.FragmentNotificationBinding
import com.beinny.teamboard.ui.adapters.NotificationAdapter
import com.beinny.teamboard.ui.home.boardList.BoardListViewModel

class NotificationFragment : Fragment() {
    private lateinit var binding: FragmentNotificationBinding
    private val viewModel: BoardListViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.rvNotificationList.layoutManager = LinearLayoutManager(requireContext())

        viewModel.allNotifications.observe(viewLifecycleOwner, { notifications ->
            binding.rvNotificationList.adapter = NotificationAdapter(ArrayList(notifications))
            binding.rvNotificationList.visibility = View.VISIBLE
            binding.tvNoNotification.visibility = View.GONE
        })

        viewModel.markAllAsRead()
    }
}