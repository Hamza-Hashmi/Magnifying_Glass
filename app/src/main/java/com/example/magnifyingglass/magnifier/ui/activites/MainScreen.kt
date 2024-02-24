package com.example.magnifyingglass.magnifier.ui.activites

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.example.magnifyingglass.magnifier.Language.LanguageActivity
import com.example.magnifyingglass.magnifier.Language.LocaleHelper
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.ads.loadAndReturnAd
import com.example.magnifyingglass.magnifier.ads.loadAndShowNativeAd
import com.example.magnifyingglass.magnifier.ads.showLoadedNativeAd
import com.example.magnifyingglass.magnifier.ads.showPriorityAdmobInterstitial
import com.example.magnifyingglass.magnifier.databinding.ExitDialogBinding
import com.example.magnifyingglass.magnifier.databinding.MainScreenBinding
import com.example.magnifyingglass.magnifier.ui.dialogs.RattingDialog
import com.example.magnifyingglass.magnifier.utils.checkAndRequestCameraPermissions
import com.example.magnifyingglass.magnifier.utils.checkAndRequestPermissions
import com.example.magnifyingglass.magnifier.utils.exitNativeAd
import com.example.magnifyingglass.magnifier.utils.isInternetConnected
import com.example.magnifyingglass.magnifier.utils.mainNativeAd
import com.example.magnifyingglass.magnifier.utils.openActivity
import com.example.magnifyingglass.magnifier.utils.privacyPolicy
import com.example.magnifyingglass.magnifier.utils.shareWithUs
import com.google.android.material.bottomsheet.BottomSheetDialog


class MainScreen : BaseActivity(){
    private lateinit var binding : MainScreenBinding
    private var isClicked : Boolean = true
    private lateinit var dialog: BottomSheetDialog
    private lateinit var bottomSheetBinding: ExitDialogBinding
    lateinit var rattingDialog: RattingDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        val selectedLanguage= LocaleHelper.getSelectedLanguage(this)
        LocaleHelper.setLocale(this, selectedLanguage)
        if (isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this@MainScreen)?.InterstitialMain?.value == 1) {
            showPriorityAdmobInterstitial(true, getString(R.string.interstitialId))
        }
        super.onCreate(savedInstanceState)
        binding = MainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showNative()
        if (remoteConfigViewModel.getRemoteConfig(this@MainScreen)?.exitNative?.value == 1){
        loadAndReturnAd(this@MainScreen,getString(R.string.exitNativeId)){
            exitNativeAd = it
        }
        }
        rattingDialog = RattingDialog(this@MainScreen)

        bottomSheetBinding = ExitDialogBinding.inflate(layoutInflater, binding.root, false)
        dialog = BottomSheetDialog(this)
        dialog.setContentView(bottomSheetBinding.root)
        clickListeners()
    }

    private fun showNative() {
        if (isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this@MainScreen)?.mainNative?.value == 1){

                loadAndShowNativeAd(
                    binding.layoutNative,
                    R.layout.native_ad_layout_main,
                    getString(R.string.mainNativeId)
                )
        }else{
            binding.layoutNative.visibility = View.GONE
        }
    }

    private fun clickListeners() {
            binding.apply {
                    btnLiveMagnifier.setOnClickListener {
                        if(isClicked){
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

                    btnSavedFiles.setOnClickListener {
                        openActivity(SavedImagesScreen::class.java)
                    }
                btnLanguages.setOnClickListener {
                    openActivity(LanguageActivity::class.java)
                }

                    btnMenu.setOnClickListener {
                        showMenu(it)
                    }


            }
    }




    private fun delayClickable() {
        Looper.getMainLooper()?.let {
            isClicked = false
            Handler(it).postDelayed({
                isClicked = true
            },1000)
        }
    }



    override fun onBackPressed() {

        dialog.show()

        if(isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this@MainScreen)?.exitNative?.value == 1) {
            bottomSheetBinding.layoutNative.visibility = View.VISIBLE

            if (exitNativeAd != null){
                showLoadedNativeAd(
                    this@MainScreen, bottomSheetBinding.layoutNative, R.layout.native_ad_layout_main,
                    exitNativeAd!!)

            }else{
                bottomSheetBinding.layoutNative.visibility = View.GONE
            }


        }else{
            bottomSheetBinding.layoutNative.visibility = View.GONE
        }
        bottomSheetBinding.cancel.setOnClickListener {
            dialog.dismiss()
        }

        bottomSheetBinding.exit.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }


    }

    private fun showMenu(view: View) {
        val popupMenu = PopupMenu(this@MainScreen, view)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId){
                R.id.shareus -> {
                    shareWithUs()
                    true
                }
                R.id.privacy ->{
                    privacyPolicy()
                    true
                }
                R.id.rate ->{
                    rattingDialog.show()
                    true
                }
                else -> false
            }
        }

        popupMenu.inflate(R.menu.popup_menu)

        popupMenu.show()
    }


}
