package com.beinny.teamboard.ui.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.beinny.teamboard.ServiceLocator
import com.beinny.teamboard.ui.carddetail.CardDetailsViewModel
import com.beinny.teamboard.ui.createboard.CreateBoardViewModel
import com.beinny.teamboard.ui.login.AuthViewModel
import com.beinny.teamboard.ui.member.MembersViewModel
import com.beinny.teamboard.ui.member.NotificationHelper
import com.beinny.teamboard.ui.myprofile.MyProfileViewModel
import com.beinny.teamboard.ui.shared.BoardListViewModel
import com.beinny.teamboard.ui.tasklist.TaskListViewModel
import java.lang.IllegalArgumentException

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BoardListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BoardListViewModel(
                boardRepository = ServiceLocator.provideBoardRepository(context),
                notificationRepository = ServiceLocator.provideNotificationRepository(context)
            ) as T
        }
        if (modelClass.isAssignableFrom(CreateBoardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateBoardViewModel(ServiceLocator.provideBoardRepository(context)) as T
        }
        if (modelClass.isAssignableFrom(CardDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardDetailsViewModel(ServiceLocator.provideBoardRepository(context)) as T
        }
        if (modelClass.isAssignableFrom(MyProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyProfileViewModel(ServiceLocator.provideBoardRepository(context)) as T
        }
        if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskListViewModel(ServiceLocator.provideBoardRepository(context)) as T
        }
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(ServiceLocator.provideBoardRepository(context)) as T
        }
        if (modelClass.isAssignableFrom(MembersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MembersViewModel(ServiceLocator.provideBoardRepository(context), NotificationHelper(context)) as T
        }
        else {
            throw IllegalArgumentException("Failed to create ViewModel: ${modelClass.name}")
        }
    }
}