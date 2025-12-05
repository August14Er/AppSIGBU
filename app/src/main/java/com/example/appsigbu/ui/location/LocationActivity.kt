package com.example.appsigbu.ui.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.appsigbu.R

class LocationActivity : AppCompatActivity(), LocationListener {

    private lateinit var locationManager: LocationManager
    private lateinit var tvCoordenadas: TextView
    private lateinit var btnVerMapa: Button

    // Variables para guardar la última ubicación
    private var latitudActual: Double = 0.0
    private var longitudActual: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        tvCoordenadas = findViewById(R.id.tvCoordenadas)
        btnVerMapa = findViewById(R.id.btnVerMapa)

        // --- CORRECCIÓN AQUÍ ---
        // Agregamos <Button> para que Kotlin sepa el tipo y habilite setOnClickListener
        val btnActualizar = findViewById<Button>(R.id.btnActualizar)

        // Inicializar el Manager como dice el PDF
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        btnActualizar.setOnClickListener {
            verificarPermisosYBuscar()
        }

        btnVerMapa.setOnClickListener {
            abrirGoogleMaps()
        }
    }

    private fun verificarPermisosYBuscar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            // Pedir permiso si no lo tenemos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        } else {
            // Si ya tenemos permiso, buscar
            obtenerUbicacion()
        }
    }

    private fun obtenerUbicacion() {
        try {
            tvCoordenadas.text = "Buscando satélites..."

            // Solicitamos actualizaciones (Mínimo tiempo 0, mínima distancia 0 para respuesta rápida)
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                this
            )

            // También intentamos red por si estamos bajo techo
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0L,
                0f,
                this
            )

        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // --- Implementación de LocationListener (Del PDF) ---

    override fun onLocationChanged(location: Location) {
        // Se ejecuta cuando el GPS detecta cambio
        latitudActual = location.latitude
        longitudActual = location.longitude

        val texto = "Latitud: $latitudActual\nLongitud: $longitudActual\n\n(Precisión: ${location.accuracy}m)"
        tvCoordenadas.text = texto

        // Habilitar botón de mapa
        btnVerMapa.isEnabled = true

        // Detener actualizaciones para ahorrar batería una vez encontrado
        locationManager.removeUpdates(this)
    }

    override fun onProviderEnabled(provider: String) {
        // GPS encendido
    }

    override fun onProviderDisabled(provider: String) {
        tvCoordenadas.text = "Por favor, active el GPS"
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // Deprecated en APIs nuevas pero necesario por la interfaz antigua
    }

    // --- Funciones Extra ---

    private fun abrirGoogleMaps() {
        val uri = "geo:$latitudActual,$longitudActual?q=$latitudActual,$longitudActual(Mi+Ubicacion)"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Si no tiene maps, abrir en navegador
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitudActual,$longitudActual")))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacion()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}