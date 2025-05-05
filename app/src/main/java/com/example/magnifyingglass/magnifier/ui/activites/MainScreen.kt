package com.example.magnifyingglass.magnifier.ui.activites

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.magnifyingglass.magnifier.Language.LanguageActivity
import com.example.magnifyingglass.magnifier.Language.LocaleHelper
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.ads.loadAndReturnAd
import com.example.magnifyingglass.magnifier.ads.loadAndShowNativeAd
import com.example.magnifyingglass.magnifier.ads.showLoadedNativeAd
import com.example.magnifyingglass.magnifier.databinding.ExitDialogBinding
import com.example.magnifyingglass.magnifier.databinding.MainScreenBinding
import com.example.magnifyingglass.magnifier.utils.checkAndRequestCameraPermissions
import com.example.magnifyingglass.magnifier.utils.checkAndRequestPermissions
import com.example.magnifyingglass.magnifier.utils.exitNativeAd
import com.example.magnifyingglass.magnifier.utils.isInternetConnected
import com.example.magnifyingglass.magnifier.utils.openActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import loadAndShowSplashInterstitial

class MainScreen : BaseActivity() {
    private lateinit var binding: MainScreenBinding
    private var isClicked: Boolean = true
    private lateinit var dialog: BottomSheetDialog
    private lateinit var bottomSheetBinding: ExitDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val selectedLanguage = LocaleHelper.getSelectedLanguage(this)
        LocaleHelper.setLocale(this, selectedLanguage)

        if (remoteConfigViewModel.getRemoteConfig(this@MainScreen)?.InterstitialMain?.value == 1) {
            loadAndShowSplashInterstitial(
                true,
                getString(R.string.interstitialId),
                getString(R.string.interstitialId)
            )
        }

        super.onCreate(savedInstanceState)
        binding = MainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        showNative()
        if (remoteConfigViewModel.getRemoteConfig(this@MainScreen)?.exitNative?.value == 1) {
            loadAndReturnAd(this@MainScreen, getString(R.string.exitNativeId)) {
                exitNativeAd = it
            }
        }

        bottomSheetBinding = ExitDialogBinding.inflate(layoutInflater, binding.root, false)
        dialog = BottomSheetDialog(this)
        dialog.setContentView(bottomSheetBinding.root)
        clickListeners()
    }

    override fun onResume() {
        super.onResume()
        showNative()
    }

    private fun showNative() {
        if (isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this@MainScreen)?.mainNative?.value == 1) {
            binding.adFrame.visibility = View.GONE
            binding.shimmerFrameLayout.root.visibility = View.VISIBLE
            loadAndShowNativeAd(
                binding.adFrame,
                binding.shimmerFrameLayout.root,
                R.layout.native_ad_layout_main,
                getString(R.string.mainNativeId)
            )
        } else {
            binding.adFrame.visibility = View.GONE
        }
    }

    private fun clickListeners() {
        binding.apply {
            btnLiveMagnifier.setOnClickListener {
                if (isClicked) {
                    delayClickable()
                    checkAndRequestCameraPermissions {
                        if (it) {
                            openActivity(LiveMagnifierScreen::class.java)
                        }
                    }
                }
            }

            btnImageMagnifier.setOnClickListener {
                checkAndRequestPermissions {
                    openActivity(ImageMagnifierScreen::class.java)
                }
            }

            btnPdf.setOnClickListener {
                openPdfFile()
            }

            btnGallery.setOnClickListener {
                openActivity(SavedImagesScreen::class.java)
            }

            btnFlash.setOnClickListener {
                openActivity(FlashLightActivity::class.java)
            }

//            btnPro.setOnClickListener {
//                openActivity(LanguageActivity::class.java)
//            }

            btnSetting.setOnClickListener {
                openActivity(SettingsActivity::class.java)
            }
        }
    }

    private fun openPdfFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }

        pdfPicker.launch(intent)
    }

    private val pdfPicker = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null) {
                val pdfUri = data.data
                val intent = Intent(this, PdfMagnifierActivity::class.java)
                intent.putExtra("pdfUri", pdfUri.toString())
                startActivity(intent)
            }
        } else {
            Toast.makeText(this@MainScreen, "No PDF selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun delayClickable() {
        Looper.getMainLooper()?.let {
            isClicked = false
            Handler(it).postDelayed({
                isClicked = true
            }, 1000)
        }
    }


    override fun onBackPressed() {
        dialog.show()
        if (isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this@MainScreen)?.exitNative?.value == 1) {
            bottomSheetBinding.layoutNative.visibility = View.VISIBLE

            if (exitNativeAd != null) {
                showLoadedNativeAd(
                    this@MainScreen,
                    bottomSheetBinding.layoutNative,
                    bottomSheetBinding.shimmerFrameLayout.root,
                    R.layout.native_ad_layout_main,
                    exitNativeAd!!
                )

            } else {
                bottomSheetBinding.layoutNative.visibility = View.GONE
            }


        } else {
            bottomSheetBinding.layoutNative.visibility = View.GONE
        }
        bottomSheetBinding.cancel.setOnClickListener {
            dialog.dismiss()
        }

        bottomSheetBinding.exit.setOnClickListener {
            dialog.dismiss()
            super.onBackPressed()
            finishAffinity()
        }
    }
}
