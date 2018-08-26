package com.raywenderlich.listmaker.Model

import android.content.Context
import android.preference.PreferenceManager

class ListDataManager(val context: Context) {
    fun saveList(list: TaskList) {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()

        sharedPreferences.putStringSet(list.name, list.tasks.toHashSet())
        sharedPreferences.apply()
    }

    fun readLists() : ArrayList<TaskList> {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context)

        val sharedPrefContents = sharedPreferences.all
        val tasksLists = ArrayList<TaskList>()

        for ((name, list) in sharedPrefContents) {
            val itemsHashSet = list as HashSet<String>
            val list = TaskList(name, ArrayList(itemsHashSet))
            tasksLists.add(list)
        }
        return tasksLists
    }
}