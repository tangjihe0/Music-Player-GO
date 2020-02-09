package com.iven.musicplayergo.library

import androidx.room.*
import com.iven.musicplayergo.models.PlaylistMusic

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlistmusic")
    fun getAll(): MutableList<PlaylistMusic>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(playlistMusic: PlaylistMusic)

    @Transaction
    fun insertOrDelete(delete: Boolean, playlistMusic: PlaylistMusic) {
        if (delete) delete(playlistMusic) else insert(playlistMusic)
    }

    @Delete
    fun delete(playlistMusic: PlaylistMusic)
}
