package com.beinny.teamboard.ui.common

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.beinny.teamboard.utils.Constants.DATE_FORMAT
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@BindingAdapter("app:createdBy")
fun applyMemoCountFormat(view: TextView, createdBy: String?) {
    view.text = createdBy + "가 생성함"
}

@BindingAdapter("app:dateInKorean")
fun applyDateFormat(view: TextView, date: Long) {
    if (date > 0) {
        val df: DateFormat = SimpleDateFormat(DATE_FORMAT) // 날짜를 문자열로 변환
        view.text = df.format(date)
    }
    else {
        view.text = "Select Due Date"
    }
}

@BindingAdapter("app:notificationDate")
fun applyNotificationDateFormat(view: TextView, date: Long) {
    if (date > 0) {
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        view.text = df.format(Date(date))
    }
    else {
        view.text = ""
    }
}