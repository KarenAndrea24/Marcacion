package com.example.marcacion.ui.marcacion.presenter

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
                    _data_marcacion.postValue(StateMarcacion.Error(Constants.NETWORK_ERROR))
                    _errorMessage.postValue(Constants.MARCACION_FAILED)
                }
            } catch (e: Exception) {
                _data_marcacion.postValue(StateMarcacion.Error(e.message.toString()))
                _errorMessage.postValue(e.message)
            }
        }
    }
}
