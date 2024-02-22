package com.beinny.teamboard.models

import android.os.Parcel
import android.os.Parcelable

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val image: String = "",
    val mobile: Long = 0,
    /** [firebase Cloud Messaging] */
    val fcmToken: String = "" // fcmToken은 어플리케이션에서 사용자에게 푸시 알림을 줄 때 필요하다. fcmToken은 디바이스마다 다르게 부여 되는데, 서버는 이 토큰으로 디바이스를 구분한다. 이 토큰으로 구분하지 않는다면, 서버는 어떤 디바이스에 알림을 줘야하는 것인지 모르기 때문이다.
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readLong(),
        source.readString()!!
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(name)
        writeString(email)
        writeString(image)
        writeLong(mobile)
        writeString(fcmToken)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}