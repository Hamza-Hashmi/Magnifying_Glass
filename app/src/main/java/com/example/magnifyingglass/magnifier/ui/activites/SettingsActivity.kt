package com.example.magnifyingglass.magnifier.ui.activites

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.magnifyingglass.magnifier.Language.LanguageActivity
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.ads.loadAndShowNativeAd
import com.example.magnifyingglass.magnifier.databinding.ActivitySettingsBinding
import com.example.magnifyingglass.magnifier.ui.dialogs.RattingDialog
import com.example.magnifyingglass.magnifier.utils.isInternetConnected
import com.example.magnifyingglass.magnifier.utils.openActivity
import com.example.magnifyingglass.magnifier.utils.privacyPolicy
import com.example.magnifyingglass.magnifier.utils.shareWithUs


class SettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVersion.text = "${getString(R.string.app_version)}: ${getVersionName()}"
        initEvents()

        showNativeAd()
    }

    private fun getVersionName(): String {
        var versionName: String = ""
        try {
            versionName = applicationContext.packageManager.getPackageInfo(
                applicationContext.packageName,
                0
            ).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return versionName
    }

    private fun initEvents() {
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }

            btnLang.setOnClickListener {
                openActivity(LanguageActivity::class.java)
            }

            btnRate.setOnClickListener {
                RattingDialog(this@SettingsActivity).show()
            }

            btnShare.setOnClickListener {
                shareWithUs()
            }

            btnPrivacy.setOnClickListener {
                privacyPolicy()
            }
        }
    }

    private fun showNativeAd() {
        if(isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this@SettingsActivity)?.settingsNativeId?.value == 1){
            loadAndShowNativeAd(binding.adFrame, binding.shimmerFrameLayout.root, R.layout.native_ad_layout_main,getString(R.string.imageMagnifierNativeId))

        }else{
            binding.adFrame.visibility = View.GONE
        }
    }
}