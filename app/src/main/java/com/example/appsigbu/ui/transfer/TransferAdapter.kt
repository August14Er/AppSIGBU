package com.example.appsigbu.ui.transfer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appsigbu.R
import com.example.appsigbu.data.model.Bien

class TransferAdapter(private var lista: List<Bien>) :
    RecyclerView.Adapter<TransferAdapter.TransferViewHolder>() {

    // Set para guardar los IDs seleccionados
    val seleccionados = mutableSetOf<String>()

    class TransferViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCodigo: TextView = view.findViewById(R.id.tvCodigo)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val chkSeleccion: CheckBox = view.findViewById(R.id.chkSeleccion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferViewHolder {
        // Reusamos el layout 'item_bien' pero le haremos un truco en el Checkbox
        // O mejor, creamos un layout simple nuevo integrado aquí si quieres ahorrar archivos.
        // Por orden, usaremos un layout nuevo: item_bien_transfer.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bien_transfer, parent, false)
        return TransferViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransferViewHolder, position: Int) {
        val bien = lista[position]
        holder.tvCodigo.text = bien.codigo.ifEmpty { bien.serie }
        holder.tvDescripcion.text = bien.descripcion

        // Evitar problemas de reciclaje de vistas
        holder.chkSeleccion.setOnCheckedChangeListener(null)
        holder.chkSeleccion.isChecked = seleccionados.contains(bien.id)

        holder.chkSeleccion.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) seleccionados.add(bien.id)
            else seleccionados.remove(bien.id)
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<Bien>) {
        lista = nuevaLista
        seleccionados.clear() // Limpiar selección al cambiar de área
        notifyDataSetChanged()
    }
}