package com.example.magnifyingglass.magnifier.ui.activites

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.ads.loadAndShowNativeAd
import com.example.magnifyingglass.magnifier.databinding.ActivityPdfMagnifierBinding
import com.example.magnifyingglass.magnifier.utils.isInternetConnected
import java.io.File

class PdfMagnifierActivity : BaseActivity() {
    private lateinit var binding: ActivityPdfMagnifierBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfMagnifierBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initEvents()
    }

    override fun onResume() {
        super.onResume()
        showNativeAd()
    }

    private fun initViews() {
        with(binding) {
            val pdfUri = intent.getStringExtra("pdfUri")

            pdfUri?.let {
                val file = File(Uri.parse(it).path.toString())
                binding.titleText.text = file.name

                pdfView.fromUri(Uri.parse(pdfUri))
                    .enableDoubletap(false)
                    .load()
            } ?: run {
                Toast.makeText(
                    this@PdfMagnifierActivity,
                    "Sorry! Unable to open file",
                    Toast.LENGTH_SHORT
                ).show()
            }

            zoomSeekBar.progress = pdfView.minZoom.toInt()
            zoomSeekBar.max = pdfView.maxZoom.toInt()
        }
    }

    private fun initEvents() {
        with(binding) {
            btnBack.setOnClickListener {
                finish()
            }

            zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    pdfView.zoomTo(progress.toFloat())
                    pdfView.invalidate()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }

            })
        }
    }

    private fun showNativeAd() {
        if(isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this@PdfMagnifierActivity)?.imageMagnifierNativeId?.value == 1){
            loadAndShowNativeAd(binding.adFrame, binding.shimmerFrameLayout.root, R.layout.native_ad_layout_small,getString(
                R.string.imageMagnifierNativeId))

        }else{
            binding.adFrame.visibility = View.GONE
        }
    }
}