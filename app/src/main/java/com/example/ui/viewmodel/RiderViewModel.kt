package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.RiderProfileEntity
import com.example.data.repository.FoodDeliveryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class RiderScreen {
    DASHBOARD,
    DELIVERIES,
    EARNINGS
}

class RiderViewModel(
    private val repository: FoodDeliveryRepository,
    val riderId: String
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(RiderScreen.DASHBOARD)
    val currentScreen: StateFlow<RiderScreen> = _currentScreen.asStateFlow()

    val riderProfile: StateFlow<RiderProfileEntity?> = repository.getRiderProfile(riderId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val riderOrders: StateFlow<List<OrderEntity>> = repository.getRiderOrders(riderId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedOrderForOtp = MutableStateFlow<OrderEntity?>(null)
    val selectedOrderForOtp: StateFlow<OrderEntity?> = _selectedOrderForOtp.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun navigateTo(screen: RiderScreen) {
        _currentScreen.value = screen
    }

    fun acceptOrder(order: OrderEntity) {
        val rider = riderProfile.value ?: return
        viewModelScope.launch {
            repository.riderAcceptOrder(order.id, rider.riderId, rider.name, rider.phone)
            _successMessage.value = "Delivery accepted for Order #${order.id}"
        }
    }

    fun pickUpOrder(orderId: String) {
        viewModelScope.launch {
            repository.riderPickUpOrder(orderId)
            _successMessage.value = "Order picked up from restaurant!"
        }
    }

    fun outForDelivery(orderId: String) {
        viewModelScope.launch {
            repository.riderOutForDelivery(orderId)
            _successMessage.value = "Order is now out for delivery to customer!"
        }
    }

    fun promptDeliveryOtp(order: OrderEntity) {
        _selectedOrderForOtp.value = order
    }

    fun dismissDeliveryOtp() {
        _selectedOrderForOtp.value = null
    }

    fun verifyDeliveryOtp(orderId: String, enteredOtp: String) {
        if (enteredOtp.isBlank() || enteredOtp.length != 4) {
            _errorMessage.value = "Please enter the 4-digit OTP provided by customer"
            return
        }

        viewModelScope.launch {
            val result = repository.verifyDeliveryOtpAndComplete(orderId, enteredOtp)
            result.onSuccess {
                _selectedOrderForOtp.value = null
                _successMessage.value = "Order #${orderId} delivered successfully! Earnings credited."
            }.onFailure { ex ->
                _errorMessage.value = ex.message ?: "Verification failed"
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
