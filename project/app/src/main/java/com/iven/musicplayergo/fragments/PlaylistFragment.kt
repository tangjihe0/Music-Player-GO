package com.iven.musicplayergo.fragments

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.recyclical.datasource.dataSourceOf
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.iven.musicplayergo.*
import com.iven.musicplayergo.models.Music
import com.iven.musicplayergo.utils.PlaylistHolder
import com.iven.musicplayergo.utils.SongsViewHolder
import com.iven.musicplayergo.utils.ThemeHelper
import com.iven.musicplayergo.utils.UIControlInterface
import kotlinx.android.synthetic.main.playlist_fragment.*


/**
 * A simple [Fragment] subclass.
 * Use the [PlaylistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class PlaylistFragment : Fragment(R.layout.playlist_fragment) {

    private lateinit var mPlaylistView: View
    private lateinit var mPlaylistAnimator: Animator

    private lateinit var mPlaylistToolbar: Toolbar

    private val mPlaylistDataSource = dataSourceOf()
    private val mSongsDataSource = dataSourceOf()

    private lateinit var mAlbumsRecyclerView: RecyclerView
    private lateinit var mPlaylistRecyclerViewLayoutManager: LinearLayoutManager
    private lateinit var mSongsRecyclerView: RecyclerView

    private lateinit var mUIControlInterface: UIControlInterface

    private var sLandscape = false

    private val mPlaylist = musicLibrary.playlist?.keys?.toList()

    private var mSelectedPlaylist: String? = null
    private var mSelectedPlaylistMusic: List<Pair<Music, Int>>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mSelectedPlaylist = mPlaylist?.get(0)
        arguments?.getString(TAG_PLAYED_PLAYLIST)?.let { playlistName ->
            mSelectedPlaylist = playlistName
        }

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mUIControlInterface = activity as UIControlInterface
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    fun onHandleBackPressed(): Animator {
        if (!mPlaylistAnimator.isRunning) mPlaylistAnimator =
            mPlaylistView.createCircularReveal(isCentered = false, show = false)
        return mPlaylistAnimator
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSelectedPlaylistMusic = musicLibrary.playlist?.get(mSelectedPlaylist)


        mPlaylistView = view
        mPlaylistToolbar = playlist_toolbar
        mAlbumsRecyclerView = playlist_rv

        mSongsRecyclerView = playlist_songs_rv

        context?.let { cxt ->

            sLandscape = ThemeHelper.isDeviceLand(cxt.resources)

            mPlaylistToolbar.apply {

                overflowIcon = AppCompatResources.getDrawable(cxt, R.drawable.ic_shuffle)

                setNavigationOnClickListener {
                    activity?.onBackPressed()
                }

                setupMenu()
            }

            setupPlaylistContainer(cxt)

            setSongsDataSource(mSelectedPlaylistMusic)

            mSongsRecyclerView.apply {

                // setup{} is an extension method on RecyclerView
                setup {
                    // item is a `val` in `this` here
                    withDataSource(mSongsDataSource)

                    if (sLandscape)
                        withLayoutManager(GridLayoutManager(cxt, 2))
                    else
                        addItemDecoration(ThemeHelper.getRecyclerViewDivider(cxt))

                    withItem<Pair<Music, Int>, SongsViewHolder>(R.layout.song_item_alt) {
                        onBind(::SongsViewHolder) { _, item ->
                            val music = item.first
                            // GenericViewHolder is `this` here
                            title.text = ThemeHelper.buildSpanned(
                                getString(
                                    R.string.track_song,
                                    music.track.toFormattedTrack(),
                                    music.title
                                )
                            )
                            duration.text = ThemeHelper.buildSpanned(
                                getString(
                                    R.string.loved_song_subtitle,
                                    item.second.toLong().toFormattedDuration(false),
                                    music.duration.toFormattedDuration(false)
                                )
                            )

                            music.duration.toFormattedDuration(false)
                            subtitle.text =
                                getString(R.string.artist_and_album, music.artist, music.album)
                        }

                        onClick {

                            mUIControlInterface.onPlayFromPlaylist(
                                mSelectedPlaylist,
                                item,
                                mSelectedPlaylistMusic?.map { it.first })

                        }
                    }
                }
            }

            view.afterMeasured {
                mPlaylistAnimator =
                    mPlaylistView.createCircularReveal(isCentered = false, show = true)
            }
        }
    }

    private fun setPlaylistDataSource(playlistList: List<String>?) {
        playlistList?.apply {
            mPlaylistDataSource.set(this)
        }
    }

    private fun setSongsDataSource(musicList: List<Pair<Music, Int>>?) {
        musicList?.apply {
            mSongsDataSource.set(this)
        }
    }

    private fun setupMenu() {

        mPlaylistToolbar.apply {

            inflateMenu(R.menu.menu_artist_details)

            // inflateMenu(menuToInflate)

            menu.apply {
                findItem(R.id.action_shuffle_am).isEnabled =
                    mPlaylist?.size!! >= 2
            }

            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_shuffle_am -> mUIControlInterface.onShuffleSongs(
                        musicLibrary.rawPlaylist.map { it.song.first }.toMutableList(),
                        false
                    )
                    R.id.action_shuffle_sa -> mUIControlInterface.onShuffleSongs(
                        mSelectedPlaylistMusic?.map { it.first }?.toMutableList(),
                        false
                    )
                }
                return@setOnMenuItemClickListener true
            }
        }
    }

    private fun setupPlaylistContainer(context: Context) {

        setPlaylistDataSource(mPlaylist)

        mAlbumsRecyclerView.apply {

            setup {

                withDataSource(mPlaylistDataSource)

                mPlaylistRecyclerViewLayoutManager = LinearLayoutManager(
                    context,
                    if (sLandscape) LinearLayoutManager.VERTICAL else LinearLayoutManager.HORIZONTAL,
                    false
                )
                withLayoutManager(mPlaylistRecyclerViewLayoutManager)

                withItem<String, PlaylistHolder>(R.layout.playlist_item) {

                    onBind(::PlaylistHolder) { _, item ->
                        // AlbumsViewHolder is `this` here
                        playlist.text = item
                        playlist.isChecked = mSelectedPlaylist == item
                        playlist.isClickable = mSelectedPlaylist != item
                    }

                    onClick { index ->

                        mAlbumsRecyclerView.adapter?.apply {
                            notifyItemChanged(
                                mPlaylist?.indexOf(mSelectedPlaylist)!!
                            )

                            notifyItemChanged(index)
                            mSelectedPlaylist = item
                            swapPlaylist(musicLibrary.playlist?.get(item))
                        }
                    }
                }
            }

            mPlaylist?.indexOf(mSelectedPlaylist)?.let { pos ->
                if (pos != 0) mPlaylistRecyclerViewLayoutManager.scrollToPositionWithOffset(
                    pos,
                    0
                )
            }

        }
    }

    private fun swapPlaylist(songs: List<Pair<Music, Int>>?) {
        mSelectedPlaylistMusic = songs
        setSongsDataSource(songs)
        mSongsRecyclerView.scrollToPosition(0)
    }

    companion object {

        private const val TAG_PLAYED_PLAYLIST = "SELECTED_PLAYLIST"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment DetailsFragment.
         */
        @JvmStatic
        fun newInstance(playedPlaylist: String?) = PlaylistFragment().apply {
            playedPlaylist?.let { playlistName ->
                arguments = Bundle().apply {
                    putString(TAG_PLAYED_PLAYLIST, playlistName)
                }
            }
        }
    }
}
