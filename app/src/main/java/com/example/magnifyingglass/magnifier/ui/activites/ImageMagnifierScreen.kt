package com.example.magnifyingglass.magnifier.ui.activites

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.ads.loadAndShowNativeAd
import com.example.magnifyingglass.magnifier.databinding.ImageMagnifierScreenBinding
import com.example.magnifyingglass.magnifier.ui.dialogs.ImagePickerDialog
import com.example.magnifyingglass.magnifier.ui.fragments.CameraXFragment
import com.example.magnifyingglass.magnifier.utils.checkAndRequestCameraPermissions
import com.example.magnifyingglass.magnifier.utils.delayViewClickable
import com.example.magnifyingglass.magnifier.utils.getFileUri
import com.example.magnifyingglass.magnifier.utils.getOutputDirectory
import com.example.magnifyingglass.magnifier.utils.getWindowWidth
import com.example.magnifyingglass.magnifier.utils.isInternetConnected
import com.example.magnifyingglass.magnifier.utils.scanMedia
import com.example.magnifyingglass.magnifier.utils.showToast
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import loadAndShowInterstitialWithCounter
import loadAndShowSplashInterstitial
import showPriorityAdmobInterstitial
import showPriorityInterstitialAdWithCounter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ImageMagnifierScreen : BaseActivity(), View.OnClickListener {
    private lateinit var binding : ImageMagnifierScreenBinding

    private var photoUri: Uri? = null
    private var photoBitmap: Bitmap? = null
    private var alteredBitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ImageMagnifierScreenBinding.inflate(layoutInflater)
        if (remoteConfigViewModel.getRemoteConfig(this@ImageMagnifierScreen)?.InterstitialMain?.value == 1) {
            loadAndShowInterstitialWithCounter(
                true,
                getString(R.string.interstitialId),
                getString(R.string.interstitialId)
            )
        }
        setContentView(binding.root)
        binding.llPlaceHolder.setOnClickListener(this)
        binding.btnCamera.setOnClickListener(this)
        binding.btnGallery.setOnClickListener(this)
        binding.btnEdit.setOnClickListener(this)
        binding.btnDelete.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)

        showNativeAd()

        binding.sbFactorBar.setOnSeekBarChangeListener(onFactorChangeListener)
        binding.sbFactorBar.progress = 30
        binding.sbRadiusBar.setOnSeekBarChangeListener(onRadiusChangeListener)
        binding.sbRadiusBar.progress = 100

        registerCameraLauncher()
        registerGalleryLauncher()
    }

    private var galleryLauncher : ActivityResultLauncher<Intent>?=null
    private fun registerGalleryLauncher() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode== RESULT_OK){
                clearImageView()
                val uri = result.data?.data
                uri?.let {
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)

                        val afterBitmap = resizeImage(bitmap)

                        binding.llPlaceHolder.visibility = View.GONE
                        binding.imageView.visibility = View.VISIBLE
                        alteredBitmap = Bitmap.createBitmap(
                            afterBitmap.width,
                            afterBitmap.height,
                            afterBitmap.config
                        )

                        binding.imageView.setNewImage(alteredBitmap, afterBitmap)

                        showToast("Touch image for zoom")
                        if ( remoteConfigViewModel.getRemoteConfig(this@ImageMagnifierScreen)?.InterstitialMain?.value == 1) {
                            loadAndShowSplashInterstitial(true,getString(R.string.interstitialId),getString(R.string.interstitialId))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun saveImage(b : Bitmap, imageName : String) {
        val imageOutStream: OutputStream?
        val imagesDir = getOutputDirectory()
        val image = File(imagesDir, "$imageName.jpg")
        image.setLastModified(System.currentTimeMillis())
        val uri = getFileUri(image)
        imageOutStream = FileOutputStream(image)
        try {
            b.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)
        } finally {
            imageOutStream.close()
            CoroutineScope(Dispatchers.Main).launch{
                showToast("Image Saved!")
                uri?.path?.let {
                    val f = File(it)
                    scanMedia(f.absolutePath)
                }
            }
        }
    }

    private fun showNativeAd() {
        if(isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this@ImageMagnifierScreen)?.imageMagnifierNativeId?.value == 1){
            loadAndShowNativeAd(binding.adFrame, binding.shimmerFrameLayout.root, R.layout.native_ad_layout_small,getString(R.string.imageMagnifierNativeId))

        }else{
            binding.adFrame.visibility = View.GONE
        }
    }

    private val onFactorChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            val factor = (progress/20)+1
            binding.imageView.setMFactor(factor)
            val factorText = "${progress/20}x"
            binding.magnificientFactorValueText.text = factorText
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }

    private val onRadiusChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            binding.imageView.setRadius(progress)
            binding.loupeRadiusValueText.text = progress.toString()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }

    private fun imagePickerDialog() {
        val imagePickerMagnifier = ImagePickerDialog(
            this@ImageMagnifierScreen,
            object : ImagePickerDialog.ImagePickerClicks {
                override fun openCamera() {
                    cameraOpen()
                }

                override fun openGallery() {
                    galleryOpen()
                }
            }
        )
        imagePickerMagnifier.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        imagePickerMagnifier.show()
    }

    override fun onClick(view: View) {
        if(binding.imageView.isUserInteracting()){
            return
        }
        when (view.id) {
            R.id.btnCamera -> {
                binding.btnCamera.delayViewClickable()
                cameraOpen()
            }
            R.id.btnGallery -> {
                binding.btnGallery.delayViewClickable()
                galleryOpen()
            }
            R.id.llPlaceHolder -> {
                binding.llPlaceHolder.delayViewClickable()
                imagePickerDialog()
            }
            R.id.btnDelete -> {
                clearImageView()
            }
            R.id.btnSave->{
                CoroutineScope(Dispatchers.Default).launch{
                    binding.imageView.getResultBitmap()?.let {
                        withContext(Dispatchers.Main) {
                            enterNameDialog(it)
                        }
                    }?:run{
                        withContext(Dispatchers.Main){
                            showToast("Something went wrong, Please try again!")
                        }
                    }
                }
            }
            R.id.btnEdit -> {
                if(!binding.imageView.isImageSelected()){
                    showToast("Please select an image!")
                    return
                }
                if (binding.imageView.isDraw()) {
                    binding.imageView.isDraw(false)
                    binding.btnEdit.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@ImageMagnifierScreen,
                            R.drawable.ic_edit
                        )
                    )
                    binding.btnSave.visibility = View.VISIBLE
                } else {
                    binding.imageView.isDraw(true)
                    binding.btnEdit.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@ImageMagnifierScreen,
                            R.drawable.ic_done
                        )
                    )
                }
            }
            R.id.btnBack->{
                onBackPressed()
            }
        }
    }

    private fun enterNameDialog(bitmap: Bitmap) {
        val dialog = Dialog(this)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.save_image_dialog_layout)
        dialog.window!!.setLayout(
            getWindowWidth(0.8f),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(
            ResourcesCompat.getDrawable(resources,R.drawable.auto_renewal_background,theme)
        )
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialog.findViewById<Button>(R.id.btnSave)
        val etName = dialog.findViewById<EditText>(R.id.etName)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        btnSave.setOnClickListener {
            val name = etName.text.toString()
            if(isValidName(name)){
                dialog.dismiss()
                CoroutineScope(Dispatchers.Default).launch {
                    saveImage(bitmap, name)
                }
            }else{
                showToast("Please enter a valid name!")
            }
        }
        dialog.setCancelable(false)
        dialog.show()
        etName.requestFocus()
    }

    private fun isValidName(name: String): Boolean {
        var res = true
        if(name.trim().isEmpty() || containsSpecialCharacters(name)){
            res = false
        }
        return res
    }

    private fun containsSpecialCharacters(name: String): Boolean {
        var res = false
        val specialCharactersString = "!@#$%&*()'+,-./:;<=>?[]^_`{|}"
        for (i in name.indices) {
            val ch: Char = name[i]
            if (specialCharactersString.contains(ch.toString())) {
                res = true
                break
            } else if (i == name.length - 1) {
                res = false
            }
        }
        return res
    }

    private fun clearImageView() {
        binding.imageView.clean()
       try{
           binding.imageView.setNewImage(null,null)
       }catch (e:Exception){
       }
        binding.llPlaceHolder.visibility = View.VISIBLE
        binding.btnSave.visibility = View.GONE
        if (binding.imageView.isDraw()) {
            binding.imageView.isDraw(false)
            binding.btnEdit.setImageDrawable(
                ContextCompat.getDrawable(
                    this@ImageMagnifierScreen,
                    R.drawable.ic_edit
                )
            )
        }
    }

    private fun galleryOpen() {
        /*checkAndRequestPermissions { granted->
            if(granted){
        */        val intentGallery = Intent()
                intentGallery.type = "image/*"
                intentGallery.action = Intent.ACTION_GET_CONTENT
                galleryLauncher?.launch(intentGallery)
          //}
       //}
    }

    private fun createImageFile(): File {
        return CameraXFragment.createFile(
            getOutputDirectory(),
            getString(R.string.app_name) + " ${System.currentTimeMillis()}"
        )
    }

    private var takePictureLauncher:ActivityResultLauncher<Uri>?=null
    private fun registerCameraLauncher(){
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()){ success->
            if(success){
                photoUri?.let {
                    try{
                        clearImageView()
                        photoBitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)

                        val afterBitmap = resizeImage(photoBitmap!!)

                        binding.llPlaceHolder.visibility = View.GONE
                        binding.imageView.visibility = View.VISIBLE

                        alteredBitmap = Bitmap.createBitmap(
                            afterBitmap.width,
                            afterBitmap.height,
                            afterBitmap.config
                        )

                        binding.imageView.setNewImage(alteredBitmap, afterBitmap)

                        showToast("Touch image for zoom")
                        if ( remoteConfigViewModel.getRemoteConfig(this@ImageMagnifierScreen)?.InterstitialMain?.value == 1) {
                            loadAndShowSplashInterstitial(true,getString(R.string.interstitialId),getString(R.string.interstitialId))


                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    override fun onBackPressed() {
        //val manager = FakeReviewManager(this@VoiceTranslationActivity)
        requestReview()
    }

    private fun requestReview() {
        try {

            val manager = ReviewManagerFactory.create(this@ImageMagnifierScreen)
            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    manager.launchReviewFlow(this@ImageMagnifierScreen, reviewInfo)
                    // The review dialog might show depending on quota
                    // The API doesn't provide a way for us to check whether it
                    // was actually shown
                    finish()
                } else {
                    @ReviewErrorCode val reviewErrorCode =
                        (task.getException() as ReviewException).errorCode
                    // We got an error log or handle it,
                    // This error means we won't be able to show the dialog
                    finish()
                }
            }
        }catch (e:Exception){
            Log.e("TAG", "onBackPressed: ${e.message}" )

        }
    }

    private fun cameraOpen() {
        checkAndRequestCameraPermissions { granted->
            if(granted){
                val photoFile: File? = try {
                    createImageFile()
                } catch (e: Exception) {
                    null
                }
                photoFile?.let {
                    photoUri = getFileUri(it)
                    photoUri?.let { uri->
                        takePictureLauncher?.launch(uri)
                    }
                }
            }
        }
    }

    private fun resizeImage(image: Bitmap): Bitmap {
        val width = image.width
        val height = image.height

        val scaleWidth = width * 0.9
        val scaleHeight = height * 0.9

        if (image.byteCount <= 1000000)
            return image

        return Bitmap.createScaledBitmap(image, scaleWidth.toInt(), scaleHeight.toInt(), false)
    }



}
