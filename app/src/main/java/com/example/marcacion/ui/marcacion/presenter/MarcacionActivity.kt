package com.example.marcacion.ui.marcacion.presenter

import android.Manifest
//noinspection SuspiciousImport
import android.R as AndroidR
import com.example.marcacion.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.marcacion.data.dto.model.StateMarcacion
import com.example.marcacion.data.dto.model.StateSearchDni
import com.example.marcacion.data.service.LocationHelper
import com.example.marcacion.data.service.MarcacioneSyncWorker
import com.example.marcacion.data.utils.CameraUtils
import com.example.marcacion.database.MarcacionDatabase
import com.example.marcacion.database.dao.MarcacionDao
import com.example.marcacion.databinding.ActivityMarcacionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MarcacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarcacionBinding
    private val viewModel by viewModels<MarcacionViewModel>()
    private var imageCapture: ImageCapture? = null
    private var useFrontCamera = false

    // Base de datos y DAO
    private lateinit var db: MarcacionDatabase
    private lateinit var dao: MarcacionDao

    // Clase helper para la ubicación
    private lateinit var locationHelper: LocationHelper

    private lateinit var nombre: String
    private lateinit var dni: String
    private lateinit var tipoMarcacionSeleccionado: String
    private var idUserMarcado: String = ""

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarcacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa la base de datos y el DAO
        db = MarcacionDatabase.getDatabase(applicationContext)
        dao = db.marcacionDao()

        // Inicializa LocationHelper
        locationHelper = LocationHelper(this, dao, viewModel)

        setupUI()
        observerBuscarDNI()
        observerCrearMarcacion()

        // Configura las restricciones para WorkManager
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Configura WorkManager para sincronización periódica
//        val workRequest = PeriodicWorkRequestBuilder<MarcacioneSyncWorker>(
////            15, // Mínimo 15 minutos
//            1, // Mínimo 1 minuto
//            TimeUnit.MINUTES
//        )
//            .setConstraints(constraints)
//            .build()
//
//        WorkManager.getInstance(applicationContext)
//            .enqueueUniquePeriodicWork(
//                "MarcacionSyncWork",
//                ExistingPeriodicWorkPolicy.KEEP,
//                workRequest
//            )

        val workRequest = OneTimeWorkRequestBuilder<MarcacioneSyncWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(applicationContext).enqueue(workRequest)

    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true) {
                startCamera()
            } else {
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

//          val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            val cameraSelector = if (useFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            imageCapture = ImageCapture.Builder().build()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.previewView.surfaceProvider
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e("MarcacionActivity", "Error al inicializar la cámara", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupUI() {
        // Solicitar permisos de la cámara
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions.launch(arrayOf(Manifest.permission.CAMERA))
        }
        // Botón para alternar entre cámaras
        binding.btnSwitchCamera.setOnClickListener {
            useFrontCamera = !useFrontCamera
            startCamera() // Reinicia la cámara con la nueva selección
        }

        // Botón para capturar foto
        binding.btnCapturePhoto.setOnClickListener {
            takePhoto()
        }

        // Botón para buscar DNI
        binding.btnFetchName.setOnClickListener {
            dni = binding.etDNI.text.toString()
            if (dni.length == 8) {
                viewModel.obtenerNombrePorDNI(dni)
            } else {
            }
        }

        // Hilo para actualizar fecha y hora
        val fechaHoraThread = Thread {
            while (!Thread.interrupted()) {
                runOnUiThread {
                    binding.tvDateTime.text = SimpleDateFormat(
                        "dd/MM/yyyy HH:mm:ss", Locale.getDefault()
                    ).format(Date())
                }
                Thread.sleep(1000)
            }
        }
        fechaHoraThread.start()

        // Configurar opciones del Spinner
        val opciones = listOf("Entrada", "Salida")
        val adapter = ArrayAdapter(this, AndroidR.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spTipoMarcacion.adapter = adapter

        // Escuchar selección del usuario
        binding.spTipoMarcacion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                tipoMarcacionSeleccionado = opciones[position].lowercase() // "entrada" o "salida"
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        // Botón para la marcacion con ubicación
        binding.btnMarcacion.setOnClickListener {

            // Pasamos los datos al helper
            locationHelper.dni = dni
            locationHelper.nombre = nombre
            locationHelper.idUserMarcado = idUserMarcado
            locationHelper.tipoMarcacion = tipoMarcacionSeleccionado // Enviar selección

            // Primero revisamos permisos de ubicación
            checkLocationPermissions()
        }
    }

    private fun checkLocationPermissions() {
        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Pedir permisos
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Si ya tenemos permisos, delegamos la lógica al LocationHelper
            locationHelper.checkGPSAndRequestLocation()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            locationHelper.checkGPSAndRequestLocation()
        } else {
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(externalMediaDirs.firstOrNull(), "${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("MarcacionActivity", "Error al capturar la foto", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // 1. Decodifica el archivo a Bitmap
                    val originalBitmap =
                        android.graphics.BitmapFactory.decodeFile(photoFile.absolutePath)

                    // 2. Corrige la orientación según EXIF
                    val correctedBitmap =
                        CameraUtils().fixImageOrientation(photoFile, originalBitmap)

                    // 3. Comprime la imagen ya rotada
                    val compressedData =
                        CameraUtils().compressImageToMaxSize(correctedBitmap, 500)  // 500 KB

                    // 4. Convertir a Base64
                    val base64String = convertToBase64(compressedData)

                    // 5. Mostrar la imagen comprimida (y rotada) en el ImageView
                    val compressedBitmap = android.graphics.BitmapFactory.decodeByteArray(
                        compressedData, 0, compressedData.size
                    )
                    binding.imgPhoto.setImageBitmap(compressedBitmap)

                    // 6. Guardar la imagen base64 en tu LocationHelper
                    locationHelper.base64String = base64String
                }

            })
    }

    private fun convertToBase64(compressedData: ByteArray): String {
        return "data:image/jpeg;base64," + android.util.Base64.encodeToString(
            compressedData,
            android.util.Base64.DEFAULT
        )
    }

    private fun observerBuscarDNI() {
        viewModel.data.observe(this) { data ->
            when (data) {
                is StateSearchDni.Success -> {
                    hideLoading()
//                    nombre = data.info.data.name
                    nombre = "${data.info.data.name} ${data.info.data.lastNameFather} ${data.info.data.lastNameMother ?: ""}".trim()
                    binding.tvUserName.text = "Nombre: $nombre"

                    idUserMarcado = data.info.data.id.toString()
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

    private fun observerCrearMarcacion() {
        viewModel.data_marcacion.observe(this) { data ->
            when (data) {
                is StateMarcacion.Success -> {
                    hideLoading()
                    //TODO: Actualizar la marcacion en la BD sqlite, pero actualizando
                    // el campo 'estado' con el 1 de sincronizado
                    // Ejecutar la actualización en un hilo de fondo
                    lifecycleScope.launch(Dispatchers.IO) {
                        dao.updateEstadoMarcacion()
                    }
//                    Toast.makeText(
//                        this,
//                        "Marcación exitosa: ${data.info.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
                    mostrarAlertaConIcono("Éxito", "Marcación registrada: ${data.info.message}", true)


                    limpiarCampos()
                }

                is StateMarcacion.Loading -> {
                    showLoading()
                }

                is StateMarcacion.Error -> {
                    hideLoading()
                    Log.e("MarcacionActivity", "Error en la marcación: ${viewModel.errorMessage.value}")

//                    Toast.makeText(this, "Error: ${data.message}", Toast.LENGTH_LONG).show()
                    mostrarAlertaConIcono("Error", data.message, false)
                }
            }
        }
    }

    private fun limpiarCampos() {
        binding.etDNI.text.clear()
        binding.tvUserName.text = "Nombre: ---"
        binding.imgPhoto.setImageResource(R.drawable.ic_camera)

        // Restablecer el spinner a la opción por defecto
        binding.spTipoMarcacion.setSelection(0)

        // Reiniciar datos en `LocationHelper`
        locationHelper.dni = ""
        locationHelper.nombre = ""
        locationHelper.base64String = ""
        locationHelper.idUserMarcado = ""
        locationHelper.tipoMarcacion = "entrada"

        Log.d("MarcacionActivity", "Campos limpiados para una nueva marcación.")
    }

    private fun showLoading() {
        binding.loginRlLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loginRlLoading.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detener las actualizaciones de ubicación (si están en curso)
        locationHelper.stopLocationUpdates()
    }


    fun mostrarAlertaConIcono(titulo: String, mensaje: String, esExito: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        builder.setMessage(mensaje)

        // Agregar ícono según el tipo de mensaje
        val icono = if (esExito) R.drawable.ic_success else R.drawable.ic_error
        builder.setIcon(icono)

        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

}
