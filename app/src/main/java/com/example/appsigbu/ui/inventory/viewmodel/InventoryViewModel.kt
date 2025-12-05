package com.example.appsigbu.ui.inventory.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appsigbu.data.model.Bien
import com.google.firebase.firestore.FirebaseFirestore

class InventoryViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _bienes = MutableLiveData<List<Bien>>()
    val bienes: LiveData<List<Bien>> = _bienes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun cargarInventario() {
        _isLoading.value = true

        // Leemos la colección "bienes"
        db.collection("bienes")
            .get()
            .addOnSuccessListener { result ->
                val listaTemp = mutableListOf<Bien>()
                for (document in result) {
                    val bien = document.toObject(Bien::class.java)
                    bien.id = document.id
                    listaTemp.add(bien)
                }
                _bienes.value = listaTemp
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
                // Aquí podrías manejar el error
            }
    }
}