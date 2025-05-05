package com.example.magnifyingglass.magnifier.ui.activites

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.databinding.OnBoardingScreenBinding
import com.example.magnifyingglass.magnifier.ui.adapters.ViewPagerAdapter
import com.example.magnifyingglass.magnifier.utils.isInternetConnected
import loadAndShowSplashInterstitial

class OnBoardingScreen : BaseActivity() {
    private val binding: OnBoardingScreenBinding by lazy {
        OnBoardingScreenBinding.inflate(layoutInflater)
    }

    companion object {
        var viewPager: ViewPager? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (remoteConfigViewModel.getRemoteConfig(this@OnBoardingScreen)?.InterstitialMain?.value == 1) {
            loadAndShowSplashInterstitial(
                true,
                getString(R.string.splashInterstitialId),
                getString(R.string.interstitialId)
            )
        }

        setContentView(binding.root)

        setUpViewPager()
    }

    private fun setUpViewPager() {
        viewPager = binding.viewPager

        val adapter = ViewPagerAdapter(supportFragmentManager, (remoteConfigViewModel.getRemoteConfig(this@OnBoardingScreen)?.InterstitialMain?.value == 1 && isInternetConnected()))
        viewPager?.adapter = adapter
    }

}