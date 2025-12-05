package com.example.appsigbu.ui.inventory

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appsigbu.R
import com.example.appsigbu.data.model.Bien
import com.example.appsigbu.data.model.Solicitud
import com.example.appsigbu.ui.inventory.viewmodel.InventoryViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InventoryActivity : AppCompatActivity() {

    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var adapter: InventoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        // 1. Configurar RecyclerView con Callback de click largo
        val rvBienes = findViewById<RecyclerView>(R.id.rvBienes)
        rvBienes.layoutManager = LinearLayoutManager(this)

        // El adaptador recibe la lista vacía y la función que se ejecuta al mantener pulsado
        adapter = InventoryAdapter(emptyList()) { bienSeleccionado ->
            mostrarOpcionesBien(bienSeleccionado)
        }
        rvBienes.adapter = adapter

        // 2. Referencias UI
        val progressBar = findViewById<ProgressBar>(R.id.progressBarInv)
        val fabAgregar = findViewById<FloatingActionButton>(R.id.fabAgregar)

        // 3. Observadores ViewModel
        viewModel.isLoading.observe(this) { loading ->
            progressBar.isVisible = loading
        }

        viewModel.bienes.observe(this) { lista ->
            adapter.actualizarLista(lista)
            if (lista.isEmpty()) {
                Toast.makeText(this, "No hay bienes registrados aún", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Cargar datos iniciales
        viewModel.cargarInventario()

        // 5. Botón agregar: Abre la pantalla de Alta (AddBienActivity)
        fabAgregar.setOnClickListener {
            startActivity(Intent(this, AddBienActivity::class.java))
        }
    }

    private fun mostrarOpcionesBien(bien: Bien) {
        val opciones = arrayOf("Editar", "Eliminar (Dar de Baja)")

        AlertDialog.Builder(this)
            .setTitle("Acciones: ${bien.descripcion}")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> abrirEditar(bien) // Opción Editar
                    1 -> confirmarEliminar(bien) // Opción Eliminar
                }
            }
            .show()
    }

    private fun abrirEditar(bien: Bien) {
        val intent = Intent(this, AddBienActivity::class.java)
        // Pasamos TODOS los datos para rellenar el formulario de edición
        intent.putExtra("ID_BIEN", bien.id)
        intent.putExtra("DESC", bien.descripcion)
        intent.putExtra("COD", bien.codigo)
        intent.putExtra("SERIE", bien.serie)
        intent.putExtra("COLOR", bien.color)
        intent.putExtra("UBICACION", bien.ubicacion)
        intent.putExtra("ESTADO", bien.estado)
        // Nota: El ID del área se manejará en la Activity de destino
        startActivity(intent)
    }

    private fun confirmarEliminar(bien: Bien) {
        // Pedir sustento para eliminar
        val input = EditText(this)
        input.hint = "Motivo de la baja (Obligatorio)..."

        AlertDialog.Builder(this)
            .setTitle("Confirmar Baja")
            .setMessage("Se creará una solicitud para eliminar el bien: '${bien.descripcion}'.")
            .setView(input)
            .setPositiveButton("Enviar Solicitud") { _, _ ->
                val sustento = input.text.toString().trim()
                if (sustento.isNotEmpty()) {
                    crearSolicitudBaja(bien, sustento)
                } else {
                    Toast.makeText(this, "El sustento es obligatorio para eliminar.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun crearSolicitudBaja(bien: Bien, sustento: String) {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        val solicitud = Solicitud(
            usuario_id = user?.uid ?: "",
            usuario_nombre = user?.email ?: "Usuario",
            tipo = "Baja", // Importante: Tipo Baja para que salga Rojo en el Dashboard del Jefe
            estado = "Pendiente",
            sustento = sustento,
            items = listOf(bien.id), // ID del bien a eliminar
            datos_bien = mapOf(
                "descripcion" to bien.descripcion,
                "codigo" to bien.codigo
            )
        )

        db.collection("solicitudes").add(solicitud)
            .addOnSuccessListener {
                Toast.makeText(this, "Solicitud de baja enviada al Jefe", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al enviar solicitud", Toast.LENGTH_SHORT).show()
            }
    }
}