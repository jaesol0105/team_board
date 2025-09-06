package com.beinny.teamboard.data.model

import android.os.Parcel
import android.os.Parcelable

data class KakaoProfile(
    val email: String? = "",
    val nickname: String? = "",
    val profileImageUrl: String? = ""
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(email)
        writeString(nickname)
        writeString(profileImageUrl)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<KakaoProfile> {
        override fun createFromParcel(parcel: Parcel): KakaoProfile {
            return KakaoProfile(parcel)
        }

        override fun newArray(size: Int): Array<KakaoProfile?> {
            return arrayOfNulls(size)
        }
    }
}