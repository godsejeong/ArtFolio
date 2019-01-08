package com.didimstory.artfolioapplication.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.KeyEvent
import com.didimstory.artfolioapplication.R
import kotlinx.android.synthetic.main.activity_web.*
import android.webkit.WebViewClient
import android.widget.Toast
import com.didimstory.artfolioapplication.data.ImageData
import com.didimstory.artfolioapplication.model.web.JavascriptBridge
import com.didimstory.artfolioapplication.util.Utils
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import android.provider.MediaStore




class WebActivity : AppCompatActivity() {
    var basicurl = "http://artfolio.co.kr"
    var url: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        url = basicurl + intent.getStringExtra("url")

        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(JavascriptBridge(this@WebActivity), "ARBridge")
        webView.addJavascriptInterface(JavascriptBridge(this@WebActivity), "CameraBridge")
        webView.loadUrl(url)
        webView.webViewClient = WebViewClient()


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("test","request")
        if(resultCode === Activity.RESULT_OK && requestCode == 1){
            //martpart
            Log.e("camera","return")
            var uri = data!!.getParcelableExtra<Parcelable>("uri") as Uri

            var categoryIdx =  RequestBody.create(MediaType.parse("text/plain"),"3")
            var res = Utils.postService.UploadImg(
                   Utils.createMultipartBody(File(getPath(uri)),"file"),
                   categoryIdx
            )

            res.enqueue(object : Callback<ImageData> {
                override fun onFailure(call: Call<ImageData>, t: Throwable) {
                    Log.e("UploadError",t.message)
                }

                override fun onResponse(call: Call<ImageData>, response: Response<ImageData>) {
                    if(response.code() == 200){
                        response.body()?.let {
                            Log.e("upload","complete")
                            Toast.makeText(this@WebActivity,"이미지 업로드가 완료되었습니다.",Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Log.e("responceCode : ", response.code().toString())
                    }
                }
            })
        }
    }

    fun getPath(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null) ?: return null
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val s = cursor.getString(column_index)
        cursor.close()
        return s
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
