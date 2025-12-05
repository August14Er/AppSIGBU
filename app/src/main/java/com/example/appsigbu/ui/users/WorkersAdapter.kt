package com.example.appsigbu.ui.users

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appsigbu.R
import com.example.appsigbu.data.model.Usuario

class WorkersAdapter(
    private var lista: List<Usuario>,
    private val onLongClick: (Usuario) -> Unit
) : RecyclerView.Adapter<WorkersAdapter.WorkerViewHolder>() {

    class WorkerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInicial: TextView = view.findViewById(R.id.tvInicial)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCompleto)
        val tvDni: TextView = view.findViewById(R.id.tvDni)
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_worker, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val user = lista[position]

        holder.tvNombre.text = "${user.nombres} ${user.ap_paterno}"
        holder.tvDni.text = "DNI: ${user.dni}"
        holder.tvEmail.text = user.email
        holder.tvInicial.text = user.nombres.take(1).uppercase()
        holder.tvEstado.text = user.estado

        // Estilos de estado
        if (user.estado == "Activo") {
            holder.tvEstado.setTextColor(Color.parseColor("#198754")) // Verde
            holder.tvEstado.setBackgroundColor(Color.parseColor("#E8F5E9"))
        } else {
            holder.tvEstado.setTextColor(Color.parseColor("#DC3545")) // Rojo
            holder.tvEstado.setBackgroundColor(Color.parseColor("#F8D7DA"))
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(user)
            true
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nueva: List<Usuario>) {
        lista = nueva
        notifyDataSetChanged()
    }
}