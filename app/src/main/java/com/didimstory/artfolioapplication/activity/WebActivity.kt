package com.didimstory.artfolioapplication.activity

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.didimstory.artfolioapplication.R
import kotlinx.android.synthetic.main.activity_web.*
import android.webkit.WebViewClient



class WebActivity : AppCompatActivity() {
    var url : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        url = intent.getStringExtra("url")

        //자스 설정
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)
        webView.webViewClient = WebViewClient()


    }
}
