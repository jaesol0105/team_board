package com.beinny.teamboard

import android.content.Context
import androidx.room.Room
import com.beinny.teamboard.data.repository.BoardRepository
import com.beinny.teamboard.data.source.local.NotificationDatabase
import com.beinny.teamboard.data.repository.NotificationRepository
import com.beinny.teamboard.data.source.local.SharedPrefsHelper
import com.beinny.teamboard.data.source.remote.BoardRemoteDataSource

object ServiceLocator {
    private var database : NotificationDatabase? = null
    private var notificationRepository : NotificationRepository? = null
    private var boardRepository: BoardRepository? = null

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
        return notificationRepository ?: kotlin.run {
            val dao = provideDatabase(context.applicationContext).notificationDao()
            NotificationRepository(dao).also {
                notificationRepository = it
            }
        }
    }

    fun provideBoardRepository(context: Context): BoardRepository {
        if (boardRepository == null) {
            boardRepository = BoardRepository(BoardRemoteDataSource(), SharedPrefsHelper(context))
        }
        return boardRepository!!
    }
}
