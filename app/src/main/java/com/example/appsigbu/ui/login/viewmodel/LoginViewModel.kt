package com.example.appsigbu.ui.login.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appsigbu.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Estados observables
    private val _loginResult = MutableLiveData<Result<Usuario>>()
    val loginResult: LiveData<Result<Usuario>> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _loginResult.value = Result.failure(Exception("Complete todos los campos"))
            return
        }

        _isLoading.value = true

        // 1. Autenticar con Firebase Auth
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    // 2. Obtener datos extra (Rol) de Firestore
                    fetchUserData(uid)
                } else {
                    _isLoading.value = false
                    _loginResult.value = Result.failure(Exception("Error identificando usuario"))
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _loginResult.value = Result.failure(e)
            }
    }

    private fun fetchUserData(uid: String) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                _isLoading.value = false
                if (document.exists()) {
                    val usuario = document.toObject(Usuario::class.java)
                    if (usuario != null) {
                        usuario.id = uid // Asegurar que el ID esté en el objeto
                        _loginResult.value = Result.success(usuario)
                    } else {
                        _loginResult.value = Result.failure(Exception("Datos de usuario corruptos"))
                    }
                } else {
                    _loginResult.value = Result.failure(Exception("Usuario no encontrado en base de datos"))
                }
            }
            .addOnFailureListener {
                _isLoading.value = false
                _loginResult.value = Result.failure(it)
            }
    }
}