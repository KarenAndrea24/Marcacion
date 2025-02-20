package com.example.marcacion.ui.login.presenter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.marcacion.data.dto.model.StateLogin
import com.example.marcacion.data.dto.request.LoginRequest
import com.example.marcacion.data.repository.UserRepository
import com.example.marcacion.data.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    private val _data = MutableLiveData<StateLogin>()
    val data: LiveData<StateLogin> = _data

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        _errorMessage.postValue(null)
    }

    fun login(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _data.postValue(StateLogin.Loading)
            val response = repository.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let {
                    _data.postValue(StateLogin.Success(it))
                    _errorMessage.postValue(null)
                } ?: {
                    _data.postValue(StateLogin.Error(Constants.LOGIN_FAILED))
                    _errorMessage.postValue(Constants.LOGIN_FAILED)
                }
            } else {
                _data.postValue(StateLogin.Error(Constants.NETWORK_ERROR))
                _errorMessage.postValue(Constants.LOGIN_FAILED)
            }
        }
    }
}
