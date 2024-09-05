package com.example.magnifyingglass.magnifier.ads

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.ui.activites.SplashScreen
import com.example.magnifyingglass.magnifier.utils.MyApp
import com.example.magnifyingglass.magnifier.utils.isInternetConnected
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import isInterstitialAdOnScreen
import org.jetbrains.annotations.NotNull

class OpenApp(private val globalClass: MyApp) : Application.ActivityLifecycleCallbacks,
    LifecycleEventObserver {

    private var adVisible = false
    private val TAG = "TESTINGOpenApp"
    private var appOpenAd: AppOpenAd? = null
    private var currentActivity: Activity? = null
    private var isShowingAd = false
    private var isHighAdFailedToLoad = false
    private var isMediumAdFailedToLoad = false
    private var myApplication: MyApp? = globalClass
    private var fullScreenContentCallback: FullScreenContentCallback? = null

    init {
        this.myApplication?.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun fetchAd(adId: String) {
        if (isAdAvailable()) {
            return
        }
        val loadCallback: AppOpenAd.AppOpenAdLoadCallback =
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    Log.d(TAG, "onAdLoaded: ")
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    if (!isHighAdFailedToLoad) {
                        Log.d(TAG, "onAdFailedToLoad: $p0")
                        isHighAdFailedToLoad = true
                        fetchAd(globalClass.getString(R.string.app_open_id))
                    } else if (!isMediumAdFailedToLoad) {
                        Log.d(TAG, "onAdFailedToLoad: $p0")
                        isMediumAdFailedToLoad = true
                        fetchAd(globalClass.getString(R.string.app_open_id))
                    } else {
                        Log.d(TAG, "onAdFailedToLoad: $p0")
                    }
                }
            }
        val request: AdRequest = getAdRequest()

        myApplication?.applicationContext?.apply {
            AppOpenAd.load(
                this, adId, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
            )
        }
    }

    private fun showAdIfAvailable() {
        Log.d(TAG, "showAdIfAvailable: $isInterstitialAdOnScreen")
        if (!isInterstitialAdOnScreen) {
            appOpenAd?.let {
                if (!isShowingAd && isAdAvailable()) {
                    fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            appOpenAd = null
                            isShowingAd = false
                            adVisible = false
                            dismissLoadingDialog()
                            fetchAd(globalClass.getString(R.string.app_open_id))
                            Log.d(TAG, "onAdDismissedFullScreenContent: ")
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            dismissLoadingDialog()
                            fetchAd(globalClass.getString(R.string.app_open_id))
                            appOpenAd = null
                            Log.d(TAG, "onAdFailedToShowFullScreenContent: $p0")
                        }

                        override fun onAdShowedFullScreenContent() {
                            isHighAdFailedToLoad = false
                            isHighAdFailedToLoad = false
                            isShowingAd = true
                            Log.d(TAG, "onAdShowedFullScreenContent: ")
                        }
                    }

                    adVisible = true
                    appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
                    currentActivity?.let {
                        showLoadingDialog(it)
                    }
                    appOpenAd?.show(currentActivity!!)
                }
            }
        }
    }


    override fun onStateChanged(p0: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_START) {
            Log.d(TAG, "onStateChanged: ")

            currentActivity?.let {
                Log.d(TAG, "onStateChanged: $it")
                if (it !is SplashScreen
                    ) {
                    showAdIfAvailable()
                } else {
                    fetchAd(globalClass.getString(R.string.app_open_id))
                }
            }
        }
    }


    @NotNull
    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityStarted(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = p0
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

    private var loadingDialog: Dialog? = null
    private fun showLoadingDialog(activity: Activity) {
        loadingDialog = Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        loadingDialog?.setContentView(R.layout.ad_loading_dialog)
        loadingDialog?.setCancelable(false)
        if (loadingDialog?.isShowing == false) {
            loadingDialog?.show()
        }
    }

    private fun dismissLoadingDialog() {
        if (loadingDialog?.isShowing == true) {
            try {
                loadingDialog?.dismiss()
            } catch (e: IllegalArgumentException) {
                // Handle exception, e.g., log it
            } finally {
                loadingDialog = null
            }
        }
    }
}