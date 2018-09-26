package com.raywenderlich.podplay.ui

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.bumptech.glide.Glide
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.repository.PodcastRepo
import com.raywenderlich.podplay.viewmodel.PodcastViewModel
import kotlinx.android.synthetic.main.fragment_podcast_details.*

class PodcastDetailsFragment: Fragment() {
    private lateinit var podcastViewModel: PodcastViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        setupViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_podcast_details, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.menu_details, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        updateControls()
    }

    private fun updateControls() {
        podcastViewModel.podcastView?.let {
            feedTitleTextView.text = it.feedTitle
            feedDescTextView.text = it.feedDesc

            Glide.with(requireActivity())
                    .load(it.imageUrl)
                    .into(feedImageView)
        }
    }

    private fun setupViewModel() {
        podcastViewModel = ViewModelProviders.of(requireActivity())
                .get(PodcastViewModel::class.java)
    }

    companion object {
        fun newInstance(): PodcastDetailsFragment {
            return PodcastDetailsFragment()
        }
    }
}