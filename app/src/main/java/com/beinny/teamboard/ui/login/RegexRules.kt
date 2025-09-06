package com.beinny.teamboard.ui.login

object RegexRules {
    // 이름: 한글/영문/공백/일부 특수문자(.’-) 허용, 길이 2~30
    val NAME = Regex("^[\\p{L}][\\p{L} .'-]{1,29}$")
    // 이메일: 이메일 형식 준수
    val EMAIL = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)
    // 비밀번호: 공백 없는 6자 이상
    val PASSWORD = Regex("^[^\\s]{6,}$")
}