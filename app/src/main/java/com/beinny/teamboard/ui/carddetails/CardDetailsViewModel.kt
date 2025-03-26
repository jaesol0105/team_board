package com.beinny.teamboard.ui.carddetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Date

class CardDetailsViewModel() : ViewModel() {
    private var _selectedDate = MutableLiveData<Long>()
    val selectedDate: LiveData<Long> get() = _selectedDate

    fun onDateSelected(date: Date) {
        _selectedDate.value = date.time
    }
}