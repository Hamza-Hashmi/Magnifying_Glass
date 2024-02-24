package com.example.magnifyingglass.magnifier.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.magnifyingglass.magnifier.R

class Fragment_One : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment__one, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = Fragment_One()
    }
}