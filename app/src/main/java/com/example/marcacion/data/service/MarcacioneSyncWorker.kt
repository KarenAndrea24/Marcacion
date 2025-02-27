package com.example.marcacion.data.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.marcacion.data.dto.dataSource.getIdUser
import com.example.marcacion.data.dto.request.MarcacionRequest
import com.example.marcacion.data.repository.UserRepository
import com.example.marcacion.database.MarcacionDatabase
import com.example.marcacion.database.dao.MarcacionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MarcacioneSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    // Acceso a la base de datos
    private val dao: MarcacionDao

    // Repositorio o servicio para enviar marcaciones al servidor
    private val repository: UserRepository

    init {
        // Inicializa la base de datos y el DAO
        val db = MarcacionDatabase.getDatabase(context)
        dao = db.marcacionDao()

        // Suponiendo que tienes un Repositorio para hacer llamadas a tu API
        // Podrías inyectarlo, o crearlo directamente aquí
        repository = UserRepository()
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("WorkManagerTest", "Iniciando sincronización de marcaciones...")

                // 1. Buscar todas las marcaciones con estado=0 (pendientes)
                val pendientes = dao.obtenerPendientes() // SELECT * FROM marcaciones WHERE estado=0
                Log.d("WorkManagerTest", "Marcaciones pendientes encontradas: ${pendientes.size}")

                // 2. Intentar sincronizar cada una con el servidor
                pendientes.forEach { marcacion ->
                    val marcacionRequest = getIdUser(applicationContext)?.let {
                        MarcacionRequest(
                            idUser = marcacion.idUserMarcado,
                            idMarcacionUser = it,
                            tipoMarcacion = marcacion.tipoMarcacion,
                            fechaHora = marcacion.fechaHora,
                            foto = "", // Ajusta esto según sea necesario
                            latitud = marcacion.latitud.toString(),
                            longitud = marcacion.longitud.toString()
                        )
                    }
                    if (marcacionRequest != null) {
                        val response = repository.observerCrearMarcacion(marcacionRequest)
                        // Si fue exitosa la sincronización, actualiza estado=1
                        if (response.isSuccessful) {
                            dao.actualizarEstado(marcacion.id) // Cambia estado=1
                            Log.d("WorkManagerTest", "Marcación sincronizada con éxito: ${marcacion.id}")
                        } else {
                            Log.e("WorkManagerTest", "Error al sincronizar la marcación ${marcacion.id}")
                        }
                    }
                }

                // 3. Si todo va bien, retornamos success
                Log.d("WorkManagerTest", "Finalizó la sincronización")
                Result.success()

            } catch (e: Exception) {
                // Si algo falla, WorkManager puede reintentar
                Log.e("WorkManagerTest", "Error en WorkManager: ${e.message}")
                Result.retry()
            }
        }
    }
}