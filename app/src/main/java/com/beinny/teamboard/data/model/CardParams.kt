package com.beinny.teamboard.data.model

data class CardParams(
    val board: Board,
    val taskPosition: Int,
    val cardPosition: Int,
    val members: ArrayList<User>
) {
    val card: Card
        get() = board.taskList[taskPosition].cards[cardPosition]
}