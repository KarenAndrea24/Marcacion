package com.example.marcacion.data.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.marcacion.data.dto.dataSource.getIdUser
import com.example.marcacion.data.dto.request.MarcacionRequest
import com.example.marcacion.database.dao.MarcacionDao
import com.example.marcacion.database.entities.MarcacionEntity
import com.example.marcacion.ui.marcacion.presenter.MarcacionViewModel
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationHelper(
    private val context: Context,
    private val dao: MarcacionDao,
    private val viewModel: MarcacionViewModel
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private lateinit var locationCallback: LocationCallback

    // Variables para almacenar temporalmente los datos del usuario
    var dni: String = ""
    var nombre: String = ""
    var base64String: String = ""

    /**
     * Verifica si el GPS está activado. Si no lo está, abre la configuración para que el usuario
     * lo habilite. Si sí lo está, inicia las actualizaciones de ubicación.
     */
    fun checkGPSAndRequestLocation() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!isGPSEnabled) {
            Log.d("LocationHelper", "GPS DESACTIVADO. Solicitando activación...")
            // Abrir ajustes para que el usuario active el GPS
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            // Como 'context' no siempre es una Activity, a veces se requiere FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            Log.d("LocationHelper", "GPS ACTIVADO. Iniciando actualizaciones de ubicación...")
            startLocationUpdates()
        }
    }

    /**
     * Inicia las actualizaciones de ubicación con el intervalo definido. Al recibir la primera
     * ubicación válida, inserta en la base de datos y detiene las actualizaciones.
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10_000  // Cada 10s
            fastestInterval = 5_000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                Log.d("LocationHelper", "Ubicación recibida: ${location.latitude}, ${location.longitude}")

                // Insertar en la base de datos
                CoroutineScope(Dispatchers.IO).launch {
                    val insertedId = dao.insertarMarcacion(
                        MarcacionEntity(
                            dni = dni,
                            nombre = nombre,
                            latitud = location.latitude,
                            longitud = location.longitude,
                            estado = 0
                        )
                    )

                    // insertedId contendrá el ID de la nueva fila insertada
                    if (insertedId > 0) {
                        // Inserción exitosa
                        Log.d("LocationHelper", "Marcación insertada con ID: $insertedId")
                        val marcacionRequest = getIdUser(context)?.let {
                            MarcacionRequest(
                                idUser = it,
                                idMarcacionUser = "13",
                                tipoMarcacion = "entrada",
                                fechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                                    Date()
                                ),
                                foto = base64String,
                                latitud = location.latitude.toString(),
                                longitud = location.longitude.toString()
                            )
                        }
                        if (marcacionRequest != null) {
                            viewModel.observerCrearMarcacion(marcacionRequest)
                        }
                    } else {
                        // Retornará 0 si, por alguna razón, Room no insertó nada
                        // (aunque normalmente lanzaría una excepción si falla)
                        Log.e("LocationHelper", "No se pudo insertar la marcación")
                    }
                }

                // Detenemos las actualizaciones para que no inserte múltiples veces
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        // Suponemos que ya se verificaron los permisos en la Activity
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    /**
     * Detiene las actualizaciones de ubicación si están activas.
     */
    fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
