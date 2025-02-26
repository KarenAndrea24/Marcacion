package com.example.marcacion.data.service

import android.content.Context
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
                // 1. Buscar todas las marcaciones con estado=0 (pendientes)
                val pendientes = dao.obtenerPendientes() // SELECT * FROM marcaciones WHERE estado=0

                // 2. Intentar sincronizar cada una con el servidor
                pendientes.forEach { marcacion ->
                    val marcacionRequest = getIdUser(applicationContext)?.let {
                        MarcacionRequest(
//                            idUser = it,
//                            idMarcacionUser = marcacion.id.toString(),
                            idUser = marcacion.idUserMarcado,
                            idMarcacionUser = it,
                            tipoMarcacion = "entrada", // Ajusta esto según sea necesario
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
                            // Actualiza en la BD
                            dao.actualizarEstado(marcacion.id) // Por ejemplo, un método que ponga estado=1
                        }
                    }
                }

                // 3. Si todo va bien, retornamos success
                Result.success()

            } catch (e: Exception) {
                // Si algo falla, WorkManager puede reintentar
                Result.retry()
            }
        }
    }
}