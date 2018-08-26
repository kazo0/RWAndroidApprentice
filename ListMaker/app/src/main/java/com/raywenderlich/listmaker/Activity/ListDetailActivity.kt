package com.raywenderlich.listmaker.Activity

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.widget.EditText
import com.raywenderlich.listmaker.Model.TaskList
import com.raywenderlich.listmaker.R
import com.raywenderlich.listmaker.View.ListItemsRecyclerViewAdapter
import kotlinx.android.synthetic.main.task_view_holder.*

class ListDetailActivity : AppCompatActivity() {
    lateinit var list: TaskList
    lateinit var listItemsRecyclerView : RecyclerView
    lateinit var addTaskButton : FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_detail)

        list = intent.getParcelableExtra(MainActivity.INTENT_LIST_KEY)
        title = list.name

        listItemsRecyclerView = findViewById(R.id.list_items_recyclerview)
        listItemsRecyclerView.adapter = ListItemsRecyclerViewAdapter(list)
        listItemsRecyclerView.layoutManager = LinearLayoutManager(this)

        addTaskButton = findViewById(R.id.add_task_button)
        addTaskButton.setOnClickListener { _ -> showCreateTaskDialog() }
    }

    private fun showCreateTaskDialog() {
        val taskEditText = EditText(this)
        taskEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS

        AlertDialog.Builder(this)
                .setView(taskEditText)
                .setTitle(R.string.task_to_add)
                .setPositiveButton(R.string.add_task) {dialog, _ ->
                    val task = taskEditText.text.toString()
                    list.tasks.add(task)
                    val recyclerAdapter = listItemsRecyclerView.adapter as? ListItemsRecyclerViewAdapter
                    recyclerAdapter?.notifyItemInserted(list.tasks.size)
                    dialog.dismiss()
                }
                .show()
    }

    override fun onBackPressed() {
        val bundle = Bundle()
        bundle.putParcelable(MainActivity.INTENT_LIST_KEY, list)

        val intent = Intent()
        intent.putExtras(bundle)
        setResult(Activity.RESULT_OK, intent)

        super.onBackPressed()

    }
}
