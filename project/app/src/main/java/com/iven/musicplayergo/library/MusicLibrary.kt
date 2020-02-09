package com.iven.musicplayergo.library

import android.content.Context
import com.iven.musicplayergo.models.Album
import com.iven.musicplayergo.models.Music
import com.iven.musicplayergo.models.PlaylistMusic
import com.iven.musicplayergo.musicLibrary
import com.iven.musicplayergo.utils.MusicUtils

class MusicLibrary {

    var allSongsUnfiltered: MutableList<Music> = mutableListOf()

    var allSongs: MutableList<Music>? = null

    //keys: artist || value: its songs
    var allSongsByArtist: Map<String?, List<Music>>? = null

    //keys: artist || value: albums
    val allAlbumsByArtist: MutableMap<String, List<Album>>? = mutableMapOf()

    //keys: artist || value: songs contained in the folder
    var allSongsByFolder: Map<String, List<Music>?>? = null

    lateinit var rawPlaylist: MutableList<PlaylistMusic>

    var playlist: MutableMap<String, List<Pair<Music, Int>>>? = mutableMapOf()

    val randomMusic get() = allSongs?.random()

    fun updatePlaylist(playlistDao: PlaylistDao) {
        val playlist = playlistDao.getAll()
        rawPlaylist = playlist
        val groupedPlaylist = playlist.groupBy { it.playlistName }
        groupedPlaylist.keys.forEach { key ->
            val getVal = groupedPlaylist[key]?.map { it.song }!!
            musicLibrary.playlist?.set(key, getVal)
        }
    }

    fun buildMusicLibrary(context: Context) = try {


        allSongs =
            allSongsUnfiltered.distinctBy { it.artist to it.year to it.track to it.title to it.duration to it.album }
                .toMutableList()

        allSongsByArtist = allSongs?.groupBy { it.artist }

        allSongsByArtist?.keys?.iterator()?.forEach {
            it?.let { artist ->
                allAlbumsByArtist.apply {
                    this?.set(
                        artist,
                        MusicUtils.buildSortedArtistAlbums(
                            context.resources,
                            allSongsByArtist?.getValue(artist)
                        )
                    )
                }
            }
        }

        allSongsByFolder = allSongs?.groupBy {
            it.relativePath!!
        }
        allSongsByFolder
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
