package com.raywenderlich.podplay.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.service.ItunesService
import com.raywenderlich.podplay.service.PodcastResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PodcastActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast)

        val TAG = javaClass.simpleName
        val itunesService = ItunesService.instance
        val itunesRepo = ItunesRepo(itunesService)

        itunesRepo.searchByTerm("Android Developer") { podcasts ->
            Log.i(TAG, "Results = $podcasts")

        }
    }
}
