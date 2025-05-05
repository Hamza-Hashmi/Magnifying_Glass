package com.example.magnifyingglass.magnifier.ui.activites

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.databinding.ActivityBillingBinding

class BillingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBillingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}