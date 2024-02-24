package com.example.magnifyingglass.magnifier.ui.models

import android.net.Uri

data class ImagesModel(
    val uri: Uri,
    val name: String,
    var isSelected:Boolean =false
)
