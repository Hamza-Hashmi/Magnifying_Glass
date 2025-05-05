package com.example.magnifyingglass.magnifier.ui.activites

import android.os.Bundle
import android.util.Log
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.databinding.LiveMagnifierScreenBinding
import com.example.magnifyingglass.magnifier.ui.fragments.CameraXFragment
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import loadAndShowInterstitialWithCounter
import showPriorityAdmobInterstitial
import showPriorityInterstitialAdWithCounter


class LiveMagnifierScreen : BaseActivity() {

    val binding: LiveMagnifierScreenBinding by lazy{
        LiveMagnifierScreenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (remoteConfigViewModel.getRemoteConfig(this@LiveMagnifierScreen)?.InterstitialMain?.value == 1) {
            loadAndShowInterstitialWithCounter(
                true,
                getString(R.string.interstitialId),
                getString(R.string.interstitialId)
            )
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

    /*override fun onBackPressed() {
        //val manager = FakeReviewManager(this@VoiceTranslationActivity)
      requestReview()
    }

    private fun requestReview() {
        try {

            val manager = ReviewManagerFactory.create(this@LiveMagnifierScreen)
            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    manager.launchReviewFlow(this@LiveMagnifierScreen, reviewInfo)
                    // The review dialog might show depending on quota
                    // The API doesn't provide a way for us to check whether it
                    // was actually shown
                    finish()
                } else {
                    @ReviewErrorCode val reviewErrorCode =
                        (task.getException() as ReviewException).errorCode
                    // We got an error log or handle it,
                    // This error means we won't be able to show the dialog
                    finish()
                }
            }
        }catch (e:Exception){
            Log.e("TAG", "onBackPressed: ${e.message}" )

        }
    }*/
}
