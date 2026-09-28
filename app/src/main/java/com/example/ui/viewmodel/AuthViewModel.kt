package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.UserEntity
import com.example.data.repository.FoodDeliveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthScreenState {
    object RoleSelect : AuthScreenState()
    data class Login(val role: String) : AuthScreenState()
    object CustomerSignup : AuthScreenState()
    object SellerSignup : AuthScreenState()
    object RiderSignup : AuthScreenState()
    data class OtpVerification(val phone: String, val generatedOtp: String, val onVerified: () -> Unit) : AuthScreenState()
    object ForgotPassword : AuthScreenState()
    data class Authenticated(val user: UserEntity) : AuthScreenState()
}

class AuthViewModel(private val repository: FoodDeliveryRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthScreenState>(AuthScreenState.RoleSelect)
    val authState: StateFlow<AuthScreenState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun navigateTo(state: AuthScreenState) {
        _errorMessage.value = null
        _authState.value = state
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun login(identifier: String, password: String, role: String) {
        if (identifier.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please enter both credentials"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.login(identifier, password, role)
            _isLoading.value = false

            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = AuthScreenState.Authenticated(user)
            }.onFailure { ex ->
                _errorMessage.value = ex.message ?: "Login failed"
            }
        }
    }

    fun registerCustomer(name: String, email: String, phone: String, password: String) {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill in all customer details"
            return
        }
        if (phone.length < 10) {
            _errorMessage.value = "Please enter a valid 10-digit mobile number"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Generate realistic OTP for mobile verification
            val generatedOtp = (1000..9999).random().toString()
            _isLoading.value = false

            _authState.value = AuthScreenState.OtpVerification(
                phone = phone,
                generatedOtp = generatedOtp,
                onVerified = {
                    viewModelScope.launch {
                        _isLoading.value = true
                        val result = repository.registerCustomer(name, email, phone, password)
                        _isLoading.value = false
                        result.onSuccess { user ->
                            _currentUser.value = user
                            _authState.value = AuthScreenState.Authenticated(user)
                        }.onFailure { ex ->
                            _errorMessage.value = ex.message ?: "Registration failed"
                            _authState.value = AuthScreenState.CustomerSignup
                        }
                    }
                }
            )
        }
    }

    fun registerSeller(
        restaurantName: String,
        ownerName: String,
        phone: String,
        email: String,
        password: String,
        address: String,
        panNumber: String,
        bankAccount: String,
        bankIfsc: String,
        bankName: String,
        documentProofUri: String?,
        imageUri: String?
    ) {
        if (restaurantName.isBlank() || ownerName.isBlank() || phone.isBlank() || email.isBlank() ||
            password.isBlank() || address.isBlank() || panNumber.isBlank() || bankAccount.isBlank() || bankIfsc.isBlank()
        ) {
            _errorMessage.value = "Please fill in all mandatory seller & banking details"
            return
        }

        viewModelScope.launch {
            val generatedOtp = (1000..9999).random().toString()
            _authState.value = AuthScreenState.OtpVerification(
                phone = phone,
                generatedOtp = generatedOtp,
                onVerified = {
                    viewModelScope.launch {
                        _isLoading.value = true
                        val result = repository.registerSeller(
                            restaurantName, ownerName, phone, email, password,
                            address, panNumber, bankAccount, bankIfsc, bankName,
                            documentProofUri, imageUri
                        )
                        _isLoading.value = false
                        result.onSuccess {
                            _errorMessage.value = "Application submitted! Once Admin approves your restaurant, you can log in."
                            _authState.value = AuthScreenState.Login("SELLER")
                        }.onFailure { ex ->
                            _errorMessage.value = ex.message ?: "Seller registration failed"
                            _authState.value = AuthScreenState.SellerSignup
                        }
                    }
                }
            )
        }
    }

    fun registerRider(
        name: String,
        phone: String,
        email: String,
        password: String,
        vehicleNumber: String,
        licenseUri: String?
    ) {
        if (name.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank() || vehicleNumber.isBlank()) {
            _errorMessage.value = "Please fill in all rider details"
            return
        }

        viewModelScope.launch {
            val generatedOtp = (1000..9999).random().toString()
            _authState.value = AuthScreenState.OtpVerification(
                phone = phone,
                generatedOtp = generatedOtp,
                onVerified = {
                    viewModelScope.launch {
                        _isLoading.value = true
                        val result = repository.registerRider(
                            name, phone, email, password, vehicleNumber, licenseUri
                        )
                        _isLoading.value = false
                        result.onSuccess {
                            _errorMessage.value = "Rider application submitted! Awaiting Admin approval."
                            _authState.value = AuthScreenState.Login("RIDER")
                        }.onFailure { ex ->
                            _errorMessage.value = ex.message ?: "Rider registration failed"
                            _authState.value = AuthScreenState.RiderSignup
                        }
                    }
                }
            )
        }
    }

    fun updateProfile(name: String, email: String, phone: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateProfile(user.id, name, email, phone)
            _currentUser.value = user.copy(name = name, email = email, phone = phone)
        }
    }

    fun updateProfilePhoto(photoUri: String?) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateProfilePhoto(user.id, photoUri)
            _currentUser.value = user.copy(profilePhotoUri = photoUri)
        }
    }

    fun logout() {
        _currentUser.value = null
        _authState.value = AuthScreenState.RoleSelect
    }
}
