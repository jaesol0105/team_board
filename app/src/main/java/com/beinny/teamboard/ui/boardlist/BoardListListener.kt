package com.beinny.teamboard.ui.boardlist

import com.beinny.teamboard.data.model.Board

interface BoardListListener {
    fun onBoardClick(board: Board)
    fun onBookmarkClick(board: Board)
}