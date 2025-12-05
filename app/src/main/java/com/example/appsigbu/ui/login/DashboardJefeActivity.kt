package com.example.appsigbu.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.appsigbu.R
import com.example.appsigbu.ui.inventory.InventoryActivity
import com.example.appsigbu.ui.requests.RequestsActivity
import com.example.appsigbu.ui.transfer.TransferActivity
import com.example.appsigbu.ui.location.LocationActivity
import com.google.firebase.auth.FirebaseAuth // <--- Importante
import com.example.appsigbu.ui.users.ManageWorkersActivity

class DashboardJefeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_jefe)

        val nombre = intent.getStringExtra("USER_NAME") ?: "Jefe"
        findViewById<TextView>(R.id.tvBienvenida).text = "Bienvenido, $nombre"

        // Opción Inventario
        findViewById<CardView>(R.id.cardInventario).setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        // Opción Solicitudes
        findViewById<CardView>(R.id.cardSolicitudes).setOnClickListener {
            startActivity(Intent(this, RequestsActivity::class.java))
        }

        findViewById<CardView>(R.id.cardTransferencias).setOnClickListener {
            startActivity(Intent(this, TransferActivity::class.java))
        }
        findViewById<CardView>(R.id.cardUsuarios).setOnClickListener {
            startActivity(Intent(this, ManageWorkersActivity::class.java))
        }
        findViewById<CardView>(R.id.cardUbicacion).setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }

        // --- CERRAR SESIÓN (CORREGIDO) ---
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            // 1. Cerrar sesión en Firebase
            FirebaseAuth.getInstance().signOut()

            // 2. Ir al Login y limpiar historial
            val intent = Intent(this, LoginActivity::class.java)
            // Estas banderas borran todo el historial anterior para que "Atrás" no funcione
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // 3. Cerrar esta actividad
            finish()
        }
    }
}