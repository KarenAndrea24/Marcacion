package com.example.marcacion.ui.marcacion.presenter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.marcacion.data.dto.model.StateSearchDni
import com.example.marcacion.databinding.ActivityMarcacionBinding
import com.google.android.gms.location.*
import java.io.File

class MarcacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarcacionBinding
    private val viewModel by viewModels<MarcacionViewModel>()
    private var imageCapture: ImageCapture? = null

    // Para geolocalización
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarcacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupUI()
        startLocationUpdates()
        observerBuscarDNI()
    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            imageCapture = ImageCapture.Builder().build()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Toast.makeText(this, "Error al iniciar la cámara", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun convertImageFileToBase64(file: File): String {
        val bytes = file.readBytes()
        return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(externalMediaDirs.firstOrNull(), "${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@MarcacionActivity, "Error al capturar la foto: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = photoFile.toUri()
                    //TODO: Enviar la foto a la API como base64
                    val base64String = convertImageFileToBase64(photoFile)
                    binding.imgPhoto.setImageURI(savedUri)
                }
            })
    }

    private fun setupUI() {
        //solicitar permisos de la camera
        if (this.allPermissionsGranted()) {
            this.startCamera()
        } else {
            this.requestPermissions.launch(arrayOf(Manifest.permission.CAMERA))
        }

        // Capturar foto
        binding.btnCapturePhoto.setOnClickListener {
            this.takePhoto()
        }

        // Buscar DNI
        binding.btnFetchName.setOnClickListener {
            val dni = binding.etDNI.text.toString()
            if (dni.length == 8) {
                viewModel.obtenerNombrePorDNI(dni)
            } else {
                Toast.makeText(this, "Ingrese un DNI válido", Toast.LENGTH_SHORT).show()
            }
        }

        // Actualizar fecha y hora en tiempo real
        val fechaHoraThread = Thread {
            while (!Thread.interrupted()) {
                runOnUiThread {
                    binding.tvDateTime.text = java.text.SimpleDateFormat(
                        "dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()
                    ).format(java.util.Date())
                }
                Thread.sleep(1000)
            }
        }
        fechaHoraThread.start()

        // Obtener ubicación
        binding.btnGetLocation.setOnClickListener {
            println("🟢 Botón 'Obtener Ubicación' presionado")
            Toast.makeText(this, "Obteniendo ubicación...", Toast.LENGTH_SHORT).show()
            checkGPSAndRequestLocation()  // ⚡ Primero revisa si el GPS está encendido
        }
    }



    private fun showLoading() {
        binding.loginRlLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loginRlLoading.visibility = View.GONE
    }

    private fun observerBuscarDNI() {
        viewModel.data.observe(this) { data ->
            when (data) {
                is StateSearchDni.Success -> {
                    hideLoading()
                    binding.tvUserName.text = "Nombre: ${data.info.data.name}"
                }

                is StateSearchDni.Loading -> {
                    showLoading()
                }

                is StateSearchDni.Error -> {
                    hideLoading()
                }
            }
        }
    }

    private fun checkGPSAndRequestLocation() {
        val locationManager = getSystemService(LOCATION_SERVICE) as android.location.LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)

        if (!isGPSEnabled) {
            println("⚠ GPS DESACTIVADO. Solicitando activación...")

            // 📌 Mostrar diálogo para activar GPS
            val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)

            Toast.makeText(this, "Activa el GPS para obtener ubicación", Toast.LENGTH_LONG).show()
        } else {
            println("✅ GPS ACTIVADO. Procediendo a obtener ubicación...")
            startLocationUpdates()  // 📡 Iniciar ubicación si el GPS está activado
        }
    }


    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000  // Actualización cada 10 segundos
            fastestInterval = 5000  // Intervalo rápido
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    println("📍 Nueva ubicación recibida: ${location.latitude}, ${location.longitude}")

                    runOnUiThread {
                        binding.tvLocation.text = "Ubicación: ${location.latitude}, ${location.longitude}"
                    }
                }
            }
        }

        // 📌 Verificar si los permisos de ubicación están concedidos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            println("⚠ Permisos de ubicación NO concedidos. Solicitando permisos...")
            requestPermissions()
            return
        }

        // 📌 Obtener la última ubicación conocida antes de iniciar actualizaciones en tiempo real
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                println("📍 Última ubicación conocida: ${location.latitude}, ${location.longitude}")
                runOnUiThread {
                    binding.tvLocation.text = "Última Ubicación: ${location.latitude}, ${location.longitude}"
                }
            } else {
                println("⚠ No se encontró una última ubicación conocida.")
            }
        }.addOnFailureListener { e ->
            println("❌ Error obteniendo última ubicación: ${e.message}")
        }

        // 📡 Iniciar actualizaciones en tiempo real
        println("📡 Iniciando requestLocationUpdates...")
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }


    private fun requestPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            println("✅ Permiso concedido. Iniciando ubicación...")
            startLocationUpdates()
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            println("❌ Permiso de ubicación denegado.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
