package com.raywenderlich.placebook.ui

import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.ui.PhotoOptionDialogFragment.PhotoOptionDialogListener
import com.raywenderlich.placebook.util.ImageUtils
import com.raywenderlich.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.android.synthetic.main.activity_bookmarks_details.*
import kotlinx.android.synthetic.main.content_bookmark_info.*
import java.io.File
import java.io.IOException
import java.net.URLEncoder

class BookmarkDetailsActivity: AppCompatActivity(), PhotoOptionDialogListener {
    private lateinit var bookmarkDetailsViewModel: BookmarkDetailsViewModel
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_bookmarks_details)
        setupToolbar()
        setupViewModel()
        getIntentData()
        setupFab()
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
            R.id.action_delete -> {
                deleteBookmark()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCaptureClick() {
        photoFile = null
        try {
            photoFile = ImageUtils.createUniqueImageFile(this)
            if (photoFile == null) {
                return
            }
        } catch (ex: IOException) {
            return
        }

        photoFile?.let { photoFile ->
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoUri = FileProvider.getUriForFile(this, "com.raywenderlich.placebook.fileprovider", photoFile)

            captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri)

            val intentActivities = packageManager.queryIntentActivities(
                    captureIntent,
                    PackageManager.MATCH_DEFAULT_ONLY)
            intentActivities.map { it.activityInfo.packageName }
                    .forEach { grantUriPermission(it, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }

            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)
        }
    }
    override fun onPickClick() {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(pickIntent, REQUEST_GALLERY_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAPTURE_IMAGE -> {
                    photoFile?.let {
                        val uri = FileProvider.getUriForFile(
                                this,
                                "com.raywenderlich.placebook.fileprovider",
                                it
                        )
                        revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        val image = getImageWithPath(it.absolutePath)
                        image?.let { updateImage(it) }
                    }
                }
                REQUEST_GALLERY_IMAGE -> {
                    if (data?.data != null) {
                        val imageUri = data.data
                        val image = getImageWithAuthority(imageUri)
                        image?.let { updateImage(it) }
                    }
                }
            }
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
            imageViewPlace.setOnClickListener { replaceImage() }
        }
    }

    private fun populateCategories() {
        bookmarkDetailsView?.let { bookmarkView ->
            val resourceId = bookmarkDetailsViewModel.getCategoryResourceId(bookmarkView.category)
            resourceId?.let { imageViewCategory.setImageResource(it) }

            val categories = bookmarkDetailsViewModel.getCategories()
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinnerCategory.adapter = adapter
            val category = bookmarkView.category

            spinnerCategory.setSelection(adapter.getPosition(category))

            spinnerCategory.post {
                spinnerCategory.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View,
                                                position: Int, id: Long) {
                        val category = parent.getItemAtPosition(position) as String
                        val resourceId =
                                bookmarkDetailsViewModel.getCategoryResourceId(category)
                        resourceId?.let {
                            imageViewCategory.setImageResource(it) }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // NOTE: This method is required but not used.
                    } }
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
                    populateCategories()
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
            it.category = spinnerCategory.selectedItem as String
            bookmarkDetailsViewModel.updateBookmark(it)
        }

        finish()
    }

    private fun replaceImage() {
        PhotoOptionDialogFragment
                .newInstance(this)
                ?.show(supportFragmentManager, "photoOptionDialog")
    }

    private fun updateImage(image: Bitmap) {
        bookmarkDetailsView?.let { bookmarkView ->
            imageViewPlace.setImageBitmap(image)
            bookmarkView.setImage(this, image)
        }
    }

    private fun getImageWithPath(filePath: String): Bitmap? {
        return ImageUtils.decodeFileToSize(filePath,
                resources.getDimensionPixelSize(
                        R.dimen.default_image_width),
                resources.getDimensionPixelSize(
                        R.dimen.default_image_height))
    }

    private fun getImageWithAuthority(uri: Uri): Bitmap? {
        return ImageUtils.decodeUriStreamToSize(uri,
                resources.getDimensionPixelSize(
                        R.dimen.default_image_width),
                resources.getDimensionPixelSize(
                        R.dimen.default_image_height),
                this)
    }

    private fun deleteBookmark() {
        bookmarkDetailsView?.let {
            AlertDialog.Builder(this)
                    .setMessage("Delete?")
                    .setPositiveButton("Ok") { _, _ ->
                        bookmarkDetailsViewModel.deleteBookmark(it)
                        finish()
                    }
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show()
        }
    }

    private fun sharePlace() {
        bookmarkDetailsView?.let { bookmarkView ->
            var mapUrl = if (bookmarkView.placeId == null) {
                val location = URLEncoder.encode("${bookmarkView.latitiude},${bookmarkView.longitude}", "utf-8")
                "https://www.google.com/maps/dir/?api=1&destination=$location"
            } else {
                val name = URLEncoder.encode(bookmarkView.name, "utf-8")
                "https://www.google.com/maps/dir/?api=1&destination=$name&destination_place_id=${bookmarkView.placeId}"
            }

            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Check out ${bookmarkView.name} at:\n$mapUrl")
            sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                    "Sharing ${bookmarkView.name}")
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
        }
    }

    private fun setupFab() {
        fab.setOnClickListener { sharePlace() }
    }

    companion object {
        private const val REQUEST_CAPTURE_IMAGE = 1
        private const val REQUEST_GALLERY_IMAGE = 2
    }
}