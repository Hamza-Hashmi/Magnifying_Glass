package com.example.magnifyingglass.magnifier.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.magnifyingglass.magnifier.Language.LanguageActivity
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.ads.loadAndShowNativeAd
import com.example.magnifyingglass.magnifier.ads.obNativeAd3
import com.example.magnifyingglass.magnifier.ads.showLoadedNativeAd
import com.example.magnifyingglass.magnifier.databinding.FragmentFourBinding
import com.example.magnifyingglass.magnifier.ui.activites.OnBoardingScreen
import com.example.magnifyingglass.magnifier.utils.RemoteConfigViewModel
import com.example.magnifyingglass.magnifier.utils.isInternetConnected
import org.koin.androidx.viewmodel.ext.android.viewModel

class Fragment_Four : Fragment() {
    private lateinit var binding: FragmentFourBinding
    val remoteConfigViewModel: RemoteConfigViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFourBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvNext.setOnClickListener {
            activity?.apply {
                startActivity(Intent(this, LanguageActivity::class.java))
                finish()
            }
        }

        showNativeAd()
    }

    private fun showNativeAd() {
        activity?.apply {
            if (isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this)?.onBoardingNative?.value == 1) {
                binding.adFrame.visibility = View.VISIBLE
                obNativeAd3?.let { ad ->
                    showLoadedNativeAd(this, binding.adFrame, binding.shimmerFrameLayout.root, R.layout.native_ad_layout_main, ad)
                } ?: run {
                    loadAndShowNativeAd(
                        binding.adFrame,
                        binding.shimmerFrameLayout.root,
                        R.layout.native_ad_layout_main,
                        getString(R.string.onBoardingNativeId)
                    )
                }
            } else {
                binding.adFrame.visibility = View.GONE
            }
        }
    }
}