package com.didimstory.artfolioapplication

import android.graphics.Color
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_crop_activitry.*

class CropActivitry : AppCompatActivity() {
    var uri : Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_activitry)
        uri = Uri.parse(intent.getStringExtra("data"))
        Log.e("imagepath", uri.toString())
        ucrop.cropImageView.setImageUri(uri!!,null)
        ucrop.overlayView.setShowCropFrame(false)
        ucrop.overlayView.setShowCropGrid(false)
        ucrop.overlayView.setDimmedColor(Color.TRANSPARENT)
    }
}
