package com.raywenderlich.placebook.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.ui.MapsActivity
import com.raywenderlich.placebook.viewmodel.MapsViewModel

class BookmarkListAdapter(
        private var bookmarkData: List<MapsViewModel.BookmarkView>?,
        private val mapsActivity: MapsActivity)  : RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>() {

    fun setBookmarkData(bookmarks: List<MapsViewModel.BookmarkView>) {
        bookmarkData = bookmarks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.bookmark_item, parent, false),
                mapsActivity)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bookmarkData?.let {
            val bookmarkView = it[position]

            holder.itemView.tag = bookmarkView
            holder.nameTextView.text = bookmarkView.name
            bookmarkView.categoryResourceId?.let {
                holder.categoryImageView.setImageResource(it)
            }

        }
    }

    override fun getItemCount(): Int {
        return bookmarkData?.size ?: 0
    }

    class ViewHolder(v: View, private val mapsActivity: MapsActivity) : RecyclerView.ViewHolder(v) {
        init {
            v.setOnClickListener { _ ->
                val bookmarkView = itemView.tag as MapsViewModel.BookmarkView
                mapsActivity.moveToBookmark(bookmarkView)
            }
        }
        val nameTextView = v.findViewById<TextView>(R.id.bookmarkNameTextView)
        val categoryImageView = v.findViewById<ImageView>(R.id.bookmarkIcon)
    }
}