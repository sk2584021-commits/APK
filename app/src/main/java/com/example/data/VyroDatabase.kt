package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, Post::class, Like::class, Comment::class, Follow::class, Notification::class, Conversation::class, Message::class, Block::class, SavedPost::class, Report::class], version = 5, exportSchema = false)
abstract class VyroDatabase : RoomDatabase() {
    abstract fun vyroDao(): VyroDao

    companion object {
        @Volatile
        private var INSTANCE: VyroDatabase? = null

        fun getDatabase(context: Context): VyroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VyroDatabase::class.java,
                    "vyro_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
