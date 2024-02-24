package com.example.magnifyingglass.magnifier.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.utils.getWindowWidth
import com.example.magnifyingglass.magnifier.databinding.ImagePickerBinding

class ImagePickerDialog(val context: Activity, private val imagePickerClicks: ImagePickerClicks)
    : Dialog(context), View.OnClickListener {
    lateinit var binding : ImagePickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ImagePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setLayout(
            context.getWindowWidth(0.85f),
            WRAP_CONTENT
        )
        binding.llCamera.setOnClickListener(this@ImagePickerDialog)
        binding.llGallery.setOnClickListener(this@ImagePickerDialog)
        binding.ivClose.setOnClickListener(this@ImagePickerDialog)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.llCamera -> imagePickerClicks.openCamera()
            R.id.llGallery -> imagePickerClicks.openGallery()
            R.id.ivClose -> dismiss()
        }
        dismiss()
    }

    interface ImagePickerClicks {
        fun openCamera()
        fun openGallery()
    }
}
