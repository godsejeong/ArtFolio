package com.didimstory.artfolioapplication.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.net.Uri
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.scanlibrary.ScanConstants
import java.io.ByteArrayOutputStream


class CameraView : SurfaceView, SurfaceHolder.Callback {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    var fileUri :Uri? = null
    private var mCamera: Camera? = null

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        setFocus { b, camera -> }
        if (holder?.surface == null)
            return
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mCamera?.run {
            stopPreview()
            release()
        }
        mCamera = null
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        if (mCamera == null)
            mCamera = Camera.open()
        mCamera?.run {
            parameters = mCamera!!.parameters.apply {
                set("orientation", "portrait")
                setRotation(90)
            }
            setDisplayOrientation(90)
            setPreviewDisplay(holder)
            startPreview()

            val params = parameters
            val sizes = params.supportedPreviewSizes
            getOptimumSize().let {
                params.setPictureSize(it.width, it.height)
            }
            params.setPreviewSize(sizes.first().width, sizes.first().height)
            parameters = params
        }
    }

    fun Flashon() {
        val p = mCamera!!.parameters
        p.flashMode = Camera.Parameters.FLASH_MODE_TORCH
        mCamera!!.parameters = p
    }

    fun Flashoff() {
        val p = mCamera!!.parameters
        p.flashMode = Camera.Parameters.FLASH_MODE_OFF
        mCamera!!.parameters = p
    }

    private fun getOptimumSize(): Camera.Size {
        mCamera?.run {
            val params = parameters
            var biggestWidth = params.supportedPreviewSizes.first().width
            var biggestHeight = params.supportedPreviewSizes.first().height

            var i = 2
            while (i < biggestHeight) {
                val hResult = biggestHeight % i
                val wResult = biggestWidth % i
                if (hResult == 0 && wResult == 0) {
                    biggestHeight /= i
                    biggestWidth /= i
                } else
                    i++
            }

            params.supportedPictureSizes.forEach {
                var width = it.width
                var height = it.height

                i = 2
                while (i < height) {
                    val hResult = height % i
                    val wResult = width % i
                    if (hResult == 0 && wResult == 0) {
                        height /= i
                        width /= i
                    } else
                        i++
                }
                if (width == biggestWidth && height == biggestHeight) {
                    return it

                }

            }
        }
        return mCamera!!.parameters.supportedPreviewSizes.first()
    }

    fun setFocus(onFocus: (Boolean, Camera) -> Unit) {
        mCamera?.autoFocus { b, camera ->
            onFocus(b, camera)
        }
    }

    fun getPicture(callback: (img: ByteArray) -> Unit) {
        mCamera?.takePicture(null, null) { biteArray, camera ->
            callback.invoke(biteArray)
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val file = File(ScanConstants.IMAGE_PATH, "IMG_" + timeStamp +
                ".jpg")
        fileUri = Uri.fromFile(file)
        return file
    }

    @SuppressLint("SimpleDateFormat")
    private fun getOutputMediaFileUri(): File? {
        // check for external storage
//        try {
                // Create an image file nameFile imagePath = new File(Context.getFilesDir(), "images");
                val imagePath = "IMG_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val storageDir = Environment.getExternalStorageDirectory().absolutePath + "/ArtFolio/img/"
                val folderPath = File(storageDir)
                if (!folderPath.isDirectory)
                    folderPath.mkdirs()
                val image = File.createTempFile(imagePath, ".jpg", folderPath)
//                var path = image.absolutePath.toInt()
                return image

//        } catch (e: IOException) {
//            e.printStackTrace()
//            Log.e("asdfsadfa","ASdfasdf")
//        }
//        return null
    }

    @SuppressLint("SimpleDateFormat")
    fun saveImage(): File? {
        var uri = getOutputMediaFileUri()
//        val folder = Environment.getExternalStorageDirectory().absolutePath + "/ArtFolio/img/"
//        val filename = "${SimpleDateFormat("yyyyMMddhhmmss").format(Date())}.jpg"

        getPicture { img ->
            val bmp = BitmapFactory.decodeByteArray(img, 0, img.size)
//            if (!folderPath.isDirectory)
//                folderPath.mkdirs()
//            val cacheFile = File(context.cacheDir.toString() + File.separator + uri)
            val out = FileOutputStream(uri)
            val bytes = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.close()
            mCamera?.startPreview()
        }
        Log.e("path2",uri!!.path)
        return uri
    }
}