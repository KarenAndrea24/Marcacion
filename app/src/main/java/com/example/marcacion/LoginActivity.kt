package com.example.marcacion


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.provider.Settings
import android.net.Uri
import com.example.marcacion.data.service.LocationService

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Ajustar insets para bordes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Verificar y solicitar permisos
        checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION) // Android 10+
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS) // Android 13+
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            // Verificar si se debe mostrar el cuadro de permisos o si el usuario marcó "No volver a preguntar"
            val shouldShowRationale = permissionsToRequest.any { shouldShowRequestPermissionRationale(it) }

            if (shouldShowRationale) {
                // Si el usuario no ha marcado "No volver a preguntar", pedimos los permisos
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            } else {
                // Si el usuario marcó "No volver a preguntar", mostramos un mensaje y lo redirigimos a Configuración
                Toast.makeText(this, "Debes habilitar los permisos manualmente en Configuración.", Toast.LENGTH_LONG).show()
                openAppSettings()
            }
        } else {
            // Si todos los permisos ya fueron concedidos, iniciar la ubicación
            iniciarServicioUbicacion()
        }
    }

    // Callback para manejar la respuesta del usuario al solicitar permisos
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val deniedPermissions = permissions.filterValues { !it }

            if (deniedPermissions.isNotEmpty()) {
                Toast.makeText(this, "Algunos permisos fueron denegados.", Toast.LENGTH_SHORT).show()
            } else {
                iniciarServicioUbicacion()
            }
        }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun iniciarServicioUbicacion() {
        val intent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detenerServicioUbicacion()
    }

    private fun detenerServicioUbicacion() {
        val intent = Intent(this, LocationService::class.java)
        stopService(intent)
    }
}