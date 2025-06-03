package com.beinny.teamboard.ui.tasklist

import com.beinny.teamboard.data.model.Card
import com.beinny.teamboard.data.model.Task

interface TaskListListener {
    fun onCreateTaskList(name: String)
    fun onEditTaskList(position: Int, name: String, task: Task)
    fun onDeleteTaskList(position: Int)
    fun onAddCard(position: Int, cardName: String)
    fun onCardClick(taskPosition: Int, cardPosition: Int)
    fun onCardsReordered(taskPosition: Int, cards: ArrayList<Card>)
}