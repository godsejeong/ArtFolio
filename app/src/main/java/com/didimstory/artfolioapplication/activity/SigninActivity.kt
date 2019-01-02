package com.didimstory.artfolioapplication.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.didimstory.artfolioapplication.R
import android.text.style.UnderlineSpan
import android.text.SpannableString
import kotlinx.android.synthetic.main.activity_signin.*


class SigninActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        val content = SpannableString("회원이 아니신가요 ?")
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        signinRegister.text = content

        signinLogin.setOnClickListener {
            var intent = Intent(applicationContext,WebActivity::class.java)
            intent.putExtra("url","/user/login")
            startActivity(intent)
            finish()
        }

        signinRegister.setOnClickListener {
            var intent = Intent(applicationContext,WebActivity::class.java)
            intent.putExtra("url","/user/register")
            startActivity(intent)
            finish()
        }
    }
}
