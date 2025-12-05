package com.example.appsigbu.ui.inventory

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appsigbu.R
import com.example.appsigbu.data.model.Bien

class InventoryAdapter(private var lista: List<Bien>,private val onLongClick: (Bien) -> Unit) :
    RecyclerView.Adapter<InventoryAdapter.BienViewHolder>() {

    class BienViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCodigo: TextView = view.findViewById(R.id.tvCodigo)

        val tvSerie: TextView = view.findViewById(R.id.tvSerie)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val tvUbicacion: TextView = view.findViewById(R.id.tvUbicacion)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BienViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bien, parent, false)
        return BienViewHolder(view)
    }

    override fun onBindViewHolder(holder: BienViewHolder, position: Int) {
        val bien = lista[position]
        holder.tvCodigo.text = "COD: ${bien.codigo.ifEmpty { bien.serie }}"
        if (bien.serie.isNotEmpty()) {
            holder.tvSerie.visibility = View.VISIBLE
            holder.tvSerie.text = "SERIE: ${bien.serie}"
        } else {
            holder.tvSerie.visibility = View.GONE
        }
        holder.tvDescripcion.text = bien.descripcion
        holder.tvUbicacion.text = "Ubicación: ${bien.ubicacion}"
        holder.tvEstado.text = bien.estado
        if (bien.estado == "Malo" || bien.estado == "Baja") {
            holder.tvEstado.setTextColor(Color.RED)
            holder.tvEstado.setBackgroundColor(Color.parseColor("#FDECEC")) // Fondo rojizo suave
        } else {
            holder.tvEstado.setTextColor(Color.parseColor("#2E7D32")) // Verde
            holder.tvEstado.setBackgroundColor(Color.parseColor("#E8F5E9")) // Fondo verdoso suave
        }
        holder.itemView.setOnLongClickListener {
            onLongClick(bien) // Disparamos el evento hacia la Activity
            true
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<Bien>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}