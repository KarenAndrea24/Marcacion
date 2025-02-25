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
        when {
            email.isBlank() -> {
                _errorMessage.postValue(Constants.EMPTY_EMAIL_ERROR)
                _data.postValue(StateLogin.Error(Constants.EMPTY_EMAIL_ERROR))
                return
            }

            password.isBlank() -> {
                _errorMessage.postValue(Constants.EMPTY_PASSWORD_ERROR)
                _data.postValue(StateLogin.Error(Constants.EMPTY_PASSWORD_ERROR))
                return
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            _data.postValue(StateLogin.Loading)
            val response = repository.login(LoginRequest(email, password))

            if (response.isSuccessful) {
                response.body()?.let {
                    _data.postValue(StateLogin.Success(it))
                    _errorMessage.postValue(null)
                } ?: run {
                    _data.postValue(StateLogin.Error(Constants.GENERAL_ERROR))
                    _errorMessage.postValue(Constants.GENERAL_ERROR)
                }
            } else {
                _errorMessage.postValue(
                    when (response.code()) {
                        400 -> Constants.INVALID_CREDENTIALS_ERROR
                        401 -> Constants.UNAUTHORIZED_ERROR
                        else -> Constants.NETWORK_ERROR
                    }
                )
                _data.postValue(StateLogin.Error(_errorMessage.value ?: Constants.GENERAL_ERROR))
            }
        }
    }
}
