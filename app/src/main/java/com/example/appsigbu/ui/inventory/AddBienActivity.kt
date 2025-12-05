package com.example.appsigbu.ui.inventory

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appsigbu.R
import com.example.appsigbu.data.model.Area
import com.example.appsigbu.data.model.Solicitud
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddBienActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var idBienEditar: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bien)

        // Referencias UI
        val headerContainer = findViewById<LinearLayout>(R.id.headerContainer)
        val tvTitulo = findViewById<TextView>(R.id.tvTituloFormulario)

        // CORRECCIÓN: Quitamos la línea que causaba el error del ImageView

        val spArea = findViewById<AutoCompleteTextView>(R.id.spArea)
        val spEstado = findViewById<AutoCompleteTextView>(R.id.spEstado)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)

        // 1. Configurar Estados
        val estados = listOf("Bueno", "Regular", "Malo", "Nuevo")
        spEstado.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, estados))

        // Si es ALTA, poner valor por defecto. Si es EDICIÓN, lo ponemos más abajo.
        if (intent.getStringExtra("ID_BIEN") == null) {
            spEstado.setText(estados[0], false)
        }

        // 2. Cargar Áreas
        val listaAreas = mutableListOf<Area>()
        val adapterAreas = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaAreas)
        spArea.setAdapter(adapterAreas)

        db.collection("areas").get().addOnSuccessListener { res ->
            listaAreas.clear()
            for (doc in res) {
                val a = doc.toObject(Area::class.java)
                a.id = doc.id
                listaAreas.add(a)
            }
            adapterAreas.notifyDataSetChanged()
        }

        // 3. Revisar si es MODO EDITAR
        idBienEditar = intent.getStringExtra("ID_BIEN")

        if (idBienEditar != null) {
            // === MODO EDITAR (Amarillo) ===
            tvTitulo.text = "Modificar Bien"
            tvTitulo.setTextColor(Color.BLACK)

            val colorWarning = Color.parseColor("#FFC107")
            headerContainer.setBackgroundColor(colorWarning)

            btnGuardar.text = "Guardar Cambios"
            btnGuardar.backgroundTintList = ColorStateList.valueOf(colorWarning)
            btnGuardar.setTextColor(Color.BLACK)

            // Rellenar campos
            findViewById<EditText>(R.id.etDescripcion).setText(intent.getStringExtra("DESC"))
            findViewById<EditText>(R.id.etCodigo).setText(intent.getStringExtra("COD"))
            findViewById<EditText>(R.id.etSerie).setText(intent.getStringExtra("SERIE"))
            findViewById<EditText>(R.id.etColor).setText(intent.getStringExtra("COLOR"))
            findViewById<EditText>(R.id.etUbicacion).setText(intent.getStringExtra("UBICACION"))

            val estadoActual = intent.getStringExtra("ESTADO")
            if (estadoActual != null) spEstado.setText(estadoActual, false)

        } else {
            // === MODO AGREGAR (Verde) ===
            tvTitulo.text = "Registrar Nuevo Bien"
            val colorSuccess = Color.parseColor("#198754")
            headerContainer.setBackgroundColor(colorSuccess)
            btnGuardar.backgroundTintList = ColorStateList.valueOf(colorSuccess)
        }

        // --- BOTONES ---
        btnCancelar.setOnClickListener { finish() }

        btnGuardar.setOnClickListener {
            enviarSolicitud(listaAreas)
        }
    }

    private fun enviarSolicitud(listaAreas: List<Area>) {
        // Referencias locales para leer texto
        val etDescripcion = findViewById<EditText>(R.id.etDescripcion)
        val etCodigo = findViewById<EditText>(R.id.etCodigo)
        val etSerie = findViewById<EditText>(R.id.etSerie)
        val etColor = findViewById<EditText>(R.id.etColor)
        val etUbicacion = findViewById<EditText>(R.id.etUbicacion)
        val etSustento = findViewById<EditText>(R.id.etSustento)
        val spArea = findViewById<AutoCompleteTextView>(R.id.spArea)
        val spEstado = findViewById<AutoCompleteTextView>(R.id.spEstado)

        // Validaciones
        if (etDescripcion.text.isNullOrEmpty() || etSustento.text.isNullOrEmpty()) {
            Toast.makeText(this, "Descripción y Sustento son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val nombreArea = spArea.text.toString()
        val areaObj = listaAreas.find { it.nombre == nombreArea }

        if (areaObj == null) {
            Toast.makeText(this, "Seleccione un área válida", Toast.LENGTH_SHORT).show()
            return
        }

        // Mapa de datos
        val datosBien = hashMapOf(
            "descripcion" to etDescripcion.text.toString().trim(),
            "codigo" to etCodigo.text.toString().trim(),
            "serie" to etSerie.text.toString().trim(),
            "color" to etColor.text.toString().trim(),
            "estado" to spEstado.text.toString(),
            "ubicacion" to etUbicacion.text.toString().trim(),
            "area_id" to areaObj.id
        )

        val user = FirebaseAuth.getInstance().currentUser

        // Crear Solicitud
        val solicitud = Solicitud(
            usuario_id = user?.uid ?: "",
            usuario_nombre = user?.email ?: "Usuario",
            sustento = etSustento.text.toString().trim(),
            tipo = if (idBienEditar == null) "Alta" else "Editar",
            items = if (idBienEditar == null) emptyList() else listOf(idBienEditar!!),
            datos_bien = datosBien
        )

        // Enviar a Firebase
        db.collection("solicitudes").add(solicitud)
            .addOnSuccessListener {
                Toast.makeText(this, "Solicitud enviada correctamente", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}