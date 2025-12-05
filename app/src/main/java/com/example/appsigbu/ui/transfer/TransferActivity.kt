package com.example.appsigbu.ui.transfer

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appsigbu.R
import com.example.appsigbu.data.model.Area
import com.google.android.material.textfield.TextInputEditText

class TransferActivity : AppCompatActivity() {

    private val viewModel: TransferViewModel by viewModels()
    private lateinit var adapter: TransferAdapter

    // Variables para guardar selección temporal
    private var areaOrigenSeleccionada: Area? = null
    private var areaDestinoSeleccionada: Area? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)

        // --- Referencias UI ---
        val layoutPaso1 = findViewById<LinearLayout>(R.id.layoutPaso1)
        val layoutPaso2 = findViewById<LinearLayout>(R.id.layoutPaso2)
        val spOrigen = findViewById<Spinner>(R.id.spOrigen)
        val spDestino = findViewById<Spinner>(R.id.spDestino)
        val etSustento = findViewById<TextInputEditText>(R.id.etSustento)
        val btnSiguiente = findViewById<Button>(R.id.btnSiguiente)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarTransfer)
        val rvBienes = findViewById<RecyclerView>(R.id.rvBienesTransfer)

        // Configurar RecyclerView
        rvBienes.layoutManager = LinearLayoutManager(this)
        adapter = TransferAdapter(emptyList())
        rvBienes.adapter = adapter

        // --- Observadores ViewModel ---

        // 1. Cargar Áreas en los Spinners
        viewModel.areas.observe(this) { listaAreas ->
            if (listaAreas.isNotEmpty()) {
                val adapterSpin = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaAreas)
                spOrigen.adapter = adapterSpin
                spDestino.adapter = adapterSpin
            } else {
                Toast.makeText(this, "No se encontraron áreas.", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Cargar Bienes en la lista (Paso 2)
        viewModel.bienesOrigen.observe(this) { listaBienes ->
            if (listaBienes.isEmpty()) {
                Toast.makeText(this, "El área origen no tiene bienes.", Toast.LENGTH_LONG).show()
                // Volver al paso 1 si no hay nada
                layoutPaso2.visibility = View.GONE
                layoutPaso1.visibility = View.VISIBLE
            } else {
                adapter.actualizarLista(listaBienes)
            }
        }

        // 3. Control de Carga
        viewModel.isLoading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            btnSiguiente.isEnabled = !loading
            btnConfirmar.isEnabled = !loading
        }

        // 4. Éxito al guardar
        viewModel.operacionExitosa.observe(this) { result ->
            result.onSuccess { msg ->
                Toast.makeText(this, "¡Éxito! $msg", Toast.LENGTH_LONG).show()
                finish() // Cerramos la pantalla al terminar
            }.onFailure { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // --- Eventos Botones ---

        // BOTÓN SIGUIENTE (Paso 1 -> Paso 2)
        btnSiguiente.setOnClickListener {
            val origen = spOrigen.selectedItem as? Area
            val destino = spDestino.selectedItem as? Area
            val sustento = etSustento.text.toString().trim()

            if (origen == null || destino == null) {
                Toast.makeText(this, "Cargando áreas...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Código corregido
            if (origen.id == destino.id) {
                Toast.makeText(this, "El origen y destino no pueden ser iguales", Toast.LENGTH_LONG).show() // O LENGTH_SHORT
                return@setOnClickListener
            }

            if (sustento.isEmpty()) {
                etSustento.error = "Requerido"
                return@setOnClickListener
            }

            // Guardamos referencias y avanzamos
            areaOrigenSeleccionada = origen
            areaDestinoSeleccionada = destino

            // Cambiamos de pantalla visualmente
            layoutPaso1.visibility = View.GONE
            layoutPaso2.visibility = View.VISIBLE

            // Pedimos los bienes al ViewModel
            viewModel.cargarBienesDeArea(origen.id)
        }

        // BOTÓN CANCELAR (Paso 2 -> Paso 1)
        btnCancelar.setOnClickListener {
            layoutPaso2.visibility = View.GONE
            layoutPaso1.visibility = View.VISIBLE
        }

        // BOTÓN CONFIRMAR (Enviar a Firebase)
        btnConfirmar.setOnClickListener {
            val idsSeleccionados = adapter.seleccionados.toList() // Convertimos el Set a List
            val sustento = etSustento.text.toString().trim()

            // Código corregido
            if (idsSeleccionados.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos un bien", Toast.LENGTH_LONG).show() // O LENGTH_SHORT
                return@setOnClickListener
            }


            // ¡Disparamos el envío!
            viewModel.enviarSolicitud(
                areaOrigenSeleccionada!!,
                areaDestinoSeleccionada!!,
                sustento,
                idsSeleccionados
            )
        }

        // Carga inicial de datos
        viewModel.cargarAreas()
    }
}