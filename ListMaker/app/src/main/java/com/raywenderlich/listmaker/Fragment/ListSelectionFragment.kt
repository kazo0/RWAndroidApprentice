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
import com.raywenderlich.listmaker.Model.ListDataManager
import com.raywenderlich.listmaker.Model.TaskList

import com.raywenderlich.listmaker.R
import com.raywenderlich.listmaker.View.ListSelectionRecyclerViewAdapter

class ListSelectionFragment : Fragment() {
    private var listener: OnListItemFragmentInteractionListener? = null

    lateinit var listDataManager : ListDataManager
    lateinit var listsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_selection, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val lists = listDataManager.readLists()
        view?.let {
            listsRecyclerView = it.findViewById(R.id.lists_recyclerview)
            listsRecyclerView.layoutManager = LinearLayoutManager(activity)
            listsRecyclerView.adapter = ListSelectionRecyclerViewAdapter(lists) { list -> listener?.onListItemClicked(list)}
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListItemFragmentInteractionListener) {
            listener = context
            listDataManager = ListDataManager(context)
        } else {
            throw RuntimeException(context.toString() + " must implement OnListItemFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun addList(list: TaskList) {
        listDataManager.saveList(list)

        val adapter = listsRecyclerView.adapter as? ListSelectionRecyclerViewAdapter
        adapter?.addList(list)
    }

    fun saveList(list: TaskList) {
        listDataManager.saveList(list)
        updateLists()
    }

    private fun updateLists() {
        val lists = listDataManager.readLists()
        listsRecyclerView.adapter = ListSelectionRecyclerViewAdapter(lists) { list -> listener?.onListItemClicked(list) }
    }


    interface OnListItemFragmentInteractionListener {
        fun onListItemClicked(list: TaskList)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                ListSelectionFragment()
    }
}
