package com.example.magnifyingglass.magnifier.ads


import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*

private fun setBannerAdListener(admobView: AdView, myView: FrameLayout, adCallBack: () -> Unit) {
    admobView.adListener = object : AdListener() {
        override fun onAdLoaded() {
            adCallBack.invoke()
            myView.removeAllViews()
            myView.addView(admobView)
        }
    }
}

private fun getBannerAdSize(context: Context): AdSize? {
    val displayMetricsOut = findRelevantOutMetrics(context)
    val widthPixels = displayMetricsOut.widthPixels.toFloat()
    val density = displayMetricsOut.density
    val adWidth = (widthPixels / density).toInt()
    return AdSize.getInlineAdaptiveBannerAdSize( adWidth,50)
}

private fun findRelevantOutMetrics(context: Context): DisplayMetrics {
    return if (androidVersionIs11OrAbove())
        getOutMetricsForApiLevel30orAbove(context)
    else
        getOutMetricsForOtherVersions(context)
}

private fun androidVersionIs11OrAbove(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
}

@RequiresApi(Build.VERSION_CODES.R)
private fun getOutMetricsForApiLevel30orAbove(context: Context): DisplayMetrics {
    val display: Display? = context.display
    val outMetrics = DisplayMetrics()
    display?.getRealMetrics(outMetrics)
    return outMetrics
}

@Suppress("DEPRECATION")
private fun getOutMetricsForOtherVersions(context: Context): DisplayMetrics {
    return context.resources.displayMetrics
}
fun showCollapsibleBanner(
    myView: FrameLayout,
    context: Context,
    bannerId: String,
    adCallBack: (() -> Unit)? = null
) {
    // Determine the ad size before creating the AdView

    var  adSize: AdSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        getCollapsibleAdSize(context) ?: AdSize.BANNER
    } else {
        AdSize.BANNER
    }


    // Create the AdView and set the adUnitId and adSize immediately
    val admobView = AdView(context).apply {
        this.adUnitId = bannerId
        this.setAdSize(adSize)
    }

    val extras = Bundle().apply {
        putString("collapsible", "bottom")
    }

    val adRequest = AdRequest.Builder()
        .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        .build()

    // Add the AdView to the FrameLayout
    myView.addView(admobView)

    // Load the ad
    admobView.loadAd(adRequest)

    // Set the ad listener
    setBannerAdListener(admobView, myView) {
        adCallBack?.invoke()
    }
}

fun showBannerAdmob(
    myView: FrameLayout,
    context: Context,
    bannerId: String,
    adCallBack: (() -> Unit)? = null
) {
    val admobView = AdView(context)
    admobView.adUnitId = bannerId
    getAdSize(context)?.let { admobView.setAdSize(it) }
    val adRequest = AdRequest.Builder().build()
    admobView.loadAd(adRequest)
    setAdListener(admobView, myView) {
        adCallBack?.invoke()
    }
}

private fun setAdListener(admobView: AdView, myView: FrameLayout, adCallBack: () -> Unit) {
    admobView.adListener = object : AdListener() {
        override fun onAdLoaded() {
            adCallBack.invoke()
            myView.removeAllViews()
            myView.addView(admobView)
        }
    }
}

private fun getAdSize(context: Context): AdSize? {
    val displayMetricsOut = findRelevantOutMetrics(context)
    val widthPixels = displayMetricsOut.widthPixels.toFloat()
    val density = displayMetricsOut.density
    val adWidth = (widthPixels / density).toInt()
    return AdSize.getInlineAdaptiveBannerAdSize( adWidth,50)
}


@RequiresApi(Build.VERSION_CODES.R)
private fun getCollapsibleAdSize(context: Context): AdSize? {
    val displayMetricsOut = findRelevantOutMetrics(context)
    val widthPixels = displayMetricsOut.widthPixels.toFloat()
    val density = displayMetricsOut.density
    val adWidth = (widthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
}