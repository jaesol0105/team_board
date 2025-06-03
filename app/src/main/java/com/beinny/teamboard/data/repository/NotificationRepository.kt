package com.beinny.teamboard.data.repository

import com.beinny.teamboard.data.source.local.NotificationDao
import com.beinny.teamboard.data.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val dao: NotificationDao) {
    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()
    val unreadCount: Flow<Int> = dao.getUnreadCount()

    /** 모든 알림 읽음 표시 */
    suspend fun markAllAsRead() {
        dao.markAllAsRead()
    }

    /** Room에 알림 메세지를 저장 */
    suspend fun insertNotification(notification: NotificationEntity) {
        dao.insert(notification)
    }

    /** Room에서 알림 메세지를 삭제 */
    suspend fun deleteNotification(notification: NotificationEntity) {
        dao.delete(notification)
    }
}