package com.example.magnifyingglass.magnifier.ui.activites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.example.magnifyingglass.magnifier.Language.LanguageActivity
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.databinding.OnBoardingScreenBinding
import com.example.magnifyingglass.magnifier.ui.adapters.ViewPagerAdapter
import com.example.magnifyingglass.magnifier.utils.isInternetConnected
import com.example.magnifyingglass.magnifier.ads.loadAndReturnAd
import com.example.magnifyingglass.magnifier.ads.loadAndShowNativeAd
import com.example.magnifyingglass.magnifier.ads.showPriorityAdmobInterstitial
import com.example.magnifyingglass.magnifier.utils.languagesNativeAd

class OnBoardingScreen : BaseActivity() {
    private var fragmentDestination = 0
    private val binding :  OnBoardingScreenBinding by lazy{
        OnBoardingScreenBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)


        initViews()
        handleClicks()

        if(isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this@OnBoardingScreen)?.onBoardingNative?.value == 1){
        loadAndShowNativeAd(binding.layoutNative,
                    R.layout.native_ad_layout_small,
                    getString(R.string.onBoardingNativeId)
                )
        }else{
            binding.layoutNative.visibility = View.GONE
        }

    }


    private fun initViews() {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = adapter
        setupIndicatorDots()
    }

    private fun handleClicks() {
        binding.tvNext.setOnClickListener {
            fragmentDestination++
            binding.viewPager.currentItem = fragmentDestination
        }
        binding.tvDone.setOnClickListener {
            startActivity(Intent(this@OnBoardingScreen, MainScreen::class.java))
            finish()
        }
    }


    private fun setupIndicatorDots() {
        val numPages = binding.viewPager.adapter?.count ?: 0
        val dots = arrayOfNulls<ImageView>(numPages)
        for (i in 0 until numPages) {
            dots[i] = ImageView(this)
            dots[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (i == 0) R.drawable.ic_selected_row else R.drawable.ic_unselected_row
                )
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            binding.indicatorLayout.addView(dots[i], params)
        }

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                for (i in 0 until numPages) {
                    dots[i]?.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@OnBoardingScreen,
                            if (i == position) R.drawable.ic_selected_row else R.drawable.ic_unselected_row
                        )
                    )
                }

                if (position == 2) {
                    binding.tvNext.visibility = View.GONE
                    binding.tvDone.visibility = View.VISIBLE
                } else {
                    binding.tvDone.visibility = View.GONE
                    binding.tvNext.visibility = View.VISIBLE
                }
                fragmentDestination = position
            }

            override fun onPageScrollStateChanged(state: Int) {
                Log.d("TAG42", "onPageScrollStateChanged: ")
            }
        })
    }

}