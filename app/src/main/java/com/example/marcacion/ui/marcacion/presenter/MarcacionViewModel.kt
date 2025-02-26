package com.example.marcacion.ui.marcacion.presenter

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.marcacion.data.dto.model.StateMarcacion
import com.example.marcacion.data.dto.model.StateSearchDni
import com.example.marcacion.data.dto.request.MarcacionRequest
import com.example.marcacion.data.repository.UserRepository
import com.example.marcacion.data.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class MarcacionViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    private val _data = MutableLiveData<StateSearchDni>()
    val data: LiveData<StateSearchDni> = _data

    private val _data_marcacion = MutableLiveData<StateMarcacion>()
    val data_marcacion: LiveData<StateMarcacion> = _data_marcacion

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun obtenerNombrePorDNI(dni: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _data.postValue(StateSearchDni.Loading)
            val response = repository.obtenerNombrePorDNI(dni)
            if (response.isSuccessful) {
                response.body()?.let {
                    _data.postValue(StateSearchDni.Success(it))
                    _errorMessage.postValue(null)
                } ?: {
                    _data.postValue(StateSearchDni.Error(Constants.SEARCH_DNI_FAILED))
                    _errorMessage.postValue(Constants.SEARCH_DNI_FAILED)
                }
            } else {
                _data.postValue(StateSearchDni.Error(Constants.NETWORK_ERROR))
                _errorMessage.postValue(Constants.SEARCH_DNI_FAILED)
            }
        }
    }

    fun observerCrearMarcacion(marcacionRequest: MarcacionRequest) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _data_marcacion.postValue(StateMarcacion.Loading)
                val response = repository.observerCrearMarcacion(marcacionRequest)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _data_marcacion.postValue(StateMarcacion.Success(it))
                        _errorMessage.postValue(null)
                    } ?: run {
                        _data_marcacion.postValue(StateMarcacion.Error(Constants.MARCACION_FAILED))
                        _errorMessage.postValue(Constants.MARCACION_FAILED)
                    }
                } else {
                    // ⚠️ Manejar errores cuando la API responde con un 422
                    val errorBody = response.errorBody()?.string()
                    Log.e("MarcacionViewModel", "Error en la API: Código ${response.code()} - $errorBody")

                    if (response.code() == 422 && errorBody != null) {
                        val errorMessage = extraerMensajeDeError(errorBody)
                        _data_marcacion.postValue(StateMarcacion.Error(errorMessage))
                        _errorMessage.postValue(errorMessage)
                    } else {
                        _data_marcacion.postValue(StateMarcacion.Error(Constants.NETWORK_ERROR))
                        _errorMessage.postValue(Constants.MARCACION_FAILED)
                    }
                }
            } catch (e: Exception) {
                _data_marcacion.postValue(StateMarcacion.Error(e.message.toString()))
                _errorMessage.postValue(e.message)
            }
        }
    }

    private fun extraerMensajeDeError(errorBody: String): String {
        return try {
            val jsonObject = JSONObject(errorBody)
            if (jsonObject.has("message")) {
                jsonObject.getString("message") // Tomar el mensaje principal
            } else {
                "Error desconocido en la marcación"
            }
        } catch (e: JSONException) {
            "Error procesando la respuesta del servidor"
        }
    }

}
