package com.raywenderlich.placebook.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
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
}