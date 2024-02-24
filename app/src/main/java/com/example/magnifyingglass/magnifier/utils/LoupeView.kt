package com.example.magnifyingglass.magnifier.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView

class LoupeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AppCompatImageView(context, attrs), View.OnTouchListener {

    private var isBitmapDrawable = false
    private var mFactor = MAGNIFICATION_FACTOR_DEFAULT
    private var loupeRadius: Int
    private val loupePath = Path()
    private var mLupeBorderPaint: Paint? = null
    private val mDrawMatrix = Matrix()
    private val mDrawableBounds = RectF()
    private var mGravity = VERTICAL_BOTTOM or HORIZONTAL_RIGHT
    private var mCenterX = 0
    private var mCenterY = 0
    private var mOffsetX = 0
    private var mOffsetY = 0
    private val mExtraOffsetX: Int
    private val mExtraOffsetY: Int
    private var mIsTouching = false
    private var mIsUserInteracting = false

    private var isDraw = false

    private var downX = 0f
    private var downy = 0f
    private var upx = 0f
    private var upy = 0f
    private var canvas: Canvas? = null
    private var paint: Paint? = null

    init {
        setOnTouchListener(this)
        initPaints()
        loupeRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            LOUPE_RADIUS_DP.toFloat(),
            resources.displayMetrics
        ).toInt()
        mExtraOffsetX = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            EXTRA_OFFSET.toFloat(),
            resources.displayMetrics
        ).toInt()
        mExtraOffsetY = mExtraOffsetX
    }

    fun isDraw(value: Boolean) {
        isDraw = value
    }

    var alteredBitmap: Bitmap?=null
    var bmp: Bitmap? = null
    fun clean() {
        bmp?.let {
            //            Log.e("this_", "${alteredBitmap.height} ${alteredBitmap.width}")
            alteredBitmap?.let {ab->
                canvas = Canvas(ab)
                paint = Paint()
                paint!!.color = Color.GREEN
                paint!!.strokeWidth = 5f
                val matrix = Matrix()
                canvas!!.drawBitmap(it, matrix, paint)
                setImageBitmap(ab)
            }
        }
    }

    fun isDraw(): Boolean {
        return isDraw
    }

    private fun initPaints() {
        mLupeBorderPaint = Paint()
        mLupeBorderPaint!!.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            15f,
            resources.displayMetrics
        )
        mLupeBorderPaint!!.style = Paint.Style.STROKE
        mLupeBorderPaint!!.color = Color.WHITE
        mLupeBorderPaint!!.alpha = 127
    }

    fun setMFactor(factor: Int) {
        mFactor = factor
    }

    fun setRadius(radiusDp: Int) {
        loupeRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            radiusDp.toFloat(),
            resources.displayMetrics
        ).toInt()
        setGravity(mGravity)
    }

    private fun setGravity(gravity: Int) {
        when (gravity and VERTICAL_MASK) {
            VERTICAL_TOP -> mOffsetY = loupeRadius / 2 + mExtraOffsetY
            VERTICAL_CENTER -> mOffsetY = 0
            VERTICAL_BOTTOM -> mOffsetY = -(loupeRadius / 2 + mExtraOffsetY)
        }
        when (gravity and HORIZONTAL_MASK) {
            HORIZONTAL_LEFT -> mOffsetX = loupeRadius / 2 + mExtraOffsetX
            HORIZONTAL_CENTER -> mOffsetX = 0
            HORIZONTAL_RIGHT -> mOffsetX = -(loupeRadius / 2 + mExtraOffsetX)
        }
        mGravity = gravity
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        mIsUserInteracting = !(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)
        if (!isDraw) {
            mIsTouching = !(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)
            mCenterX = event.x.toInt() + mOffsetX
            mCenterY = event.y.toInt() + mOffsetY
            invalidateSelf()
            performClick()
        }
        return true
    }

    private fun getPointerCords(e: MotionEvent): FloatArray {
        val index = e.actionIndex
        val cords = floatArrayOf(e.getX(index), e.getY(index))
        val matrix = Matrix()
        imageMatrix.invert(matrix)
        matrix.postTranslate(scrollX.toFloat(), scrollY.toFloat())
        matrix.mapPoints(cords)
        return cords
    }

    override fun invalidateDrawable(dr: Drawable) {
        setOnTouchListener(this)
        if (dr is BitmapDrawable) {
            isBitmapDrawable = true
            val matrix = imageMatrix
            mDrawableBounds.set(dr.getBounds())
            matrix.mapRect(mDrawableBounds)
        }
        super.invalidateDrawable(dr)
    }

    override fun onDraw(canvas: Canvas) {
        try{
            if(!isImageSelected()){
                return
            }
            super.onDraw(canvas)
            // draw lupe effect
            if (mIsTouching) {
                drawLoupe(canvas)
            }
        }catch (e:Exception){
        }
    }

    private fun drawLoupe(canvas: Canvas) {
        if (!isBitmapDrawable) {
            Log.w(
                "LoupeView",
                "In order to get zoom in your images the src image should be a Bitmap"
            )
            return
        }

        if(mCenterX<mDrawableBounds.left){
            mCenterX = mDrawableBounds.left.toInt()
        }else if(mCenterX>mDrawableBounds.right){
            mCenterX = mDrawableBounds.right.toInt()
        }

        if(mCenterY>mDrawableBounds.bottom){
            mCenterY = mDrawableBounds.bottom.toInt()
        }else if(mCenterY<mDrawableBounds.top){
            mCenterY = (mDrawableBounds.top).toInt()
        }

        canvas.save()
        clipCircle(canvas)
        mDrawMatrix.reset()
        mDrawMatrix.preScale(mFactor.toFloat(), mFactor.toFloat())
        mDrawMatrix.postConcat(imageMatrix)
        val px = mCenterX - mDrawableBounds.left
        val py = mCenterY - mDrawableBounds.top
        mDrawMatrix.postTranslate(-px * (mFactor - 1), -py * (mFactor - 1))

        canvas.drawBitmap((drawable as BitmapDrawable).bitmap, mDrawMatrix, null)

        // draw border
        canvas.drawCircle(
            mCenterX.toFloat(),
            mCenterY.toFloat(),
            loupeRadius.toFloat(),
            mLupeBorderPaint!!
        )
        canvas.restore()
    }

    private fun clipCircle(canvas: Canvas) {
        loupePath.reset()
        loupePath.addCircle(
            mCenterX.toFloat(),
            mCenterY.toFloat(),
            loupeRadius.toFloat(),
            Path.Direction.CW
        )
        canvas.clipPath(loupePath)
    }

    private fun invalidateSelf() {
        drawable?.invalidateSelf()
    }

    fun setNewImage(alteredBitmap: Bitmap?, b: Bitmap?) {
        b?.let {
            setOnTouchListener(this)
            this.alteredBitmap = alteredBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
            this.bmp = it.copy(Bitmap.Config.ARGB_8888, true)
            canvas = Canvas(alteredBitmap)
            paint = Paint()
            paint!!.color = Color.GREEN
            paint!!.strokeWidth = 7f
            val matrix = Matrix()
            canvas?.drawBitmap(it, matrix, paint)
            setImageBitmap(alteredBitmap)
        }?:run{
            setOnTouchListener(null)
            this.bmp = null
            setImageBitmap(null)
        }
        invalidate()
    }

    fun isImageSelected():Boolean{
        return this.bmp!=null
    }

    companion object {
        private const val verticalShift = 0
        private const val horizontalShift = 4
        const val VERTICAL_TOP = 0x0001 shl verticalShift
        const val VERTICAL_CENTER = 0x0002 shl verticalShift
        const val VERTICAL_BOTTOM = 0x0004 shl verticalShift
        private const val VERTICAL_MASK = 0x000F
        const val HORIZONTAL_LEFT = 0x0001 shl horizontalShift
        const val HORIZONTAL_CENTER = 0x0002 shl horizontalShift
        const val HORIZONTAL_RIGHT = 0x0004 shl horizontalShift
        private const val HORIZONTAL_MASK = 0x00F0
        private const val LOUPE_RADIUS_DP = 100
        private const val MAGNIFICATION_FACTOR_DEFAULT = 2
        private const val EXTRA_OFFSET = 15
    }

    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
        val action = event!!.action
        mIsUserInteracting = !(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)
        if (isDraw)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = getPointerCords(event)[0] //event.getX();
                    downy = getPointerCords(event)[1] //event.getY();
                }
                MotionEvent.ACTION_MOVE -> {
                    upx = getPointerCords(event)[0] //event.getX();
                    upy = getPointerCords(event)[1] //event.getY();
                    canvas?.drawLine(downX, downy, upx, upy, paint!!)
                    invalidate()
                    downX = upx
                    downy = upy
                }
                MotionEvent.ACTION_UP -> {
                    upx = getPointerCords(event)[0] //event.getX();
                    upy = getPointerCords(event)[1] //event.getY();
                    canvas?.drawLine(downX, downy, upx, upy, paint!!)
                    invalidate()
                }
                MotionEvent.ACTION_CANCEL -> {
                }
                else -> {
                }
            }
        else {
            mIsTouching = !(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)
            mCenterX = event.x.toInt() + mOffsetX
            mCenterY = event.y.toInt() + mOffsetY
            invalidateSelf()
            performClick()
        }
        return true
    }

    fun getResultBitmap(): Bitmap? {
        return getViewScreenshot(this)
    }

    private fun getViewScreenshot(view: View): Bitmap? {
        return try{
            val returnedBitmap = Bitmap.createBitmap(
                view.width,
                view.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(returnedBitmap)
            val bgDrawable = view.background
            if (bgDrawable != null) {
                bgDrawable.draw(canvas)
            } else {
                canvas.drawColor(Color.WHITE)
            }
            view.draw(canvas)
            getCroppedBitmap(returnedBitmap)
        }catch (e:Exception){
            e.message?.let {
                context.showToast(it)
            }
            null
        }
    }

    private fun getCroppedBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = getImageBounds(bitmap.width,bitmap.height)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRect(getImageBounds(bitmap.width,bitmap.height),paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return getScaledBitmap(output)
    }

    private fun getImageBounds(w:Int,h:Int): Rect {
        val centerPoint = Point(w/2,h/2)
        val width = canvas!!.width
        return Rect(
            centerPoint.x-width,
            centerPoint.y-(h/2),
            centerPoint.x+width,
            centerPoint.y+(h/2)
        )
    }

    private fun getScaledBitmap(b: Bitmap): Bitmap {
        val matrix = Matrix()

        matrix.postRotate(rotation)

        val scaledBitmap = Bitmap.createScaledBitmap(b, b.width, b.height, true)

        return Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )
    }

    fun isUserInteracting(): Boolean {
        return mIsUserInteracting
    }
}
