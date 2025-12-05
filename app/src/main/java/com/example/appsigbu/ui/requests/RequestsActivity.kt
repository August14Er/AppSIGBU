package com.example.appsigbu.ui.requests

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appsigbu.R
import com.example.appsigbu.data.model.Solicitud
import com.example.appsigbu.ui.requests.viewmodel.RequestsViewModel
import com.google.android.material.chip.ChipGroup

class RequestsActivity : AppCompatActivity() {

    private val viewModel: RequestsViewModel by viewModels()
    private lateinit var adapter: RequestsAdapter

    // Guardamos la lista completa para poder filtrar localmente
    private var listaCompleta: List<Solicitud> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        // Referencias UI
        val progressBar = findViewById<ProgressBar>(R.id.progressBarRequests)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        val rvRequests = findViewById<RecyclerView>(R.id.rvRequests)
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupFiltros)

        // Configurar RecyclerView
        rvRequests.layoutManager = LinearLayoutManager(this)
        adapter = RequestsAdapter(emptyList()) { solicitud ->
            mostrarDialogoDetalle(solicitud)
        }
        rvRequests.adapter = adapter

        // --- Observadores ---

        viewModel.isLoading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.solicitudes.observe(this) { lista ->
            listaCompleta = lista
            filtrarLista(chipGroup.checkedChipId) // Aplicar filtro actual
        }

        viewModel.mensajeOperacion.observe(this) { result ->
            result.onSuccess { msg ->
                Toast.makeText(this, "✅ $msg", Toast.LENGTH_SHORT).show()
            }.onFailure { e ->
                Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // --- Filtros (Chips) ---
        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            filtrarLista(checkedId)
        }

        // Carga inicial
        viewModel.cargarSolicitudes()
    }

    private fun filtrarLista(chipId: Int) {
        val rvRequests = findViewById<RecyclerView>(R.id.rvRequests)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        val listaFiltrada = when (chipId) {
            R.id.chipTransfer -> listaCompleta.filter { it.tipo == "Transferir" }
            R.id.chipBienes -> listaCompleta.filter { it.tipo == "Agregar" || it.tipo == "Alta" }
            else -> listaCompleta // R.id.chipTodos
        }

        adapter.actualizarLista(listaFiltrada)

        // Mostrar mensaje si está vacío
        if (listaFiltrada.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvRequests.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvRequests.visibility = View.VISIBLE
        }
    }

    private fun mostrarDialogoDetalle(solicitud: Solicitud) {
        val mensaje = """
            Tipo: ${solicitud.tipo.uppercase()}
            Solicitante: ${solicitud.usuario_nombre}
            
            Sustento:
            "${solicitud.sustento}"
            
            Origen: ${solicitud.area_origen_nombre}
            Destino: ${solicitud.area_destino_nombre}
            Items: ${solicitud.items.size} bien(es)
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Detalle de Solicitud")
            .setMessage(mensaje)
            .setPositiveButton("APROBAR") { _, _ ->
                confirmarAccion("Aprobar", solicitud)
            }
            .setNegativeButton("RECHAZAR") { _, _ ->
                confirmarAccion("Rechazar", solicitud)
            }
            .setNeutralButton("Cerrar", null)
            .show()
    }

    private fun confirmarAccion(accion: String, solicitud: Solicitud) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar $accion")
            .setMessage("¿Estás seguro de $accion esta solicitud?")
            .setPositiveButton("Sí") { _, _ ->
                if (accion == "Aprobar") {
                    viewModel.aprobarSolicitud(solicitud)
                } else {
                    viewModel.rechazarSolicitud(solicitud.id)
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}