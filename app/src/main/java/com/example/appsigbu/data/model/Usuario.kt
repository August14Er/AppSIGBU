package com.example.appsigbu.data.model

data class Usuario(
    var id: String = "",           // ID del documento en Firestore (uid)
    var nombres: String = "",
    var ap_paterno: String = "",
    var ap_materno: String = "",
    var dni: String = "",
    var email: String = "",        // Usaremos email para el login de Firebase
    var rol: String = "",          // "Jefe" o "Trabajador"
    var estado: String = "Activo"
)