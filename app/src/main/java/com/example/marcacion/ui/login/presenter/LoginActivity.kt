package com.example.marcacion.ui.login.presenter

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.marcacion.data.dto.dataSource.getIdUser
import com.example.marcacion.databinding.ActivityLoginBinding
import com.example.marcacion.data.dto.dataSource.saveIdUser
import com.example.marcacion.data.dto.model.StateLogin

class LoginActivity : AppCompatActivity() {

    private val viewModel by viewModels<LoginViewModel>()

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actions()
        observerLogin()
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

    private fun observerLogin() {
        viewModel.data.observe(this) { data ->
            when (data) {
                is StateLogin.Success -> {
                    hideLoading()
                    saveIdUser(this, data.info.user.id.toString())
                    Toast.makeText(this, getIdUser(this) + "TE AMO KAREN", Toast.LENGTH_SHORT).show()
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