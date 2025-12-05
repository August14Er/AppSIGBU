package com.example.appsigbu.data.model

data class Bien(
    var id: String = "",             // ID del documento Firestore
    var codigo: String = "",         // Código patrimonial
    var serie: String = "",
    var descripcion: String = "",
    var color: String = "",
    var ubicacion: String = "",
    var area_id: String = "",        // Referencia al Área (ID)
    var estado: String = "Bueno"     // Bueno, Malo, etc.
)