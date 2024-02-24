package com.example.magnifyingglass.magnifier.ui.activites

import androidx.appcompat.app.AppCompatActivity
import com.example.magnifyingglass.magnifier.utils.RemoteConfigViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

open class BaseActivity : AppCompatActivity() {
    val remoteConfigViewModel: RemoteConfigViewModel by viewModel()
}