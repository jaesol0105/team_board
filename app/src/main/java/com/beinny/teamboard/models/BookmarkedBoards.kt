package com.beinny.teamboard.models

import android.os.Parcel
import android.os.Parcelable

data class BookmarkedBoards(
    val title: String = "",
    val bookmarkedBoards: ArrayList<Board> = ArrayList(),
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.createTypedArrayList(Board.CREATOR)!!,
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(title)
        writeTypedList(bookmarkedBoards)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BookmarkedBoards> = object : Parcelable.Creator<BookmarkedBoards> {
            override fun createFromParcel(source: Parcel): BookmarkedBoards = BookmarkedBoards(source)
            override fun newArray(size: Int): Array<BookmarkedBoards?> = arrayOfNulls(size)
        }
    }
}