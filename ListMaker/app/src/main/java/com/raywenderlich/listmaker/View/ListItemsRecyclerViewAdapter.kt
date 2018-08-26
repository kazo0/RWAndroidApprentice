package com.raywenderlich.listmaker.View

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.raywenderlich.listmaker.Model.TaskList
import com.raywenderlich.listmaker.R

class ListItemsRecyclerViewAdapter(var list: TaskList) : RecyclerView.Adapter<ListItemsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemsViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.task_view_holder, parent, false)
        return ListItemsViewHolder(view)
    }

    override fun getItemCount() = list.tasks.size

    override fun onBindViewHolder(holder: ListItemsViewHolder, position: Int) {
        holder.textTextView.text = list.tasks[position]
    }
}