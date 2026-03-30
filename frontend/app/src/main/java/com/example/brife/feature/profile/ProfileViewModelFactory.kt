package com.example.brife.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.brife.data.local.OnboardingLocalStorage
import com.example.brife.data.repository.UserRepository

class ProfileViewModelFactory(
    private val userRepository: UserRepository,
    private val onboardingLocalStorage: OnboardingLocalStorage
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(userRepository, onboardingLocalStorage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
