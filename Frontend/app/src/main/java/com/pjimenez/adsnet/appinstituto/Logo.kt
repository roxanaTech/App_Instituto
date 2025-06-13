package com.pjimenez.adsnet.appinstituto

import android.content.Intent
import android.os.Bundle
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.time.Duration

class Logo : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 2000
    private lateinit var logo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logo)

        logo = findViewById(R.id.imgLogo)

        val animation = TranslateAnimation(0f, 0f, 0f, -500f).apply {
            duration = 2000
            fillAfter = true
        }

        logo.startAnimation(animation)

        Timer().schedule(SPLASH_DELAY){
            val intent = Intent(this@Logo, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
}