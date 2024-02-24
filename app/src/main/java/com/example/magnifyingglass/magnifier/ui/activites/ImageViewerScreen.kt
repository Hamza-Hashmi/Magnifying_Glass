package com.example.magnifyingglass.magnifier.ui.activites

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import com.bumptech.glide.Glide
import com.example.magnifyingglass.magnifier.ui.models.ImagesModel
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.ads.loadAndShowNativeAd
import com.example.magnifyingglass.magnifier.ads.showPriorityInterstitialAdWithCounter
import com.example.magnifyingglass.magnifier.databinding.ImageViewerScreenBinding
import com.example.magnifyingglass.magnifier.utils.getFileUri
import com.example.magnifyingglass.magnifier.utils.isInternetConnected
import com.example.magnifyingglass.magnifier.utils.shareImage
import com.example.magnifyingglass.magnifier.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ImageViewerScreen : BaseActivity() {
    lateinit var binding:ImageViewerScreenBinding

    companion object {
        var imagesModelsGlobal: ImagesModel?=null
        var isImageDelete:Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ImageViewerScreenBinding.inflate(layoutInflater)
        if (isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this@ImageViewerScreen)?.InterstitialMain?.value == 1) {

            showPriorityInterstitialAdWithCounter(true, getString(R.string.interstitialId))
        }
        setContentView(binding.root)
        loadSmallNativeAd()

        imagesModelsGlobal?.let {
            initImage(it)
        }
        initListeners()
    }

    private fun loadSmallNativeAd() {
        if(isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this@ImageViewerScreen)?.viewImageNativeId?.value == 1){

            loadAndShowNativeAd(
                binding.layoutNativeLarge,
                R.layout.native_ad_layout_small,
                getString(R.string.viewImageNativeId)

            )
        }
    }

    private fun initImage(m: ImagesModel) {
        binding.titleText.text = m.name
        Glide.with(this)
            .load(m.uri)
            .into(binding.ivImage)
    }

    private fun initListeners() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        binding.btnDeleteImage.setOnClickListener {
            Looper.getMainLooper()?.let {
                binding.btnDeleteImage.isClickable = false
                Handler(it).postDelayed(
                    {
                        binding.btnDeleteImage.isClickable = true
                    }, 1000
                )
            }
            imagesModelsGlobal?.let {
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Delete image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("YES") { _, _ ->
                        deleteImage()
                    }
                    .setNegativeButton("NO",null)
                    .create()
                dialog.show()
                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(
                    R.color.colorPrimary))
                dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(
                    R.color.colorPrimary))
            }
        }
        binding.btnShareImage.setOnClickListener {
            Looper.getMainLooper()?.let {
                binding.btnShareImage.isClickable = false
                Handler(it).postDelayed(
                    {
                        binding.btnShareImage.isClickable = true
                    }, 1000
                )
            }
            imagesModelsGlobal?.uri?.toFile()?.let { f->
                getFileUri(f)?.let { uri->
                    shareImage(uri)
                }
            }
        }
    }

    private fun deleteImage() {
        CoroutineScope(Dispatchers.Main).launch {
            imagesModelsGlobal?.uri?.path?.let {
                val b = File(it)
                val d = b.delete()
                if(d){
                    showToast("image deleted!")
                    isImageDelete = true
                    onBackPressed()
                }else{
                    showToast("Something went wrong!")
                }
            }
        }
    }



}