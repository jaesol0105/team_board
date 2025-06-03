package com.beinny.teamboard.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.beinny.teamboard.data.model.NotificationEntity

@Database(entities = [NotificationEntity::class], version = 1)
abstract class NotificationDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}