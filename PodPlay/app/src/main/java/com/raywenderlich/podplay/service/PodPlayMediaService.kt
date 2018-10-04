package com.raywenderlich.podplay.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.ui.PodcastActivity

class PodPlayMediaService : MediaBrowserServiceCompat(), PodPlayMediaCallback.PodPlayMediaListener {

    private lateinit var mediaSession: MediaSessionCompat

    override fun onCreate() {
        super.onCreate()
        createMediaSession()
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot(
                PODPLAY_EMPTY_ROOT_MEDIA_ID, null)
    }

    override fun onStateChanged() {
        displayNotification()
    }

    override fun onStopPlaying() {
        stopSelf()
        stopForeground(true)
    }

    override fun onPausePlaying() {
        stopForeground(false)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        mediaSession.controller.transportControls.stop()
    }

    private fun createMediaSession() {
        mediaSession = MediaSessionCompat(this, "PodPlayMediaService")
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        sessionToken = mediaSession.sessionToken
        val callback = PodPlayMediaCallback(this, mediaSession)
        callback.listener = this
        mediaSession.setCallback(callback)
    }

    private fun getPausePlayActions():
            Pair<NotificationCompat.Action, NotificationCompat.Action>  {
        val pauseAction = NotificationCompat.Action(
                R.drawable.ic_pause_white, getString(R.string.pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_PAUSE))
        val playAction = NotificationCompat.Action(
                R.drawable.ic_play_arrow_white, getString(R.string.play),
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_PLAY))
        return Pair(pauseAction, playAction)
    }

    private fun isPlaying(): Boolean {
        if (mediaSession.controller.playbackState != null) {
            return mediaSession.controller.playbackState.state ==
                    PlaybackStateCompat.STATE_PLAYING
        } else {
            return false
        }
    }

    private fun getNotificationIntent(): PendingIntent {
        val openActivityIntent = Intent(this, PodcastActivity::class.java)
        openActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(
                this@PodPlayMediaService, 0, openActivityIntent,
                PendingIntent.FLAG_CANCEL_CURRENT)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel()
    {
        val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager
        if (notificationManager.getNotificationChannel(PLAYER_CHANNEL_ID)
                == null) {
            val channel = NotificationChannel(PLAYER_CHANNEL_ID, "Player",
                    NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(mediaDescription: MediaDescriptionCompat,
                                   bitmap: Bitmap?): Notification {
        val notificationIntent = getNotificationIntent()
        val (pauseAction, playAction) = getPausePlayActions()
        val notification = NotificationCompat.Builder(
                this@PodPlayMediaService, PLAYER_CHANNEL_ID)
        notification
                .setContentTitle(mediaDescription.title)
                .setContentText(mediaDescription.subtitle)
                .setLargeIcon(bitmap)
                .setContentIntent(notificationIntent)
                .setDeleteIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                                PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_episode_icon)
                .addAction(if (isPlaying()) pauseAction else playAction)
                .setStyle(
                        android.support.v4.media.app.NotificationCompat.MediaStyle()
                                .setMediaSession(mediaSession.sessionToken)
                                .setShowActionsInCompactView(0)
                                .setShowCancelButton(true)
                                .setCancelButtonIntent(
                                        MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                                                PlaybackStateCompat.ACTION_STOP)))
        return notification.build()
    }

    private fun displayNotification() {
        if (mediaSession.controller.metadata == null) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        val mediaDescription = mediaSession.controller.metadata.description
        Glide.with(this)
                .asBitmap()
                .load(mediaDescription.iconUri)
                .into(object : SimpleTarget<Bitmap>() {
                    // 5
                    override fun onResourceReady(resource: Bitmap,
                                                 transition: Transition<in Bitmap>?) {

                        val notification = createNotification(mediaDescription,
                                resource)

                        ContextCompat.startForegroundService(
                                this@PodPlayMediaService,
                                Intent(this@PodPlayMediaService,
                                        PodPlayMediaService::class.java))

                        startForeground(NOTIFICATION_ID, notification)
                    }
                }) }


    companion object {
        private const val PODPLAY_EMPTY_ROOT_MEDIA_ID = "podplay_empty_root_media_id"
        private const val PLAYER_CHANNEL_ID = "podplay_player_channel"
        private const val NOTIFICATION_ID = 1
    }
}