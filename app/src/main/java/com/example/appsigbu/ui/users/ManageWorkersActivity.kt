package com.example.appsigbu.ui.users

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appsigbu.R
import com.example.appsigbu.data.model.Usuario
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class ManageWorkersActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: WorkersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_workers)

        val rv = findViewById<RecyclerView>(R.id.rvWorkers)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = WorkersAdapter(emptyList()) { user ->
            mostrarOpciones(user)
        }
        rv.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAddWorker).setOnClickListener {
            startActivity(Intent(this, AddWorkerActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarTrabajadores()
    }

    private fun cargarTrabajadores() {
        // Traer todos los que NO sean Jefe (o todos si prefieres)
        db.collection("usuarios")
            .whereNotEqualTo("rol", "Jefe")
            .get()
            .addOnSuccessListener { res ->
                val lista = mutableListOf<Usuario>()
                for (doc in res) {
                    val u = doc.toObject(Usuario::class.java)
                    u.id = doc.id
                    lista.add(u)
                }
                adapter.actualizarLista(lista)
            }
    }

    private fun mostrarOpciones(user: Usuario) {
        val opciones = arrayOf("Editar Datos", "Dar de Baja (Obsoleto)")
        AlertDialog.Builder(this)
            .setTitle(user.nombres)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> {
                        val i = Intent(this, AddWorkerActivity::class.java)
                        i.putExtra("ID", user.id)
                        i.putExtra("DNI", user.dni)
                        i.putExtra("NOM", user.nombres)
                        i.putExtra("APP", user.ap_paterno)
                        i.putExtra("APM", user.ap_materno)
                        i.putExtra("MAIL", user.email)
                        startActivity(i)
                    }
                    1 -> confirmarBaja(user)
                }
            }
            .show()
    }

    private fun confirmarBaja(user: Usuario) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Baja")
            .setMessage("¿Deshabilitar acceso a ${user.nombres}?")
            .setPositiveButton("Sí") { _, _ ->
                // Actualizar estado a Obsoleto
                db.collection("usuarios").document(user.id)
                    .update("estado", "Obsoleto")
                    .addOnSuccessListener {
                        Toast.makeText(this, "Usuario dado de baja", Toast.LENGTH_SHORT).show()
                        cargarTrabajadores()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }
}