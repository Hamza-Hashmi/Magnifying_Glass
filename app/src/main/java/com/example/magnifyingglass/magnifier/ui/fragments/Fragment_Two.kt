package com.example.magnifyingglass.magnifier.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.ads.loadAndReturnAd
import com.example.magnifyingglass.magnifier.ads.loadAndShowNativeAd
import com.example.magnifyingglass.magnifier.ads.obNativeAd1
import com.example.magnifyingglass.magnifier.ads.obNativeAd2
import com.example.magnifyingglass.magnifier.ads.obNativeAd3
import com.example.magnifyingglass.magnifier.ads.showLoadedNativeAd
import com.example.magnifyingglass.magnifier.databinding.FragmentOneBinding
import com.example.magnifyingglass.magnifier.databinding.FragmentTwoBinding
import com.example.magnifyingglass.magnifier.ui.activites.OnBoardingScreen
import com.example.magnifyingglass.magnifier.utils.RemoteConfigViewModel
import com.example.magnifyingglass.magnifier.utils.isInternetConnected
import org.koin.androidx.viewmodel.ext.android.viewModel

class Fragment_Two : Fragment() {
    private lateinit var binding: FragmentTwoBinding
    val remoteConfigViewModel: RemoteConfigViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTwoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvNext.setOnClickListener {
            OnBoardingScreen.viewPager?.currentItem = 2
        }

        showNativeAd()
    }

    private fun showNativeAd() {
        activity?.apply {
            if (isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this)?.onBoardingNative?.value == 1) {
                binding.adFrame.visibility = View.VISIBLE
                obNativeAd2?.let { ad ->
                    showLoadedNativeAd(this, binding.adFrame, binding.shimmerFrameLayout.root, R.layout.native_ad_layout_main, ad)
                } ?: run {
                    loadAndShowNativeAd(
                        binding.adFrame,
                        binding.shimmerFrameLayout.root,
                        R.layout.native_ad_layout_main,
                        getString(R.string.onBoardingNativeId)
                    )
                }

                //Pre load 3rd
                loadAndReturnAd(this, getString(R.string.onBoardingNativeId)) { ad ->
                    obNativeAd3 = ad
                }
            } else {
                binding.adFrame.visibility = View.GONE
            }
        }
    }
}