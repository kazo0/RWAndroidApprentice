package com.raywenderlich.podplay.adapter

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.viewmodel.SearchViewModel

class PodcastListAdapter(private var podcastSummaryViewList: List<SearchViewModel.PodcastSummaryView>?,
                         private val podcastListAdapterListener: PodcastListAdapterListener,
                         private val parentActivity: Activity) :
        RecyclerView.Adapter<PodcastListAdapter.ViewHolder>() {

    interface PodcastListAdapterListener {
        fun onShowDetails(podcastSummaryViewData:
                          SearchViewModel.PodcastSummaryView)
    }

    inner class ViewHolder(v: View,
                           private val podcastListAdapterListener: PodcastListAdapterListener) :
            RecyclerView.ViewHolder(v) {

        var podcastSummaryViewData: SearchViewModel.PodcastSummaryView? = null
        val nameTextView: TextView = v.findViewById(R.id.podcastNameTextView)
        val lastUpdatedTextView: TextView = v.findViewById(R.id.podcastLastUpdatedTextView)
        val podcastImageView: ImageView = v.findViewById(R.id.podcastImage)

        init {
            v.setOnClickListener {
                podcastSummaryViewData?.let {
                    podcastListAdapterListener.onShowDetails(it)
                }
            } }
    }

    fun setSearchData(podcastSummaryViewData:
                      List<SearchViewModel.PodcastSummaryView>) {
        podcastSummaryViewList = podcastSummaryViewData
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int):
            ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.search_item, parent, false),
                podcastListAdapterListener)
    }

    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {
        val searchViewList = podcastSummaryViewList ?: return
        val searchView = searchViewList[position]
        holder.podcastSummaryViewData = searchView
        holder.nameTextView.text = searchView.name
        holder.lastUpdatedTextView.text = searchView.lastUpdated

        Glide.with(parentActivity)
                .load(searchView.imageUrl)
                .into(holder.podcastImageView)
    }

    override fun getItemCount(): Int {
        return podcastSummaryViewList?.size ?: 0
    }
}