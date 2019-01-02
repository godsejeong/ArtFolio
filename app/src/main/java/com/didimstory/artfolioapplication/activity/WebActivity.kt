package com.didimstory.artfolioapplication.activity

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import com.didimstory.artfolioapplication.R
import kotlinx.android.synthetic.main.activity_web.*
import android.webkit.WebViewClient
import android.widget.Toast


class WebActivity : AppCompatActivity() {
    var basicurl = "http://artfolio.co.kr"
    var url: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        url = basicurl + intent.getStringExtra("url")

        webView.settings.javaScriptEnabled = true

        webView.loadUrl(url)
        webView.webViewClient = WebViewClient()

        webView.addJavascriptInterface(com.didimstory.artfolioapplication.JavascriptBridge(this), "ARBridge")


    }

    override fun onKeyDown(keyCode : Int,event : KeyEvent) : Boolean{
        if((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack() ){
            webView.goBack()
            return true
        }

        if ((keyCode == KeyEvent.KEYCODE_BACK) && !webView.canGoBack()){
            AlertDialog.Builder(this@WebActivity)
                    .setTitle("어플 종료")
                    .setMessage("어플을 종료하시겠습니까?")
                    .setPositiveButton("예") { dialog, which -> android.os.Process.killProcess(android.os.Process.myPid()) }
                    .setNegativeButton("아니오",  null).show()
        }

        return super.onKeyDown(keyCode, event)
    }
}
