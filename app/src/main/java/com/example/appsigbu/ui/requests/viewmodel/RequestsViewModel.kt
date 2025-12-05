package com.example.appsigbu.ui.requests.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appsigbu.data.model.Solicitud
import com.google.firebase.firestore.FirebaseFirestore

class RequestsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _solicitudes = MutableLiveData<List<Solicitud>>()
    val solicitudes: LiveData<List<Solicitud>> = _solicitudes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _mensajeOperacion = MutableLiveData<Result<String>>()
    val mensajeOperacion: LiveData<Result<String>> = _mensajeOperacion

    fun cargarSolicitudes() {
        _isLoading.value = true
        // Solo mostramos las Pendientes
        db.collection("solicitudes")
            .whereEqualTo("estado", "Pendiente")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.toObjects(Solicitud::class.java)
                // Asignar IDs manualmente
                for (i in 0 until result.size()) {
                    lista[i].id = result.documents[i].id
                }
                // Ordenar por fecha descendente (más recientes primero)
                // Nota: Firestore ordena mejor en el query, pero para MVP lo hacemos en memoria
                _solicitudes.value = lista.sortedByDescending { it.id }
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    fun aprobarSolicitud(solicitud: Solicitud) {
        _isLoading.value = true

        db.runTransaction { transaction ->
            val solicitudRef = db.collection("solicitudes").document(solicitud.id)

            when (solicitud.tipo) {
                "Transferir" -> {
                    // Lógica existente de mover bien
                    if (solicitud.items.isNotEmpty()) {
                        val bienRef = db.collection("bienes").document(solicitud.items[0])
                        transaction.update(bienRef, "area_id", solicitud.area_destino_id)
                        transaction.update(bienRef, "ubicacion", solicitud.area_destino_nombre)
                    }
                }
                "Alta" -> {
                    // CREAR NUEVO DOCUMENTO
                    val nuevoBienRef = db.collection("bienes").document() // ID auto
                    val datos = solicitud.datos_bien ?: emptyMap()
                    transaction.set(nuevoBienRef, datos)
                }
                "Baja", "Eliminar" -> {
                    // ELIMINAR DOCUMENTO (o cambiar estado a "Inactivo")
                    if (solicitud.items.isNotEmpty()) {
                        val bienRef = db.collection("bienes").document(solicitud.items[0])
                        transaction.delete(bienRef) // O transaction.update(bienRef, "estado", "Baja")
                    }
                }
                "Editar" -> {
                    // ACTUALIZAR DOCUMENTO
                    if (solicitud.items.isNotEmpty() && solicitud.datos_bien != null) {
                        val bienRef = db.collection("bienes").document(solicitud.items[0])
                        transaction.update(bienRef, solicitud.datos_bien!!)
                    }
                }
            }

            // Finalizar solicitud
            transaction.update(solicitudRef, "estado", "Aprobado")
        }.addOnSuccessListener {
            _isLoading.value = false
            _mensajeOperacion.value = Result.success("Solicitud ${solicitud.tipo} aprobada correctamente.")
            cargarSolicitudes()
        }.addOnFailureListener { e ->
            _isLoading.value = false
            _mensajeOperacion.value = Result.failure(e)
        }
    }

    fun rechazarSolicitud(solicitudId: String) {
        _isLoading.value = true
        db.collection("solicitudes").document(solicitudId)
            .update("estado", "Rechazado")
            .addOnSuccessListener {
                _isLoading.value = false
                _mensajeOperacion.value = Result.success("Solicitud rechazada.")
                cargarSolicitudes()
            }
            .addOnFailureListener {
                _isLoading.value = false
                _mensajeOperacion.value = Result.failure(it)
            }
    }
}