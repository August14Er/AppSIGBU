package com.example.appsigbu.data.model

data class Solicitud(
    var id: String = "",
    var usuario_id: String = "",
    var usuario_nombre: String = "",
    var fecha: Any? = null, // Usaremos FieldValue.serverTimestamp()
    var tipo: String = "Transferir",
    var estado: String = "Pendiente",
    var sustento: String = "",
    var area_origen_id: String = "",
    var area_origen_nombre: String = "",
    var area_destino_id: String = "",
    var area_destino_nombre: String = "",
    var items: List<String> = emptyList(), // Guardaremos los IDs de los bienes
    var datos_bien: Map<String, String>? = null
)