package com.example.brife.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.repository.AuthRepository

class LoginViewModelFactory(
    private val repository: AuthRepository,
    private val authLocalStorage: AuthLocalStorage
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(repository, authLocalStorage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}