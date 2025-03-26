package com.beinny.teamboard.ui.common

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.beinny.teamboard.ServiceLocator
import com.beinny.teamboard.ui.carddetails.CardDetailsViewModel
import com.beinny.teamboard.ui.home.boardList.BoardListViewModel
import java.lang.IllegalArgumentException

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BoardListViewModel::class.java))
        {
            @Suppress("UNCHECKED_CAST")
            return BoardListViewModel(ServiceLocator.provideNotificationRepository(context)) as T
        }
        if (modelClass.isAssignableFrom(CardDetailsViewModel::class.java))
        {
            @Suppress("UNCHECKED_CAST")
            return CardDetailsViewModel() as T
        }
        else
        {
            throw IllegalArgumentException("Failed to create ViewModel: ${modelClass.name}")
        }
    }
}