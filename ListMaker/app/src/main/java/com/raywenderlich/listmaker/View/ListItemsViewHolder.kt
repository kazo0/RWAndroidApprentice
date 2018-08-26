package com.raywenderlich.listmaker.View

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.raywenderlich.listmaker.R

class ListItemsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textTextView = itemView.findViewById<TextView>(R.id.textView_task)
}