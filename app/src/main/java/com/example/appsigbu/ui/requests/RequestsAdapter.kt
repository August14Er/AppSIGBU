package com.example.appsigbu.ui.requests

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appsigbu.R
import com.example.appsigbu.data.model.Solicitud
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequestsAdapter(
    private var lista: List<Solicitud>,
    private val onClick: (Solicitud) -> Unit
) : RecyclerView.Adapter<RequestsAdapter.RequestViewHolder>() {

    class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val viewIndicator: View = view.findViewById(R.id.viewIndicator)
        val tvTipo: TextView = view.findViewById(R.id.tvTipo)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvSolicitante: TextView = view.findViewById(R.id.tvSolicitante)
        val tvResumen: TextView = view.findViewById(R.id.tvResumen)
        val ivIcono: ImageView = view.findViewById(R.id.ivIconoTipo)
        val cardRoot: View = view.findViewById(R.id.cardRoot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val item = lista[position]

        holder.tvTipo.text = item.tipo.uppercase()
        holder.tvSolicitante.text = "Por: ${item.usuario_nombre}"
        holder.tvResumen.text = "\"${item.sustento}\""

        // Formatear Fecha (Timestamp de Firebase)
        try {
            val timestamp = item.fecha as? com.google.firebase.Timestamp
            val date = timestamp?.toDate() ?: Date()
            holder.tvFecha.text = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            holder.tvFecha.text = "Hoy"
        }

        // --- LÓGICA DE COLORES (Igual al Web) ---
        val color: Int
        val icono: Int

        when (item.tipo) {
            "Transferir" -> {
                color = Color.parseColor("#0DCAF0") // Cyan/Info
                icono = android.R.drawable.ic_menu_send
            }
            "Agregar", "Alta" -> {
                color = Color.parseColor("#198754") // Verde/Success
                icono = android.R.drawable.ic_input_add
            }
            "Eliminar", "Baja" -> {
                color = Color.parseColor("#DC3545") // Rojo/Danger
                icono = android.R.drawable.ic_delete
            }
            else -> {
                color = Color.parseColor("#FFC107") // Amarillo/Warning
                icono = android.R.drawable.ic_menu_edit
            }
        }

        holder.viewIndicator.setBackgroundColor(color)
        holder.tvTipo.setTextColor(color)
        holder.ivIcono.setColorFilter(color)
        holder.ivIcono.setImageResource(icono)

        holder.cardRoot.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<Solicitud>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}