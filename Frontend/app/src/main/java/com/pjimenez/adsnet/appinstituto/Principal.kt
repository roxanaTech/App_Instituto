package com.pjimenez.adsnet.appinstituto

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Principal : AppCompatActivity() {
    private lateinit var nameUser: TextView
    private lateinit var imageUser: ImageView
    private lateinit var btnSalir: Button

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_principal)

        val name = intent.getStringExtra("NAME_USER") ?: ""
        val lastname = intent.getStringExtra("LASTNAME") ?: ""
        val image = intent.getByteArrayExtra("IMAGE")
        Log.d("PrincipalActivity", "Recibiendo imagen: ${image?.size}")

        nameUser = findViewById(R.id.dataUser)
        imageUser = findViewById(R.id.imageUser)

        nameUser.text = "$name, $lastname"
        if(image != null) {
            val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
            if (bitmap != null) {
                imageUser.setImageBitmap(bitmap)
            } else {
                Log.e("PrincipalActivity", "Error al decodificar la imagen Base64")
                imageUser.setImageResource(R.drawable.user_default)
            }
        }else {
            imageUser.setImageResource(R.drawable.user_default)
        }
        btnSalir = findViewById(R.id.btnSalir)

        btnSalir.setOnClickListener {
            cerrarSesion()
        }
    }
    private fun cerrarSesion() {
        val sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }
}


