package com.beinny.teamboard.data.repository

import androidx.lifecycle.LiveData
import com.beinny.teamboard.data.local.NotificationDao
import com.beinny.teamboard.data.local.NotificationEntity

class NotificationRepository(private val dao: NotificationDao) {
    val allNotifications: LiveData<List<NotificationEntity>> = dao.getAllNotifications()
    val unreadCount: LiveData<Int> = dao.getUnreadCount()

    /** [모든 알림 읽음 표시] */
    suspend fun markAllAsRead() {
        dao.markAllAsRead()
    }

    /** [Room에 알림을 추가] */
    suspend fun insertNotification(notification: NotificationEntity) {
        dao.insert(notification)
    }
}