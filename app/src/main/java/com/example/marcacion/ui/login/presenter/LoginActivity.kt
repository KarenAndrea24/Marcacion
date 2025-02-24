package com.example.marcacion.ui.login.presenter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.marcacion.databinding.ActivityLoginBinding
import com.example.marcacion.data.dto.dataSource.getIdUser
import com.example.marcacion.data.dto.dataSource.saveIdUser
import com.example.marcacion.data.dto.model.StateLogin
import com.example.marcacion.data.service.LocationService
import com.example.marcacion.ui.marcacion.presenter.MarcacionActivity

class LoginActivity : AppCompatActivity() {

    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        solicitarPermisosAlInicio() // Pedir permisos al inicio de la app

        actions()
        observerLogin()
    }

    /**
     * Verifica si los permisos ya están concedidos o si es necesario pedirlos.
     */
    private fun solicitarPermisosAlInicio() {
        val permisosNecesarios = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permisosNecesarios.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        val permisosFaltantes = permisosNecesarios.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permisosFaltantes.isNotEmpty()) {
            requestPermissionLauncher.launch(permisosFaltantes.toTypedArray())
        } else {
            iniciarServicioUbicacion()
        }
    }

    /**
     * Maneja la respuesta del usuario cuando acepta o rechaza los permisos.
     */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permisos ->
            val permisosDenegados = permisos.filterValues { !it }

            if (permisosDenegados.isNotEmpty()) {
                permisosDenegados.keys.forEach { permiso ->
                    if (!shouldShowRequestPermissionRationale(permiso)) {
                        // El usuario denegó permisos permanentemente
                        mostrarDialogoExplicativo()
                        return@registerForActivityResult
                    }
                }
            } else {
                iniciarServicioUbicacion()
            }
        }

    /**
     * Muestra un diálogo explicativo si el usuario deniega permisos permanentemente.
     */
    private fun mostrarDialogoExplicativo() {
        AlertDialog.Builder(this)
            .setTitle("Permisos Necesarios")
            .setMessage("Para usar esta app correctamente, debes conceder permisos de Cámara y Ubicación. Por favor, actívalos en la configuración.")
            .setPositiveButton("Abrir Configuración") { _, _ ->
                abrirConfiguracionApp()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Si el usuario deniega los permisos permanentemente, se le redirige a los ajustes de la app.
     */
    private fun abrirConfiguracionApp() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    /**
     * Iniciar el servicio de ubicación si los permisos están concedidos.
     */
    private fun iniciarServicioUbicacion() {
        val intent = Intent(this, LocationService::class.java)
        startForegroundService(intent)
    }

    private fun actions() {
        binding.btnLogin.setOnClickListener {
            sendLogin()
        }
    }

    private fun sendLogin() {
        showLoading()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        viewModel.login(email, password)
    }

    private fun showLoading() {
        binding.loginRlLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loginRlLoading.visibility = View.GONE
    }

    private fun navigateToMarcacion() {
        val intent = Intent(this, MarcacionActivity::class.java)
        startActivity(intent)
        finish() // Optional: Call finish() if you want to close the LoginActivity
    }

    private fun observerLogin() {
        viewModel.data.observe(this) { data ->
            when (data) {
                is StateLogin.Success -> {
                    hideLoading()
                    saveIdUser(this, data.info.user.id.toString())
                    navigateToMarcacion()
                }

                is StateLogin.Loading -> {
                    showLoading()
                }

                is StateLogin.Error -> {
                    hideLoading()
                }
            }
        }
    }
}
