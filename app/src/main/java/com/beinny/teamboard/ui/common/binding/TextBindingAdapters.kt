package com.beinny.teamboard.ui.common.binding

import android.graphics.Color
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.beinny.teamboard.utils.Constants.DATE_FORMAT
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@BindingAdapter("app:createdBy")
fun applyMemoCountFormat(view: TextView, createdBy: String?) {
    view.text = createdBy + "이 생성함"
}

@BindingAdapter("app:notificationDate")
fun applyNotificationDateFormat(view: TextView, date: Long) {
    if (date > 0) {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        view.text = dateFormat.format(Date(date))
    }
    else {
        view.text = ""
    }
}

@BindingAdapter("app:textValue")
fun setTextValue(view: EditText, value: Any?) {
    val text = when (value) {
        is Long -> if (value != 0L) value.toString() else ""
        is String -> value
        else -> ""
    }
    if (view.text.toString() != text) {
        view.setText(text)
    }
}

@BindingAdapter("setCursorToEnd")
fun EditText.setCursorToEnd(enable: Boolean) {
    if (enable) {
        // post : 뷰가 완전히 그려진 후 적용
        this.post { setSelection(text.length) }
    }
}

@BindingAdapter("setLabelColor")
fun TextView.setLabelColor(color: String) {
    if(color.isNotBlank()){
        text = ""
        setBackgroundColor(Color.parseColor(color))
    }
}