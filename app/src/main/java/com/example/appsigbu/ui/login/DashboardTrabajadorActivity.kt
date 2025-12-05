package com.example.appsigbu.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.appsigbu.R
import com.example.appsigbu.ui.inventory.InventoryActivity
import com.example.appsigbu.ui.transfer.TransferActivity
import com.example.appsigbu.ui.location.LocationActivity
import com.google.firebase.auth.FirebaseAuth // <--- Importante

class DashboardTrabajadorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_trabajador)

        // 1. Saludo personalizado
        val nombre = intent.getStringExtra("USER_NAME") ?: "Compañero"
        findViewById<TextView>(R.id.tvBienvenidaTrab).text = "Hola, $nombre"

        // 2. Navegación: Nueva Transferencia
        findViewById<CardView>(R.id.cardTransferencia).setOnClickListener {
            startActivity(Intent(this, TransferActivity::class.java))
        }

        // 3. Navegación: Ver Inventario
        findViewById<CardView>(R.id.cardVerInventario).setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }
        findViewById<CardView>(R.id.cardVerUbicacion).setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }
        // 4. --- CERRAR SESIÓN (CORREGIDO) ---
        findViewById<Button>(R.id.btnLogoutTrab).setOnClickListener {
            // 1. Cerrar sesión en Firebase
            FirebaseAuth.getInstance().signOut()

            // 2. Ir al Login y limpiar historial
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // 3. Cerrar esta actividad
            finish()
        }
    }
}