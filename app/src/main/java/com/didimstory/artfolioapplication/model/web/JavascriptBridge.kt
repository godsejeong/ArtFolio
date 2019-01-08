package com.didimstory.artfolioapplication.model.web

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.didimstory.artfolioapplication.activity.CameraActivity
import com.didimstory.artfolioapplication.activity.WebActivity
import com.didimstory.artfolioapplication.activity.ar.ARActivity
import com.didimstory.artfolioapplication.data.ImageData
import com.didimstory.artfolioapplication.util.Utils
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.net.URL


class JavascriptBridge(context : Activity) : AppCompatActivity() {

    var context : Activity = context
    val boundary = "*****mgd*****"

    @JavascriptInterface
    fun loadAR(imagePath : String){
        Log.e("run","AR")
        var intent = Intent(context,ARActivity::class.java)
        intent.putExtra("imagePath",imagePath)
        context.startActivity(intent)
    }

    @JavascriptInterface
    fun loadCamera(){
        Log.e("run","Camera")
        var intent = Intent(context,CameraActivity::class.java)
        context.startActivityForResult(intent, 1)
    }
}