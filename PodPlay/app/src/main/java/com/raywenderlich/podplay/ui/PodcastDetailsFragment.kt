package com.raywenderlich.podplay.ui

import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.method.ScrollingMovementMethod
import android.view.*
import com.bumptech.glide.Glide
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.adapter.EpisodeListAdapter
import com.raywenderlich.podplay.service.PodPlayMediaService
import com.raywenderlich.podplay.util.HtmlUtils
import com.raywenderlich.podplay.viewmodel.PodcastViewModel
import kotlinx.android.synthetic.main.fragment_podcast_details.*

class PodcastDetailsFragment: Fragment(), EpisodeListAdapter.EpisodeListAdapterListener {
    private lateinit var podcastViewModel: PodcastViewModel
    private lateinit var episodeListAdapter: EpisodeListAdapter
    private var listener: OnPodcastDetailsListener? = null
    private var menuItem: MenuItem? = null
    private lateinit var mediaBrowser: MediaBrowserCompat
    private var mediaControllerCallback: MediaControllerCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        setupViewModel()
        initMediaBrowser()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnPodcastDetailsListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() +
                    " must implement OnPodcastDetailsListener")
        } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_podcast_details, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.menu_details, menu)
        menuItem = menu?.findItem(R.id.menu_feed_action)
        updateMenuItem()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_feed_action -> {
                podcastViewModel.activePodcast?.feedUrl?.let {
                    if (podcastViewModel.podcastView?.subscribed == true) {
                        listener?.onUnsubscribe()
                    } else {
                        listener?.onSubscribe()
                    }
                }
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupControls()
        updateControls()
    }

    override fun onStart() {
        super.onStart()
        if (mediaBrowser.isConnected) {
            if (MediaControllerCompat.getMediaController(requireActivity()) == null) {
                registerMediaController(mediaBrowser.sessionToken)
            }
        } else {
            mediaBrowser.connect()
        }
    }

    override fun onStop() {
        super.onStop()
        if (MediaControllerCompat.getMediaController(requireActivity()) != null) {
            mediaControllerCallback?.let {
                MediaControllerCompat.getMediaController(requireActivity())
                        .unregisterCallback(it)
            }
        }
    }

    override fun onSelectedEpisode(episodeView: PodcastViewModel.EpisodeView) {
        val controller = MediaControllerCompat.getMediaController(requireActivity())

        if (controller.playbackState != null) {
            if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                controller.transportControls.pause()
            } else {
                startPlaying(episodeView)
            }
        } else {
            startPlaying(episodeView)
        }
    }

    private fun updateControls() {
        podcastViewModel.podcastView?.let {
            feedTitleTextView.text = HtmlUtils.htmlToSpannable(it.feedTitle ?: "")
            feedDescTextView.text = HtmlUtils.htmlToSpannable(it.feedDesc ?: "")

            Glide.with(requireActivity())
                    .load(it.imageUrl)
                    .into(feedImageView)
        }
    }

    private fun setupViewModel() {
        podcastViewModel = ViewModelProviders.of(requireActivity())
                .get(PodcastViewModel::class.java)
    }

    private fun setupControls() {
        feedDescTextView.movementMethod = ScrollingMovementMethod()

        episodeRecyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(activity)
        episodeRecyclerView.layoutManager = layoutManager
        val dividerItemDecoration =
                android.support.v7.widget.DividerItemDecoration(
                        episodeRecyclerView.context, layoutManager.orientation)
        episodeRecyclerView.addItemDecoration(dividerItemDecoration)

        episodeListAdapter = EpisodeListAdapter(
                podcastViewModel.podcastView?.episodes, this)
        episodeRecyclerView.adapter = episodeListAdapter
    }

    private fun updateMenuItem() {
        val viewData = podcastViewModel.podcastView ?: return
        menuItem?.title = if (viewData.subscribed)
            getString(R.string.unsubscribe) else getString(R.string.subscribe)
    }

    private fun registerMediaController(token: MediaSessionCompat.Token) {
        val mediaController = MediaControllerCompat(activity, token)
        MediaControllerCompat.setMediaController(requireActivity(), mediaController)

        mediaControllerCallback = MediaControllerCallback()
        mediaController.registerCallback(mediaControllerCallback!!)

    }

    private fun initMediaBrowser() {
        mediaBrowser = MediaBrowserCompat(activity, ComponentName(requireActivity(),
                PodPlayMediaService::class.java),
                MediaBrowserCallBacks(),
                null)


    }

    private fun startPlaying(episodeView: PodcastViewModel.EpisodeView) {
        val controller = MediaControllerCompat.getMediaController(requireActivity())
        val viewData = podcastViewModel.podcastView ?: return
        val bundle = Bundle()
        bundle.putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                episodeView.title)
        bundle.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,
                viewData.feedTitle)
        bundle.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                viewData.imageUrl)
        controller.transportControls.playFromUri(
                Uri.parse(episodeView.mediaUrl), bundle)
    }

    companion object {
        fun newInstance(): PodcastDetailsFragment {
            return PodcastDetailsFragment()
        }
    }

    interface OnPodcastDetailsListener {
        fun onSubscribe()
        fun onUnsubscribe()
    }

    inner class MediaControllerCallback: MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            println("metadata changed to ${metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)}")
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            println("state changed to $state")
        }
    }

    inner class MediaBrowserCallBacks:
            MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            registerMediaController(mediaBrowser.sessionToken)
            println("onConnected")
        }
        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            println("onConnectionSuspended")
        }
        override fun onConnectionFailed() {
            super.onConnectionFailed()
            println("onConnectionFailed")
        } }
}