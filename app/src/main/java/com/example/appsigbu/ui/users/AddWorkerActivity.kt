package com.example.appsigbu.ui.users

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appsigbu.R
import com.example.appsigbu.data.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore

class AddWorkerActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var idEditar: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_worker)

        // Referencias
        val etDni = findViewById<EditText>(R.id.etDni)
        val etNombres = findViewById<EditText>(R.id.etNombres)
        val etPaterno = findViewById<EditText>(R.id.etPaterno)
        val etMaterno = findViewById<EditText>(R.id.etMaterno)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarWorker)
        val tvTitulo = findViewById<TextView>(R.id.tvTituloWorker)

        // Modo Edición
        idEditar = intent.getStringExtra("ID")
        if (idEditar != null) {
            tvTitulo.text = "Editar Trabajador"
            etDni.setText(intent.getStringExtra("DNI"))
            etNombres.setText(intent.getStringExtra("NOM"))
            etPaterno.setText(intent.getStringExtra("APP"))
            etMaterno.setText(intent.getStringExtra("APM"))
            etEmail.setText(intent.getStringExtra("MAIL"))
            etPass.hint = "Nueva contraseña (Opcional)" // Solo visual por ahora
            btnGuardar.text = "Actualizar Datos"
        }

        btnGuardar.setOnClickListener {
            val dni = etDni.text.toString().trim()
            val nombres = etNombres.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (dni.isEmpty() || nombres.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Objeto Usuario
            val datos = hashMapOf(
                "dni" to dni,
                "nombres" to nombres,
                "ap_paterno" to etPaterno.text.toString().trim(),
                "ap_materno" to etMaterno.text.toString().trim(),
                "email" to email,
                "rol" to "Trabajador",
                "estado" to "Activo"
            )

            if (idEditar != null) {
                // EDITAR: Update directo
                db.collection("usuarios").document(idEditar!!).update(datos as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Trabajador actualizado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            } else {
                // CREAR: Nuevo documento
                // NOTA: Esto crea el dato, pero para login real se requiere crear usuario en Auth.
                db.collection("usuarios").add(datos)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Trabajador registrado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
        }
    }
}