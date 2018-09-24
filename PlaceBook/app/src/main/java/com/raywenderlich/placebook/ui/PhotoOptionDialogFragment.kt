package com.raywenderlich.placebook.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.DialogFragment

class PhotoOptionDialogFragment : DialogFragment() {
    interface PhotoOptionDialogListener {
        fun onCaptureClick()
        fun onPickClick()
    }

    private lateinit var listener: PhotoOptionDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        listener = activity as PhotoOptionDialogListener

        var captureSelectIdx = -1
        var pickSelectIdx = -1

        val options = ArrayList<String>()

        if (canCapture(this.context)) {
            options.add("Camera")
            captureSelectIdx = 0
        }

        if (canPick(this.context)) {
            options.add("Gallery")
            pickSelectIdx = if (captureSelectIdx == -1) 0 else 1
        }

        return AlertDialog.Builder(activity)
                .setTitle("Photo Option")
                .setItems(options.toTypedArray()) { _, which ->
                    if (which == captureSelectIdx) {
                        listener.onCaptureClick()
                    } else if (which == pickSelectIdx) {
                        listener.onPickClick()
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
    }

    companion object {
        fun canPick(context: Context?): Boolean {
            if (context != null) {
                val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                return pickIntent.resolveActivity(context.packageManager) != null
            }
            return false
        }

        fun canCapture(context: Context?): Boolean {
            if (context != null) {
                val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                return captureIntent.resolveActivity(context.packageManager) != null
            }
            return false
        }

        fun newInstance(context: Context) =
                if (canCapture(context) || canPick(context)) {
                    PhotoOptionDialogFragment()
                } else {
                    null
                }
    }
}