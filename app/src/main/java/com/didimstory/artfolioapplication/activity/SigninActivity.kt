package com.didimstory.artfolioapplication.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.didimstory.artfolioapplication.R
import android.text.style.UnderlineSpan
import android.text.SpannableString
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_signin.*


class SigninActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        requestPermission()
        val content = SpannableString("회원이 아니신가요 ?")
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        signinRegister.text = content

        signinLogin.setOnClickListener {
            var intent = Intent(applicationContext,WebActivity::class.java)
            intent.putExtra("url","/user/login")
            startActivity(intent)
            finish()
        }

        signinLogin.setOnLongClickListener {
            var intent = Intent(applicationContext, CameraActivity::class.java)
            startActivity(intent)
            return@setOnLongClickListener true
        }

            signinRegister.setOnClickListener {
            var intent = Intent(applicationContext,WebActivity::class.java)
            intent.putExtra("url","/user/register")
            startActivity(intent)
            finish()
        }
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                    1000)

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1000) {

            Log.e("grantResults", "1 : ${grantResults[0].toString()} 2 : ${grantResults[1].toString()}")

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            }else{
                Toast.makeText(this,"권한을 허용해주세요",Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
