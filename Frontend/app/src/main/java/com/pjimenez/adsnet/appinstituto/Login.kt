package com.pjimenez.adsnet.appinstituto

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import android.widget.ImageView

class Login : AppCompatActivity() {
    private lateinit var username: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        username = findViewById(R.id.usernameInput)
        password = findViewById(R.id.passwordInput)
        btnLogin = findViewById(R.id.btIngresar)
        btnRegister = findViewById(R.id.btRegistrar)

        btnLogin.setOnClickListener {
            val user = username.text.toString().trim()
            val pass = password.text.toString().trim()
            if (user.isNotEmpty() && pass.isNotEmpty()) {
                loginUsuario(user, pass)
            } else {
                Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, Registro::class.java))
        }
    }

    private fun loginUsuario(username: String, password: String) {
        Thread {
            try {
                val url = URL("http://10.0.2.2/app_instituto/login.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 15000
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                }

                val postData = "username=${URLEncoder.encode(username, "UTF-8")}" +
                        "&password=${URLEncoder.encode(password, "UTF-8")}"

                conn.outputStream.use { outputStream ->
                    outputStream.write(postData.toByteArray())
                }

                when (conn.responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        val response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                        processLoginResponse(response)
                    }
                    else -> {
                        runOnUiThread {
                            Toast.makeText(this, "Error en el servidor", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun processLoginResponse(response: String) {
        try {
            val jsonResponse = JSONObject(response)
            val success = jsonResponse.getBoolean("success")
            val message = jsonResponse.getString("message")

            if (success) {
                val nombre = jsonResponse.optString("nombre", "")
                val apellido = jsonResponse.optString("apellido", "")
                val imagenBase64 = jsonResponse.optString("image", null)
                Log.d("LoginResponse", "Imagen recibida: $imagenBase64")


                runOnUiThread {
                    navigateToMainActivity(nombre, apellido, imagenBase64)
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMainActivity(nombre: String, apellido: String, imagenBase64: String?) {
        try {
            val intent = Intent(this, Principal::class.java).apply {
                putExtra("NAME_USER", nombre)
                putExtra("LASTNAME", apellido)

                // Manejo seguro de la imagen
                if (!imagenBase64.isNullOrEmpty()) {
                    try {
                        Log.d("LoginDebug", "Base64 antes de enviar: ${imagenBase64?.substring(0, 50)}")

                        val imageBytes = Base64.decode(imagenBase64, Base64.DEFAULT)
                        Log.d("Base64.decode:", " ${imageBytes}")
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        Log.d("Bitmap:", " ${bitmap}")

                        if (bitmap != null) {
                            val stream = ByteArrayOutputStream()
                            val byteArray = stream.toByteArray()
                            Log.d("LoginDebug", "Tamaño de la imagen en intent: ${byteArray.size}")

                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                            putExtra("IMAGE", stream.toByteArray())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al cambiar de pantalla", Toast.LENGTH_SHORT).show()
        }
    }
}