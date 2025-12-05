package com.example.appsigbu.ui.transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appsigbu.data.model.Area
import com.example.appsigbu.data.model.Bien
import com.example.appsigbu.data.model.Solicitud
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class TransferViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // --- ESTADOS (LiveData) ---

    // Lista de Áreas para los Spinners
    private val _areas = MutableLiveData<List<Area>>()
    val areas: LiveData<List<Area>> = _areas

    // Lista de Bienes (filtrados por área origen)
    private val _bienesOrigen = MutableLiveData<List<Bien>>()
    val bienesOrigen: LiveData<List<Bien>> = _bienesOrigen

    // Estado de carga (para mostrar/ocultar progress bar)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Resultado de la operación de guardar (Éxito o Error)
    private val _operacionExitosa = MutableLiveData<Result<String>>()
    val operacionExitosa: LiveData<Result<String>> = _operacionExitosa


    // --- FUNCIONES ---

    fun cargarAreas() {
        _isLoading.value = true
        db.collection("areas")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.toObjects(Area::class.java)
                // Asignamos los IDs manuales ya que toObjects no lo hace automático
                for (i in 0 until result.size()) {
                    lista[i].id = result.documents[i].id
                }
                _areas.value = lista
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
                // Aquí podrías manejar el error
            }
    }

    fun cargarBienesDeArea(areaId: String) {
        _isLoading.value = true
        db.collection("bienes")
            .whereEqualTo("area_id", areaId) // ¡OJO! Esto requiere que tus bienes tengan este campo
            .get()
            .addOnSuccessListener { result ->
                val lista = mutableListOf<Bien>()
                for (doc in result) {
                    val b = doc.toObject(Bien::class.java)
                    b.id = doc.id
                    lista.add(b)
                }
                _bienesOrigen.value = lista
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    fun enviarSolicitud(
        origen: Area,
        destino: Area,
        sustento: String,
        idsBienes: List<String>
    ) {
        if (idsBienes.isEmpty()) {
            _operacionExitosa.value = Result.failure(Exception("No seleccionaste ningún bien."))
            return
        }

        _isLoading.value = true
        val user = auth.currentUser

        // Obtenemos nombre del usuario actual (opcional, consulta extra)
        // Para hacerlo rápido, usaremos el email o un nombre genérico por ahora
        val nombreUsuario = user?.email ?: "Desconocido"
        val uid = user?.uid ?: ""

        val nuevaSolicitud = Solicitud(
            usuario_id = uid,
            usuario_nombre = nombreUsuario,
            tipo = "Transferir",
            estado = "Pendiente",
            sustento = sustento,
            area_origen_id = origen.id,
            area_origen_nombre = origen.nombre,
            area_destino_id = destino.id,
            area_destino_nombre = destino.nombre,
            items = idsBienes,
            // fecha = FieldValue.serverTimestamp() // Se asigna al guardar
        )

        // Guardamos en Firebase
        db.collection("solicitudes")
            .add(nuevaSolicitud)
            .addOnSuccessListener { docRef ->
                // Actualizamos el campo de fecha con el timestamp del servidor
                docRef.update("fecha", FieldValue.serverTimestamp())
                _isLoading.value = false
                _operacionExitosa.value = Result.success("Solicitud enviada con ID: ${docRef.id}")
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _operacionExitosa.value = Result.failure(e)
            }
    }
}