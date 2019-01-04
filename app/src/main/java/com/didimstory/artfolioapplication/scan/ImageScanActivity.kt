package com.didimstory.artfolioapplication.scan

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.didimstory.artfolioapplication.R
import com.scanlibrary.*
import kotlinx.android.synthetic.main.activity_image_scan.*
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap

class ImageScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_scan)



    }
}
