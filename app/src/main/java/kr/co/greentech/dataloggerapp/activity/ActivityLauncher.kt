package kr.co.greentech.dataloggerapp.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import kr.co.greentech.dataloggerapp.R

class ActivityLauncher : AppCompatActivity() {

    override fun onCreate(savedInstanceStare: Bundle?) {
        super.onCreate(savedInstanceStare)
        setContentView(R.layout.activity_launcher)
        val handler = Handler()
        handler.postDelayed(Runnable {
            val intent = Intent(applicationContext, ActivityStart::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0);
            finish()
        }, 1000)
    }
}