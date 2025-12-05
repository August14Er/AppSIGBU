package com.example.appsigbu.data.model

data class Area(
    var id: String = "",
    var nombre: String = "",
    var responsable: String = ""
) {
    // Sobrescribimos toString para que el Spinner muestre el nombre automáticamente
    override fun toString(): String {
        return nombre
    }
}