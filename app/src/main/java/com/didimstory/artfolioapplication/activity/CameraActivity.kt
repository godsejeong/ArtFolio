package com.didimstory.artfolioapplication.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.didimstory.artfolioapplication.R
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Bitmap
import android.app.Activity
import android.net.Uri
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.IOException


class CameraActivity : AppCompatActivity() {
    val REQUEST_CODE = 99
//    val preference = ScanConstants.OPEN_CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        testBtn.setOnClickListener {
//            startScan(ScanConstants.OPEN_CAMERA)
//            val intent = Intent(this@CameraActivity, ScanActivity::class.java)
//            intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE,preference)
//            startActivityForResult(intent, REQUEST_CODE)
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            val uri = data!!.extras!!.getParcelable<Uri>(ScanConstants.SCANNED_RESULT)
//            var bitmap: Bitmap? = null
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
//                contentResolver.delete(uri!!, null, null)
//                scannedImageView.setImageBitmap(bitmap)
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//
//        }
//    }
}
