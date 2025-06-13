package com.pjimenez.adsnet.appinstituto

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.max

class Registro : AppCompatActivity() {

    private lateinit var name: TextInputEditText
    private lateinit var lastname: TextInputEditText
    private lateinit var cident: TextInputEditText
    private lateinit var email: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var telf: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var image: ImageView
    private var selectedImageUri: Uri? = null

    companion object{
        const val IMAGE_PICK_CODE = 1001
        const val CAMERA_REQUEST_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        name = findViewById(R.id.InputName)
        lastname = findViewById(R.id.inputLastName)
        cident = findViewById(R.id.inputCI)
        telf = findViewById(R.id.inputTelf)
        email = findViewById(R.id.inputEmail)
        image = findViewById(R.id.imagePreview)
        password = findViewById(R.id.inputPass)
        btnRegister = findViewById(R.id.btnNewRegister)

        image.setOnClickListener{
            val options = arrayOf("Seleccionar desde galeria", "Tomar foto")
            AlertDialog.Builder(this)
                .setItems(options){_, which ->
                    when (which){
                        0 -> pickImageFromGallery()
                        1 -> takePhoto()
                    }
                }.show()
        }

        btnRegister.setOnClickListener{
            val passwordText = password.text.toString().trim()
            if (passwordText.isEmpty()) {
                Toast.makeText(this, "La contraseña no puede estar vacía", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = "name=${URLEncoder.encode(name.text.toString().trim(), "UTF-8")}" +
                    "&lastname=${URLEncoder.encode(lastname.text.toString().trim(), "UTF-8")}" +
                    "&cindent=${URLEncoder.encode(cident.text.toString().trim(), "UTF-8")}" +
                    "&telf=${URLEncoder.encode(telf.text.toString().trim(), "UTF-8")}" +
                    "&email=${URLEncoder.encode(email.text.toString().trim(), "UTF-8")}" +
                    "&password=${URLEncoder.encode(passwordText, "UTF-8")}" +
                    "&image=${URLEncoder.encode(imageToBase64() ?: "", "UTF-8")}"

            Log.d("REGISTRO", "Datos a enviar (password length: ${passwordText.length}): $data")

            val nom = name.text.toString().trim()
            val ape = lastname.text.toString().trim()
            val carnet = cident.text.toString().trim()
            val tel = telf.text.toString().trim()
            val correo = email.text.toString().trim()
            val pass = password.text.toString().trim()

            if(pass.isNotEmpty() && nom.isNotEmpty() && ape.isNotEmpty() && carnet.isNotEmpty() && correo.isNotEmpty() && tel.isNotEmpty()){
                try{
                    val ci = carnet.toInt()
                    val tele = tel.toInt()
                    registrarUsuario(nom, ape, ci, tele, correo, pass)
                }catch (e: NumberFormatException){
                    Toast.makeText(this,"CI y telefono deben ser numeros validos", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Debe completar todos los campos requeridos", Toast.LENGTH_LONG).show()
            }

        }
    }
    private fun registrarUsuario(nom: String, ape: String, ci: Int, tele: Int, correo: String, pass: String) {

        val imageBase64 = imageToBase64() ?: ""

        Thread {
            try {
                val url = URL("http://10.0.2.2/app_instituto/registro.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val data = "name=${URLEncoder.encode(nom, "UTF-8")}" +
                        "&lastname=${URLEncoder.encode(ape, "UTF-8")}" +
                        "&cindent=${URLEncoder.encode(ci.toString(), "UTF-8")}" +
                        "&telf=${URLEncoder.encode(tele.toString(), "UTF-8")}" +
                        "&image=${URLEncoder.encode(imageBase64, "UTF-8")}" +
                        "&password=${URLEncoder.encode(pass.toString(), "UTF-8")}"+
                        "&email=${URLEncoder.encode(correo, "UTF-8")}"

                val outputStream = conn.outputStream
                outputStream.write(data.toByteArray())
                outputStream.flush()
                outputStream.close()

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    runOnUiThread {
                        AlertDialog.Builder(this)
                            .setMessage("Desea guardar el Registro?")
                            .setPositiveButton("Aceptar"){ dialog, _ ->
                                Toast.makeText(this, "Registro exitoso: ${response.toString()}", Toast.LENGTH_LONG).show()
                                dialog.dismiss()
                                finish() // Cierra la actividad y vuelve al login
                                logError("exitoso: ${response.toString()}")
                            }
                            .setNegativeButton("Cancelar"){dialog, _ ->
                                dialog.dismiss()
                                Toast.makeText(this, "Registro cancelado", Toast.LENGTH_SHORT).show()
                            }
                            .setCancelable(false)
                            .show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Error en servidor: $responseCode", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    logError("Error: ${e.message}")
                    Toast.makeText(this, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
    fun logError(mensaje: String) {
        Log.e("Error", mensaje)
    }
    private fun imageToBase64(): String? {
        val drawable = image.drawable ?: return null
        val bitmap = (drawable as BitmapDrawable).bitmap

        // Redimensionar si es muy grande
        val maxSize = 800 // píxeles
        val width = bitmap.width
        val height = bitmap.height
        val scale = maxSize.toFloat() / max(width, height)

        if (scale < 1) {
            val matrix = Matrix()
            matrix.postScale(scale, scale)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, (width * scale).toInt(), (height * scale).toInt(), true)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            Log.d("ImageEncoding", "Base64 length: ${base64String.length}")
            return base64String
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun takePhoto(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(packageManager) != null){
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
        }else{
            Toast.makeText(this, "Nose encontro camara", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                IMAGE_PICK_CODE ->{
                    selectedImageUri = data?.data
                    image.setImageURI(selectedImageUri)
                }
                CAMERA_REQUEST_CODE ->{
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    if(imageBitmap != null){
                        image.setImageBitmap(imageBitmap)
                    }
                }
            }
        }
    }
}
