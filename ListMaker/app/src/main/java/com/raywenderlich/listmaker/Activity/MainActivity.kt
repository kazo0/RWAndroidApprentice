package com.raywenderlich.listmaker.Activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import com.raywenderlich.listmaker.Fragment.ListDetailFragment
import com.raywenderlich.listmaker.Fragment.ListSelectionFragment
import com.raywenderlich.listmaker.View.ListSelectionRecyclerViewAdapter
import com.raywenderlich.listmaker.Model.ListDataManager
import com.raywenderlich.listmaker.Model.TaskList
import com.raywenderlich.listmaker.R
import com.raywenderlich.listmaker.View.ListItemsRecyclerViewAdapter

import kotlinx.android.synthetic.main.activity_list.*

class MainActivity : AppCompatActivity(), ListSelectionFragment.OnListItemFragmentInteractionListener {

    private var listSelectionFragment = ListSelectionFragment.newInstance()
    private var mainFragmentContainer: FrameLayout? = null
    private var secondaryFragmentContainer: FrameLayout? = null
    private var largeScreen = false
    private var listFragment: ListDetailFragment? = null

    companion object {
        val INTENT_LIST_KEY = "list"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        setSupportActionBar(toolbar)

        mainFragmentContainer = findViewById(R.id.main_fragment_container)
        secondaryFragmentContainer = findViewById(R.id.secondary_fragment_container)
        largeScreen = secondaryFragmentContainer != null

        supportFragmentManager
                .beginTransaction()
                .add(R.id.main_fragment_container, listSelectionFragment)
                .commit()

        fab.setOnClickListener { view ->
            showCreateListDialog()
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onListItemClicked(list: TaskList) {
        showListDetail(list)
    }

    private fun showCreateListDialog() {
        val listTitleEditText = EditText(this)
        listTitleEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS

        AlertDialog.Builder(this)
                .setTitle(R.string.name_of_list)
                .setView(listTitleEditText)
                .setPositiveButton(R.string.create_list) { dialog, _ ->
                    val list = TaskList(listTitleEditText.text.toString())
                    listSelectionFragment.addList(list)

                    dialog.dismiss()
                    showListDetail(list)
                }
                .show()
    }

    private fun showListDetail(list: TaskList) {
        title = list.name
        listFragment = ListDetailFragment.newInstance(list)
        val fragmentContainerId = if (largeScreen)  R.id.secondary_fragment_container else R.id.main_fragment_container

        listFragment?.let {
            supportFragmentManager.beginTransaction()
                    .replace(fragmentContainerId, it, getString(R.string.list_fragment_tag))
                    .addToBackStack(null)
                    .commit()
        }

        fab.setOnClickListener { _ -> showCreateTaskDialog() }
    }

    private fun showCreateTaskDialog() {
        val taskEditText = EditText(this)
        taskEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS

        AlertDialog.Builder(this)
                .setTitle(R.string.task_to_add)
                .setView(taskEditText)
                .setPositiveButton(R.string.add_task) { dialog, _ ->
                    val task = taskEditText.text.toString()
                    listFragment?.addTask(task)
                    dialog.dismiss()
                }
                .show()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        title = getString(R.string.app_name)

        listFragment?.let {

            listSelectionFragment?.listDataManager?.saveList(it.list)
            supportFragmentManager
                    .beginTransaction()
                    .remove(it)
                    .commit()

            listFragment = null
            val listRecyclerAdapter = listSelectionFragment?.listsRecyclerView.adapter as? ListSelectionRecyclerViewAdapter
            listRecyclerAdapter?.let {
                it.lists = listSelectionFragment?.listDataManager?.readLists()
                it.notifyDataSetChanged()
            }
        }
        fab.setOnClickListener { _ -> showCreateListDialog()}
    }
}
