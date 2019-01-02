package com.didimstory.artfolioapplication

import android.content.Context
import android.content.Intent
import android.util.Log
import android.webkit.JavascriptInterface
import com.didimstory.artfolioapplication.ar.ARActivity

class JavascriptBridge(context : Context) {

    var context : Context = context

    @JavascriptInterface
    fun loadAR(imagePath : String){
        Log.e("run","AR")
        var intent = Intent(context,ARActivity::class.java)
        intent.putExtra("imagePath",imagePath)
        context.startActivity(intent)
    }
}