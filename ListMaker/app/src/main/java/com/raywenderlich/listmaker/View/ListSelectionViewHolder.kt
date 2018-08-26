package com.raywenderlich.listmaker.View

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.raywenderlich.listmaker.R

class ListSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val listPosition = itemView.findViewById<TextView>(R.id.itemNumber)
    val listTitle = itemView.findViewById<TextView>(R.id.itemString)
}