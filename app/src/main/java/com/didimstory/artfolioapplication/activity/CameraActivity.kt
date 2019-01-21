package com.didimstory.artfolioapplication.activity

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.didimstory.artfolioapplication.R
import android.content.Intent
import android.graphics.Bitmap
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.SurfaceHolder
import android.view.WindowManager
import com.scanlibrary.*
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.IOException
import java.lang.NullPointerException


class CameraActivity : AppCompatActivity() {
    var flashbl: Boolean = true
    var scanner: IScanner? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //화면 유지
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_camera)
        requestPermission()

        cameraGalleryBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        cameraCloseBtn.setOnClickListener {
            finish()
        }

        cameraPreview.setOnClickListener{
            cameraPreview.autoFocus()
        }

        cameraPhotoButton.setOnClickListener {
            var imagefile = cameraPreview.saveImage()
            var imageurl: Uri? = null

            imageurl = try {
                FileProvider.getUriForFile(this@CameraActivity, "com.scanlibrary.provider", imagefile!!)
            } catch (e: NullPointerException) {
                Uri.parse(imagefile!!.path)
            }

            Log.e("imageurl", imageurl.toString())
            Handler().postDelayed({
                val intent = Intent(this, ScanActivity::class.java)
                intent.putExtra("uri", imageurl)
                startActivityForResult(intent, 1)
            }, 1000)
        }

        cameraFlashBtn.setOnClickListener {
            if (flashbl) {
                flashbl = false
                cameraPreview.Flashon()
                cameraFlashBtn.setBackgroundResource(R.drawable.ic_flash_off_black_24dp)
            } else {
                flashbl = true
                cameraPreview.Flashoff()
                cameraFlashBtn.setBackgroundResource(R.drawable.ic_flash_on_white)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var bitmap: Bitmap? = null
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                val intent = Intent(this, ScanActivity::class.java)
                intent.putExtra("uri", data!!.data)
                Log.e("uripath", data!!.data.toString())
                startActivityForResult(intent, 1)
            }

            if (requestCode == 1) {
                val uri = data!!.extras!!.getParcelable<Uri>(ScanConstants.SCANNED_RESULT)
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                var intent = Intent()
                intent.putExtra("uri", uri)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    protected fun postImagePick(bitmap: Bitmap) {
        val uri = Utils.getUri(this, bitmap)
        bitmap.recycle()
        scanner!!.onBitmapSelect(uri)
    }

    @Throws(IOException::class)
    private fun getBitmap(selectedimg: Uri?): Bitmap {
        val options = BitmapFactory.Options()
        options.inSampleSize = 3
        var fileDescriptor: AssetFileDescriptor? = null
        fileDescriptor = this.contentResolver.openAssetFileDescriptor(selectedimg!!, "r")
        return BitmapFactory.decodeFileDescriptor(
                fileDescriptor!!.fileDescriptor, null, options)
    }

    private fun startCamera() {
        Log.e("Asdfadf", "Asdfasdf")
        val holder = cameraPreview.holder
        holder.addCallback(cameraPreview)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                    1000)

        } else {
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1000) {

            Log.e("grantResults", "1 : ${grantResults[0].toString()} 2 : ${grantResults[1].toString()}")

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
