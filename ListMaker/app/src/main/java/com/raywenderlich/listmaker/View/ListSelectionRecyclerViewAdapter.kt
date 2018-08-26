package com.raywenderlich.listmaker.View

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.raywenderlich.listmaker.Model.TaskList
import com.raywenderlich.listmaker.R

class ListSelectionRecyclerViewAdapter(var lists: ArrayList<TaskList>, val onClick: (TaskList) -> Unit)
    : RecyclerView.Adapter<ListSelectionViewHolder>() {

    override fun getItemCount(): Int {
        return lists.size
    }

    override fun onBindViewHolder(holder: ListSelectionViewHolder, position: Int) {
        holder.listPosition.text = (position + 1).toString()
        holder.listTitle.text = lists[position].name
        holder.itemView.setOnClickListener { _ -> onClick(lists[position]) }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListSelectionViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_selection_view_holder, parent, false)

        return ListSelectionViewHolder(view)
    }

    fun addList(list: TaskList) {
        lists.add(list)
        notifyDataSetChanged()
    }
}

interface ListSelectionRecyclerViewClickListener {
    fun listItemClicked(list: TaskList)
}