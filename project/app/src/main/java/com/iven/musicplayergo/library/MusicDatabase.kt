package com.iven.musicplayergo.library

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iven.musicplayergo.models.Music
import com.iven.musicplayergo.models.PlaylistMusic

@Database(entities = [Music::class, PlaylistMusic::class], version = 1)

@TypeConverters(PlaylistMusicTypeConverter::class)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun musicDao(): MusicDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase {
            val tempInstance =
                INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "music_player_go_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
