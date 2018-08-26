package com.raywenderlich.listmaker.Fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.raywenderlich.listmaker.Activity.MainActivity
import com.raywenderlich.listmaker.Model.TaskList

import com.raywenderlich.listmaker.R
import com.raywenderlich.listmaker.View.ListItemsRecyclerViewAdapter

class ListDetailFragment : Fragment() {
    lateinit var list: TaskList
    lateinit var listItemsRecyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            list = it.getParcelable(MainActivity.INTENT_LIST_KEY)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_list_detail, container, false)
        view?.let {
            listItemsRecyclerView = it.findViewById(R.id.list_items_recyclerview)
            listItemsRecyclerView.layoutManager = LinearLayoutManager(activity)
            listItemsRecyclerView.adapter = ListItemsRecyclerViewAdapter(list)
        }

        return view
    }

    fun addTask(item: String) {
        list.tasks.add(item)

        val listRecyclerAdapter = listItemsRecyclerView.adapter as? ListItemsRecyclerViewAdapter
        listRecyclerAdapter?.let {
            it.list = list
            it.notifyDataSetChanged()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(list: TaskList) =
                ListDetailFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(MainActivity.INTENT_LIST_KEY, list)
                    }
                }
    }
}
