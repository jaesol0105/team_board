package com.beinny.teamboard

import android.content.Context
import androidx.room.Room
import com.beinny.teamboard.data.local.NotificationDatabase
import com.beinny.teamboard.data.repository.NotificationRepository

object ServiceLocator {
    private var database : NotificationDatabase? = null
    private var recordRepository : NotificationRepository? = null

    private fun provideDatabase(applicationContext: Context) : NotificationDatabase {
        return database ?: kotlin.run {
            Room.databaseBuilder(
                applicationContext,
                NotificationDatabase::class.java,
                "notifications_db"
            ).build().also {
                database = it
            }
        }
    }

    fun provideNotificationRepository(context: Context) : NotificationRepository {
        return recordRepository ?: kotlin.run {
            val dao = provideDatabase(context.applicationContext).notificationDao()
            NotificationRepository(dao).also {
                recordRepository = it
            }
        }
    }
}
