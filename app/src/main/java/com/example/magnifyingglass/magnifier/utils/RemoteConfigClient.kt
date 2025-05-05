package com.example.magnifyingglass.magnifier.utils


import android.content.Context
import androidx.annotation.Keep
import com.example.magnifyingglass.magnifier.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class RemoteConfigClient {
    private lateinit var remoteConfig: FirebaseRemoteConfig

    fun init(context: Context): FirebaseRemoteConfig {
        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSetting = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(10)
            .build()
        remoteConfig.setConfigSettingsAsync(configSetting)
        remoteConfig.setDefaultsAsync(
            mapOf(context.getString(R.string.remoteTopic) to Gson().toJson(RemoteConfig()))
        )
        return remoteConfig
    }
}

@Keep
data class RemoteConfig(
    @SerializedName("openAppAdID")
    val openAppAdID: RemoteDefaultVal = RemoteDefaultVal(1),

    @SerializedName("bannerID")
    val bannerID: RemoteDefaultVal = RemoteDefaultVal(1),

    @SerializedName("splashNative")
    val splashNative: RemoteDefaultVal = RemoteDefaultVal(1),

    @SerializedName("InterstitialMain")
    val InterstitialMain: RemoteDefaultVal = RemoteDefaultVal(1),

    @SerializedName("InterstitialSplash")
    val InterstitialSplash: RemoteDefaultVal = RemoteDefaultVal(1),

    @SerializedName("mainNative")
    val mainNative: RemoteDefaultVal = RemoteDefaultVal(1),

    @SerializedName("liveMagnifierNativeId")
    val liveMagnifierNativeId: RemoteDefaultVal = RemoteDefaultVal(1),

    @SerializedName("imageMagnifierNativeId")
    val imageMagnifierNativeId: RemoteDefaultVal = RemoteDefaultVal(1),

    @SerializedName("settingsNativeId")
    val settingsNativeId: RemoteDefaultVal = RemoteDefaultVal(1),

    @SerializedName("pdfViewerNativeId")
    val pdfViewerNativeId: RemoteDefaultVal = RemoteDefaultVal(1),

    @SerializedName("savedImagesNativeId")
    val savedImagesNativeId: RemoteDefaultVal = RemoteDefaultVal(1),

    @SerializedName("viewImageNativeId")
    val viewImageNativeId: RemoteDefaultVal = RemoteDefaultVal(1),

    @SerializedName("onBoardingNative")
    val onBoardingNative: RemoteDefaultVal = RemoteDefaultVal(1),

    @SerializedName("exitNative")
    val exitNative: RemoteDefaultVal = RemoteDefaultVal(1),

    @SerializedName("languagesNative")
    val languagesNative: RemoteDefaultVal = RemoteDefaultVal(1)
)

@Keep
data class RemoteDefaultVal(
    @SerializedName("value")
    val value: Int = 1,
)