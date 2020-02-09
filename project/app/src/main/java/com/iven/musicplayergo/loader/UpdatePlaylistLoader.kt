package com.iven.musicplayergo.loader

import android.content.Context
import com.iven.musicplayergo.library.MusicDatabase
import com.iven.musicplayergo.models.PlaylistMusic
import com.iven.musicplayergo.musicLibrary

class UpdatePlaylistLoader(
    context: Context,
    private val delete: Boolean,
    private val playlistMusic: PlaylistMusic?
) : WrappedAsyncTaskLoader<Any?>(context) {

    // This is where background code is executed
    override fun loadInBackground() {
        val playlistDao = MusicDatabase.getDatabase(context).playlistDao()
        playlistMusic?.let { music ->
            playlistDao.insertOrDelete(delete, music)
        }
        musicLibrary.updatePlaylist(playlistDao)
    }
}
