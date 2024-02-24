package com.example.magnifyingglass.magnifier.ui.activites

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.ads.showPriorityAdmobInterstitial
import com.example.magnifyingglass.magnifier.ui.fragments.CameraXFragment
import com.example.magnifyingglass.magnifier.ads.showPriorityInterstitialAdWithCounter
import com.example.magnifyingglass.magnifier.databinding.LiveMagnifierScreenBinding
import com.example.magnifyingglass.magnifier.utils.isInternetConnected

class LiveMagnifierScreen : BaseActivity() {

    val binding: LiveMagnifierScreenBinding by lazy{
        LiveMagnifierScreenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this@LiveMagnifierScreen)?.InterstitialMain?.value == 1) {
            showPriorityAdmobInterstitial(true,getString(R.string.interstitialId))
        }

        setContentView(binding.root)
        val fragmentmanager = supportFragmentManager
        var fragment = fragmentmanager.findFragmentById(R.id.fragment_container)
        if (fragment == null) {
            fragment = CameraXFragment()
            fragmentmanager.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
