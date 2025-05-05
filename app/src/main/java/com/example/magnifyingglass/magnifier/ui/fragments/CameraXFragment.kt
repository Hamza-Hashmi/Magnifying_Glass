package com.example.magnifyingglass.magnifier.ui.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.SeekBar
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.magnifyingglass.magnifier.MagnifierCamerax.LumaListenerMagnifier
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.ads.loadAndShowNativeAd
import com.example.magnifyingglass.magnifier.databinding.FragmentCameraXBinding
import com.example.magnifyingglass.magnifier.utils.ANIMATION_FAST_MILLIS
import com.example.magnifyingglass.magnifier.utils.ANIMATION_SLOW_MILLIS
import com.example.magnifyingglass.magnifier.utils.Constants
import com.example.magnifyingglass.magnifier.utils.RemoteConfigViewModel
import com.example.magnifyingglass.magnifier.utils.getOutputDirectory
import com.example.magnifyingglass.magnifier.utils.isInternetConnected
import com.example.magnifyingglass.magnifier.utils.showToast
import org.koin.androidx.viewmodel.ext.android.viewModel
import showPriorityAdmobInterstitial
import java.io.File
import java.nio.ByteBuffer
import java.util.ArrayDeque
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraXFragment : Fragment() {
    val KEY_EVENT_ACTION = "key_event_action"
    val KEY_EVENT_EXTRA = "key_event_extra"

    var cameraControl: CameraControl? = null
    var zoomLevel:Float=0f
    var exposureLevel:Int=0
    var minExposureValue:Int=0
    var maxExposureValue:Int=0

    private lateinit var binding: FragmentCameraXBinding

    val remoteViewModel:RemoteConfigViewModel by viewModel()
    private lateinit var outputDirectory: File
    private lateinit var broadcastManager: LocalBroadcastManager

    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var previewFreezed:Boolean = false
    private lateinit var windowManager: WindowManager

    private var displayManager : DisplayManager?=null

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService


    private var alteredBitmap: Bitmap? = null
    private var photo: Bitmap? = null
    /** Volume down button receiver used to trigger shutter */
    private val volumeDownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)) {
                // When the volume down button is pressed, simulate a shutter button click
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
//                    cameraUiContainerBinding?.cameraCaptureButton?.simulateClick()
                }
            }
        }
    }

    /**
     * We need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraXFragment.displayId) {
                Log.d(TAG, "Rotation changed: ${view.display.rotation}")
                imageCapture?.targetRotation = view.display.rotation
                imageAnalyzer?.targetRotation = view.display.rotation
            }
        } ?: Unit
    }

    override fun onDestroyView() {
        cameraExecutor.shutdown()
        broadcastManager.unregisterReceiver(volumeDownReceiver)
        displayManager?.unregisterDisplayListener(displayListener)
        super.onDestroyView()
    }

    private fun captureImage() {
        // Get a stable reference of the modifiable image capture use case
        imageCapture?.let { imageCapture ->

            // Create output file to hold the image
            val photoFile = createFile(
                outputDirectory,
                getString(R.string.app_name)+" ${System.currentTimeMillis()}"
            )

            photoFile.setLastModified(System.currentTimeMillis())

            // Setup image capture metadata
            val metadata = ImageCapture.Metadata().apply {
                // Mirror image when using the front camera
                isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
            }

            // Create output options object which contains file + metadata
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .setMetadata(metadata)
                .build()

            // Setup image capture listener which is triggered after photo has been taken
            imageCapture.takePicture(outputOptions, cameraExecutor, object :
                ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri ?: Uri.fromFile(photoFile)

                        requireActivity().let {
                            it.runOnUiThread {
                                Log.e("TAG", "onImageSaved: $savedUri")
                                it.showToast("Image saved!")
                                photo = MediaStore.Images.Media.getBitmap(
                                    requireActivity().contentResolver,
                                    savedUri
                                )

                                val afterBitmap = resizeImage(photo!!)
                                if (remoteViewModel.getRemoteConfig(requireActivity())?.InterstitialMain?.value == 1){
                                    requireActivity().showPriorityAdmobInterstitial(true,getString(R.string.interstitialId))
                                    binding.layoutLivePreview.visibility = View.GONE
                                    binding.layoutImageMagnifier.visibility = View.VISIBLE
                                }else{
                                    binding.layoutLivePreview.visibility = View.GONE
                                    binding.layoutImageMagnifier.visibility = View.VISIBLE
                                }

                                alteredBitmap = Bitmap.createBitmap(
                                    afterBitmap.width,
                                    afterBitmap.height,
                                    afterBitmap.config
                                )

                                binding.imageView.setNewImage(alteredBitmap, afterBitmap)

                                binding.sbFactorBar.setOnSeekBarChangeListener(mOnFactorChangeListener)
                                binding.sbFactorBar.progress = 30
                                binding.sbRadiusBar.setOnSeekBarChangeListener(mOnRadiusChangeListener)
                                binding.sbRadiusBar.progress = 100                            }
                        }

                        // Implicit broadcasts will be ignored for devices running API level >= 24
                        // so if you only target API level 24+ you can remove this statement
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                            requireActivity().sendBroadcast(
                                Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
                            )
                        }

                        // If the folder selected is an external media directory, this is
                        // unnecessary but otherwise other apps will not be able to access our
                        // images unless we scan them using [MediaScannerConnection]
                        val mimeType = MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(savedUri.toFile().extension)
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(savedUri.toFile().absolutePath),
                            arrayOf(mimeType)
                        ) { _, uri ->
                            Log.d(TAG, "Image capture scanned into media store: $uri")
                        }
                    }
                })

            // We can only change the foreground Drawable using API level 23+ API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Display flash animation to indicate that photo was captured
                binding.root.postDelayed({
                    binding.root.foreground = ColorDrawable(Color.WHITE)
                    binding.root.postDelayed({ binding.root.foreground = null },
                        ANIMATION_FAST_MILLIS
                    )
                }, ANIMATION_SLOW_MILLIS)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraXBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayManager =  requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        broadcastManager = LocalBroadcastManager.getInstance(view.context)

        // Set up the intent filter that will receive events from our main activity
        val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }
        broadcastManager.registerReceiver(volumeDownReceiver, filter)

        // Every time the orientation of device changes, update rotation for use cases
        displayManager?.registerDisplayListener(displayListener, null)

        //Initialize WindowManager to retrieve display metrics
        windowManager = (requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager)

        // Determine the output directory
        outputDirectory = requireContext().getOutputDirectory()

        // Wait for the views to be properly laid out
        binding.viewFinder.post {
            // Keep track of the display in which this view is attached
            try{
                displayId = binding.viewFinder.display.displayId
            }catch (e:Exception){
                e.printStackTrace()
            }

            // Build UI controls
            updateCameraUi()

            // Set up the camera and its use cases
            setUpCamera()
        }

        showNativeAd()

        binding.btnBack1.setOnClickListener{
            binding.apply {
                layoutImageMagnifier.visibility = View.GONE
                layoutLivePreview.visibility = View.VISIBLE
            }
        }
    }

    private fun showNativeAd() {
        activity?.let {

            if (it.isInternetConnected() && remoteViewModel.getRemoteConfig(it)?.liveMagnifierNativeId?.value == 1 ){

                it.loadAndShowNativeAd(
                    binding.adFrame,
                    binding.shimmerFrameLayout.root,
                    R.layout.native_ad_layout_small,
                    getString(R.string.liveMagnifierNativeId)
                )
            }else{
                binding.adFrame.visibility = View.GONE
            }
        }
    }

    /**
     * Inflate camera controls and update the UI manually upon config changes to avoid removing
     * and re-adding the view finder from the view hierarchy; this provides a seamless rotation
     * transition on devices that support it.
     *
     * NOTE: The flag is supported starting in Android 8 but there still is a small flash on the
     * screen for devices that run Android 9 or below.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Rebind the camera with the updated display metrics
        bindCameraUseCases()

        // Enable or disable switching between cameras
        updateCameraSwitchButton()
    }

    /** Initialize CameraX, and prepare to bind the camera use cases  */
    private fun setUpCamera() {
        try{
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            cameraProviderFuture.addListener({

                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Select lensFacing depending on the available cameras
                lensFacing = when {
                    hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                    hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                    else -> throw IllegalStateException("Back and front camera are unavailable")
                }

                // Enable or disable switching between cameras
                updateCameraSwitchButton()

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext()))
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases() {

        // Get screen metrics used to setup camera for full screen resolution
        val metrics = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
             windowManager.currentWindowMetrics.bounds
        }else{
            val m = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(m)
            Rect(0, 0, m.widthPixels, m.heightPixels)
        }

        Log.d(TAG, "Screen metrics: ${metrics.width()} x ${metrics.height()}")

        val screenAspectRatio = aspectRatio(metrics.width(), metrics.height())
        Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")

        val rotation = binding.viewFinder.display.rotation

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation
            .setTargetRotation(rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // We request aspect ratio but no resolution to match preview config, but letting
            // CameraX optimize for whatever specific resolution best fits our use cases
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()
            // The analyzer can then be assigned to the instance
            .also {
                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                    // Values returned from our analyzer are passed to the attached listener
                    // We log image analysis results here - you should do something useful
                    // instead!
                    Log.d(TAG, "Average luminosity: $luma")
                })
            }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalyzer
            )
            cameraControl = camera?.cameraControl
            cameraPreviewStarted()

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun cameraPreviewStarted() {
        previewFreezed = false
        binding.freezePreviewBtn.setImageResource(R.drawable.ic_pause)
        cameraControl?.setLinearZoom(zoomLevel)
        cameraControl?.enableTorch(Constants.isTorchOn)
        initExposureValues()
    }

    private fun initExposureValues() {
        val state = camera?.cameraInfo?.exposureState
        val range = state!!.exposureCompensationRange
        val exposureRange = range.toString()
            .replace("[","")
            .replace("]","")
            .replace(" ","")
            .split(",")
        minExposureValue = exposureRange[0].toInt()
        maxExposureValue = exposureRange[1].toInt() - exposureRange[0].toInt()
        binding.brightnessSeekBar.max = maxExposureValue
        binding.brightnessSeekBar.progress = maxExposureValue/2
    }

    private fun updateCameraExposure() {
        cameraControl?.setExposureCompensationIndex(exposureLevel)
    }

    /**
     *  [androidx.camera.core.ImageAnalysis.Builder] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    /** Method used to re-draw the camera UI controls, called every time configuration changes. */
    private fun updateCameraUi() {
        // Listener for button used to capture photo
        binding.takePhotoBtn.setOnClickListener {
            stopCrashing()
            captureImage()
        }

        // Setup for button used to switch cameras
        binding.changeCameraBtn.let {

            // Disable the button until the camera is set up
            it.isEnabled = false

            // Listener for button used to switch cameras. Only called if the button is enabled
            it.setOnClickListener {
                stopCrashing()

                lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }

                // Re-bind use cases to update selected camera
                bindCameraUseCases()
            }
        }

        binding.zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val xValue = progress/10
                binding.seekBarValueText.text = xValue.toString()+"x"
                zoomLevel = progress/100.toFloat()
                cameraControl?.setLinearZoom(zoomLevel)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.brightnessSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                exposureLevel = minExposureValue + progress
//                binding.brightnessValueText.text = exposureLevel.toString()
//                binding.brightnessLabel.text = exposureLevel.toString()
                updateCameraExposure()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.freezePreviewBtn.setOnClickListener {
            stopCrashing()
            if(previewFreezed){
                binding.freezePreviewBtn.setImageResource(R.drawable.ic_pause)
                bindCameraUseCases()
                binding.apply {
                    takePhotoBtn.isEnabled = true
                    changeCameraBtn.isEnabled = true
                    flashLight.isEnabled = true

                }
            }else{
                previewFreezed = true
                binding.freezePreviewBtn.setImageResource(R.drawable.ic_play_n)
                cameraProvider?.unbind(preview)
                binding.apply {
                    takePhotoBtn.isEnabled = false
                    changeCameraBtn.isEnabled = false
                    flashLight.isEnabled = false

                }
            }
        }

        var flash = 0
        binding.flashLight.setOnClickListener {
            when (flash) {
                R.drawable.ic_flash_on -> {
                    flash = R.drawable.ic_flash_off
                    binding.flashLight.setImageResource(flash)
                    turnOffTorch()
                }

                R.drawable.ic_flash_off -> {
                    flash = R.drawable.ic_flash_on
                    binding.flashLight.setImageResource(flash)
                    turnOnTorch()
                }

                else -> {
                    flash = R.drawable.ic_flash_on
                    binding.flashLight.setImageResource(flash)
                    turnOnTorch()
                }
            }
        }
    }

    private fun turnOnTorch() {
        if (camera?.cameraInfo?.hasFlashUnit() == true) {
            camera?.cameraControl?.enableTorch(true)

        }
    }

    private fun turnOffTorch() {
        if (camera?.cameraInfo?.hasFlashUnit() == true) {
            camera?.cameraControl?.enableTorch(false)
        }
    }


    private fun stopCrashing() {
        Looper.getMainLooper()?.let {
            binding.freezePreviewBtn.isClickable = false
            binding.takePhotoBtn.isClickable = false
            binding.changeCameraBtn.isClickable = false
            binding.flashLight.isClickable = false

            Handler(it).postDelayed({
                binding.freezePreviewBtn.isClickable = true
                binding.takePhotoBtn.isClickable = true
                binding.changeCameraBtn.isClickable = true
                binding.flashLight.isClickable = true
            }, 1000)
        }
    }

    /** Enabled or disabled a button to switch cameras depending on the available cameras */
    private fun updateCameraSwitchButton() {
        try {
            binding.changeCameraBtn.isEnabled = hasBackCamera() && hasFrontCamera()
        } catch (exception: CameraInfoUnavailableException) {
            binding.changeCameraBtn.isEnabled = false
        }
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    /**
     * Our custom image analysis class.
     *
     * <p>All we need to do is override the function `analyze` with our desired operations. Here,
     * we compute the average luminosity of the image by looking at the Y plane of the YUV frame.
     */
    private class LuminosityAnalyzer(listener: LumaListenerMagnifier? = null) :
        ImageAnalysis.Analyzer {
        private val frameRateWindow = 8
        private val frameTimestamps = ArrayDeque<Long>(5)
        private val listeners = ArrayList<LumaListenerMagnifier>().apply { listener?.let { add(it) } }
        private var lastAnalyzedTimestamp = 0L
        var framesPerSecond: Double = -1.0
            private set

        /**
         * Used to add listeners that will be called with each luma computed
         */
        fun onFrameAnalyzed(listener: LumaListenerMagnifier) = listeners.add(listener)

        /**
         * Helper extension function used to extract a byte array from an image plane buffer
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        /**
         * Analyzes an image to produce a result.
         *
         * <p>The caller is responsible for ensuring this analysis method can be executed quickly
         * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
         * images will not be acquired and analyzed.
         *
         * <p>The image passed to this method becomes invalid after this method returns. The caller
         * should not store external references to this image, as these references will become
         * invalid.
         *
         * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
         * call image.close() on received images when finished using them. Otherwise, new images
         * may not be received or the camera may stall, depending on back pressure setting.
         *
         */
        override fun analyze(image: ImageProxy) {
            // If there are no listeners attached, we don't need to perform analysis
            if (listeners.isEmpty()) {
                image.close()
                return
            }

            // Keep track of frames analyzed
            val currentTime = System.currentTimeMillis()
            frameTimestamps.push(currentTime)

            // Compute the FPS using a moving average
            while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
            val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
            val timestampLast = frameTimestamps.peekLast() ?: currentTime
            framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
                    frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

            // Analysis could take an arbitrarily long amount of time
            // Since we are running in a different thread, it won't stall other use cases

            lastAnalyzedTimestamp = frameTimestamps.first

            // Since format in ImageAnalysis is YUV, image.planes[0] contains the luminance plane
            val buffer = image.planes[0].buffer

            // Extract image data from callback object
            val data = buffer.toByteArray()

            // Convert the data into an array of pixel values ranging 0-255
            val pixels = data.map { it.toInt() and 0xFF }

            // Compute average luminance for the image
            val luma = pixels.average()

            // Call all listeners with new value
            listeners.forEach { it(luma) }

            image.close()
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        /** Helper function used to create a timestamped file */
        fun createFile(baseFolder: File, name: String) =
            File(
                baseFolder,
                name + PHOTO_EXTENSION
            )
    }

    override fun onResume() {
        super.onResume()
        if(previewFreezed){
            binding.freezePreviewBtn.setImageResource(R.drawable.ic_pause)
            binding.apply {
                takePhotoBtn.isEnabled = true
                changeCameraBtn.isEnabled = true
                flashLight.isEnabled = true
            }
            bindCameraUseCases()
        }
    }

    private val mOnFactorChangeListener: SeekBar.OnSeekBarChangeListener = object :
        SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            val factor = (progress/20)+1
            binding.imageView.setMFactor(factor)
            val factorText = "${progress/20}x"
            binding.magnificientFactorValueText.text = factorText
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }

    private val mOnRadiusChangeListener: SeekBar.OnSeekBarChangeListener = object :
        SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            binding.imageView.setRadius(progress)
            binding.loupeRadiusValueText.text = progress.toString()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }

}