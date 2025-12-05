package com.example.appsigbu.ui.login


import androidx.activity.viewModels
import androidx.core.view.isVisible
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appsigbu.ui.login.viewmodel.LoginViewModel
import com.example.appsigbu.R
import com.example.appsigbu.data.model.Usuario
import com.example.appsigbu.ui.inventory.InventoryActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_login)

        // Referencias a UI
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Observar estado de carga
        viewModel.isLoading.observe(this) { loading ->
            progressBar.isVisible = loading
            btnLogin.isEnabled = !loading
        }

        // Observar resultado del login
        viewModel.loginResult.observe(this) { result ->
            result.onSuccess { usuario ->
                reproducirSonidoExito()
                navegarAlDashboard(usuario)
            }.onFailure { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Evento Click
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            viewModel.login(email, pass)
        }
    }
    private fun reproducirSonidoExito() {
        try {
            // Reemplaza 'login_success' con el nombre EXACTO de tu archivo en raw
            val mediaPlayer = MediaPlayer.create(this, R.raw.login_success)
            mediaPlayer.setOnCompletionListener { mp -> mp.release() } // Limpiar memoria al terminar
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace() // Si falla el audio, la app sigue funcionando
        }
    }
    private fun navegarAlDashboard(usuario: Usuario) {
        // Mensaje de éxito
        Toast.makeText(this, "Acceso concedido: ${usuario.rol}", Toast.LENGTH_SHORT).show()

        val intent: Intent

        // --- LÓGICA DE ROLES ---
        if (usuario.rol.equals("Jefe", ignoreCase = true)) {
            // Si es Jefe -> Va al Dashboard Jefe
            intent = Intent(this, DashboardJefeActivity::class.java)
        } else {
            // Si es Trabajador (o cualquier otro) -> Va al Dashboard Trabajador
            intent = Intent(this, DashboardTrabajadorActivity::class.java)
        }

        // Pasamos el nombre para el saludo
        intent.putExtra("USER_NAME", usuario.nombres)

        startActivity(intent)
        finish() // Cerramos login para que no puedan volver atrás
    }
}