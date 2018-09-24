package com.raywenderlich.placebook.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.util.ImageUtils
import com.raywenderlich.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.android.synthetic.main.activity_bookmarks_details.*

class BookmarkDetailsActivity: AppCompatActivity() {
    private lateinit var bookmarkDetailsViewModel: BookmarkDetailsViewModel
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_bookmarks_details)
        setupToolbar()
        setupViewModel()
        getIntentData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_save -> {
                saveChanges()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun setupViewModel() {
        bookmarkDetailsViewModel = ViewModelProviders.of(this)
                .get(BookmarkDetailsViewModel::class.java)
    }

    private fun populateFields() {
        bookmarkDetailsView?.let {
            editTextName.setText(it.name)
            editTextPhone.setText(it.phone)
            editTextAddress.setText(it.address)
            editTextNotes.setText(it.notes)
        }
    }

    private fun populateImageView() {
        bookmarkDetailsView?.let {
            val image = it.getImage(this)
            image?.let {
                imageViewPlace.setImageBitmap(it)
            }
        }
    }

    private fun getIntentData() {
        val bookmarkId = intent.getLongExtra(MapsActivity.EXTRA_BOOKMARK_ID, 0)

        bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(
            this,
            Observer {
                it?.let {
                    bookmarkDetailsView = it
                    populateFields()
                    populateImageView()
                }
            }
        )
    }

    private fun saveChanges() {
        val name = editTextName.text.toString()
        if (name.isEmpty()) {
            return
        }

        bookmarkDetailsView?.let {
            it.name = name
            it.phone = editTextPhone.text.toString()
            it.address = editTextAddress.text.toString()
            it.notes = editTextNotes.text.toString()
            bookmarkDetailsViewModel.updateBookmark(it)
        }

        finish()
    }
}