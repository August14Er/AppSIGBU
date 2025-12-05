package com.example.appsigbu

import android.app.Application
import com.google.firebase.FirebaseApp

class SigbuApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar Firebase explícitamente ayuda a evitar errores de contexto
        FirebaseApp.initializeApp(this)
    }
}