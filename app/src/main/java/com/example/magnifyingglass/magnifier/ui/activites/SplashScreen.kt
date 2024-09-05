package com.example.magnifyingglass.magnifier.ui.activites

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.magnifyingglass.magnifier.Language.LocaleHelper
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.ads.OpenApp
import com.example.magnifyingglass.magnifier.ads.loadAndShowNativeAd
import com.example.magnifyingglass.magnifier.databinding.ActivitySplashBinding
import com.example.magnifyingglass.magnifier.utils.MyApp
import com.example.magnifyingglass.magnifier.utils.isInternetConnected
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import loadPriorityAdmobInterstitial
import java.util.concurrent.atomic.AtomicBoolean


class SplashScreen : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding

    private lateinit var consentInformation: ConsentInformation
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)



    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {

        } else {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val selectedLanguage= LocaleHelper.getSelectedLanguage(this)
        LocaleHelper.setLocale(this, selectedLanguage)
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestConsentForm()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED ) {
            }
            else  {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }


        binding.getStartedBtn.setOnClickListener {
            binding.getStartedBtn.isEnabled = false
            if (isFirstLaunch()) {

                startActivity(Intent(this@SplashScreen, OnBoardingScreen::class.java))
                finish()
            } else {

                startActivity(Intent(this@SplashScreen, MainScreen::class.java))
                finish()


            }

        }
    }

    private fun loadAds() {
        if (isInternetConnected()){
            showNativeAd()

            startHandler(8000)

        }
        else{
            binding.progressBar.visibility = View.GONE
            binding.getStartedBtn.visibility = View.VISIBLE
            binding.layoutNative.visibility = View.GONE
        }
    }

    private fun startHandler(delayTime:Long) {
        Handler(Looper.getMainLooper()).postDelayed({
            kotlin.run {
                binding.progressBar.visibility = View.GONE
                binding.getStartedBtn.visibility = View.VISIBLE
            }

        }, delayTime)
    }
    private fun showNativeAd(){
            loadAndShowNativeAd(
                binding.layoutNative,
                R.layout.native_ad_layout_main,
                getString(R.string.splashNativeId)
            )
    }

    private fun initializeRemoteConfig() {
        CoroutineScope(Dispatchers.IO).launch {

            remoteConfigViewModel.getRemoteConfigSplash(this@SplashScreen)
        }.invokeOnCompletion() {

            CoroutineScope(Dispatchers.Main).launch {
                remoteConfigViewModel.remoteConfig.observe(this@SplashScreen, fun(remoteConfig) {
                    if (remoteConfig?.InterstitialMain?.value == 1 || remoteConfigViewModel.getRemoteConfig(this@SplashScreen)?.splashNative?.value == 1) {
                        loadAds()
                        if (remoteConfigViewModel.getRemoteConfig(this@SplashScreen)?.openAppAdID?.value ==1){
                            OpenApp(application as MyApp)
                        }
                    }else{
                        binding.layoutNative.visibility = View.GONE
                        startHandler(5000)
                    }

                })
            }

        }


    }


    private fun requestConsentForm(){
        // Set tag for under age of consent. false means users are not under age
        // of consent.
        val params = ConsentRequestParameters
            .Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this@SplashScreen,
                    ConsentForm.OnConsentFormDismissedListener {
                            loadAndShowError ->
                        // Consent gathering failed.
                        Log.e("TAG", String.format("%s: %s",
                            loadAndShowError?.errorCode,
                            loadAndShowError?.message
                        ))

                        // Consent has been gathered.
                        if (consentInformation.canRequestAds()) {
                            initializeMobileAdsSdk()
                        }
                    }
                )
            },
            {
                    requestConsentError ->
                // Consent gathering failed.
                Log.w("TAG", String.format("%s: %s",
                    requestConsentError.errorCode,
                    requestConsentError.message
                ))
            })

        // Check if you can initialize the Google Mobile Ads SDK in parallel
        // while checking for new consent information. Consent obtained in
        // the previous session can be used to request ads.
        if (consentInformation.canRequestAds()) {
            initializeMobileAdsSdk()
        }

    }
    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        // Initialize the Google Mobile Ads SDK.
        MobileAds.initialize(this)

        initializeRemoteConfig()

    }
    private fun isFirstLaunch(): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isFirstLaunch = sharedPreferences.getBoolean("first_launch", true)
        if (isFirstLaunch) {
            // Update the preference to false for future launches
            sharedPreferences.edit().putBoolean("first_launch", false).apply()
        }
        return isFirstLaunch
    }


}
