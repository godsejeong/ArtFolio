package com.didimstory.artfolioapplication

import android.content.Context
import android.content.Intent
import com.didimstory.artfolioapplication.ar.ARActivity

class JavascriptBridge(context : Context) {

    var context : Context = context

    fun loadAR(imagePath : String){
        var intent = Intent(context,ARActivity::class.java)
        intent.putExtra("imagePath",imagePath)
        context.startActivity(intent)
    }
}