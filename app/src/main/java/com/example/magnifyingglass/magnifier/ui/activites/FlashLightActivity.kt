package com.example.magnifyingglass.magnifier.ui.activites

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Bundle
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.ads.showCollapsibleBanner
import com.example.magnifyingglass.magnifier.databinding.ActivityFlashLightBinding
import com.example.magnifyingglass.magnifier.utils.isInternetConnected

class FlashLightActivity : BaseActivity() {
    companion object {
        private var flashlightOn = false
    }
    private lateinit var binding: ActivityFlashLightBinding
    private lateinit var cameraID: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashLightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (flashlightOn) {
            binding.btnFlash.setImageResource(R.drawable.ic_flash_light_on)
            binding.tvText.text = getString(R.string.tap_to_turn_off)
            flashlightOn = true
        } else {
            binding.btnFlash.setImageResource(R.drawable.ic_flash_light_off)
            binding.tvText.text = getString(R.string.tap_to_turn_on)
            flashlightOn = false
        }
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            cameraID = cameraManager.cameraIdList[0]
        } catch (e: Exception) {
            // on below line we are handling exception.
            e.printStackTrace()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnFlash.setOnClickListener {
            if (!flashlightOn) {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        cameraManager.setTorchMode(cameraID, true)

                        binding.btnFlash.setImageResource(R.drawable.ic_flash_light_on)
                        binding.tvText.text = getString(R.string.tap_to_turn_off)
                        flashlightOn = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        cameraManager.setTorchMode(cameraID, false)

                        binding.btnFlash.setImageResource(R.drawable.ic_flash_light_off)
                        binding.tvText.text = getString(R.string.tap_to_turn_on)
                        flashlightOn = false

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this@FlashLightActivity)?.bannerID?.value == 1) {
            showCollapsibleBanner(binding.adViewContainer, this, getString(R.string.bannerId))
        }
    }
}