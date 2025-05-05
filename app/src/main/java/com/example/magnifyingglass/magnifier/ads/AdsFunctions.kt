import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.magnifyingglass.magnifier.ads.InterstitialAdClass
import com.example.magnifyingglass.magnifier.utils.isInternetConnected
import com.google.android.gms.ads.interstitial.InterstitialAd

var interstitialAdPriority: InterstitialAd? = null
var timeWhenPreviousIntrShowed: Long? = null
var isFirst20SecondAdControlled = false
var isInterstitialAdOnScreen = false
var interstitialAdCounter = 0
var canShowAppOpen = false
var isFirstTimeAd = true
var isFirstClick = true


fun timeAndCounterCheck(): Boolean {
    return if (isFirstClick) {
        isFirstClick = false
        false
    } else {
        if (isFirstTimeAd) {
            isFirstTimeAd = false
            timeWhenPreviousIntrShowed = System.currentTimeMillis()
            true
        } else {
            if (!isFirst20SecondAdControlled) {
                val currentTime = System.currentTimeMillis()
                val timeDifference = timeWhenPreviousIntrShowed?.let { currentTime.minus(it) }
                Log.d("TAG", "isTimeDifferenceGreaterThan20Seconds: $timeDifference")
                if (timeDifference != null) {
                    isFirst20SecondAdControlled = timeDifference > 20000
                    timeDifference > 20000
                } else {
                    return true
                }
            } else {
                return counterCheck()
            }
        }
    }
}

fun counterCheck(): Boolean {
    interstitialAdCounter++
    return if (interstitialAdCounter >8) true else interstitialAdCounter % 2 == 0
}

fun Activity.loadAndShowSplashInterstitial(
    loadAgain: Boolean= true,
    adIDLow: String,
    mainAdId:String

) {
    if (isInternetConnected()) {
        InterstitialAdClass.getInstance()
            .loadAndShowSplashInterstitial(this, adIDLow, loadAgain, mainAdId, this)
    }
}

fun Activity.loadAndShowInterstitialWithCounter(
    loadAgain: Boolean= true,
    adIDLow: String,
    mainAdId:String

) {
    if (isInternetConnected()) {
        if (counterCheck()) {
            InterstitialAdClass.getInstance()
                .loadAndShowSplashInterstitial(this, adIDLow, loadAgain, mainAdId, this)
        }
    }
}

fun Activity.showPriorityInterstitialAdWithTimeAndCounter(
    loadAgain: Boolean = false,
    adIDHigh: String? = null,
    adIDLow: String? = null,
    loadAd: ((InterstitialAd) -> Unit)? = null,
    closeListener: (() -> Unit)? = null,
    failListener: (() -> Unit)? = null,
    showListener: (() -> Unit)? = null
) {
    if (timeAndCounterCheck()) {
        showPriorityAdmobInterstitial(
            loadAgain,
            adIDLow,
            loadAd,
            closeListener,
            failListener,
            showListener,
        )
    }
}


fun Activity.showPriorityInterstitialAdWithCounter(
    loadAgain: Boolean = false,
    adIDLow: String? = null,
    loadAd: ((InterstitialAd) -> Unit)? = null,
    closeListener: (() -> Unit)? = null,
    failListener: (() -> Unit)? = null,
    showListener: (() -> Unit)? = null
) {
    if (counterCheck()) {
        showPriorityAdmobInterstitial(
            loadAgain,
            adIDLow,
            loadAd,
            closeListener,
            failListener,
            showListener,
        )
    }
}



fun Activity.showPriorityAdmobInterstitial(
    loadAgain: Boolean = false,
    adIDLow: String? = null,
    loadAd: ((InterstitialAd) -> Unit)? = null,
    closeListener: (() -> Unit)? = null,
    failListener: (() -> Unit)? = null,
    showListener: (() -> Unit)? = null
) {
    InterstitialAdClass.getInstance().showPriorityInterstitialAdNew(
        this,
        loadAgain,
        adIDLow,
        loadAd,
        closeListener,
        failListener,
        showListener
    )
}


fun Context.loadPriorityAdmobInterstitial(adIDLow: String) {
    InterstitialAdClass.getInstance().loadPriorityInterstitialAds(this, adIDLow)
}