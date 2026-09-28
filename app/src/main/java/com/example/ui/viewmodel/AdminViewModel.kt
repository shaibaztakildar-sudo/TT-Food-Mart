package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.*
import com.example.data.repository.FoodDeliveryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AdminScreen {
    DASHBOARD,
    SELLERS,
    RIDERS,
    CUSTOMERS,
    ORDERS,
    COUPONS,
    PAYOUTS,
    SETTINGS
}

class AdminViewModel(private val repository: FoodDeliveryRepository) : ViewModel() {

    private val _currentScreen = MutableStateFlow(AdminScreen.DASHBOARD)
    val currentScreen: StateFlow<AdminScreen> = _currentScreen.asStateFlow()

    val customers: StateFlow<List<UserEntity>> = repository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sellers: StateFlow<List<SellerProfileEntity>> = repository.getAllSellers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val riders: StateFlow<List<RiderProfileEntity>> = repository.getAllRiders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eligibleRiders: StateFlow<List<RiderProfileEntity>> = repository.getEligibleRiders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderEntity>> = repository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coupons: StateFlow<List<CouponEntity>> = repository.getAllCoupons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payouts: StateFlow<List<PayoutEntity>> = repository.getAllPayouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AdminSettingsEntity?> = repository.getAdminSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalRevenue: StateFlow<Double?> = repository.getTotalRevenue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun navigateTo(screen: AdminScreen) {
        _currentScreen.value = screen
    }

    fun approveSeller(sellerId: String) {
        viewModelScope.launch {
            repository.approveSeller(sellerId)
            _successMessage.value = "Seller approved successfully!"
        }
    }

    fun rejectSeller(sellerId: String) {
        viewModelScope.launch {
            repository.rejectSeller(sellerId)
            _successMessage.value = "Seller rejected"
        }
    }

    fun suspendSeller(sellerId: String) {
        viewModelScope.launch {
            repository.suspendSeller(sellerId)
            _successMessage.value = "Seller suspended"
        }
    }

    fun activateSeller(sellerId: String) {
        viewModelScope.launch {
            repository.activateSeller(sellerId)
            _successMessage.value = "Seller reactivated"
        }
    }

    fun approveRider(riderId: String) {
        viewModelScope.launch {
            repository.approveRider(riderId)
            _successMessage.value = "Rider approved!"
        }
    }

    fun rejectRider(riderId: String) {
        viewModelScope.launch {
            repository.rejectRider(riderId)
            _successMessage.value = "Rider rejected"
        }
    }

    fun toggleRiderStatus(riderId: String, currentStatus: String) {
        viewModelScope.launch {
            repository.toggleRiderActive(riderId, currentStatus)
        }
    }

    fun toggleCustomerBlock(customerId: String, currentStatus: String) {
        viewModelScope.launch {
            repository.toggleCustomerBlock(customerId, currentStatus)
        }
    }

    fun assignRiderToOrder(orderId: String, rider: RiderProfileEntity) {
        viewModelScope.launch {
            repository.assignRiderManual(orderId, rider)
            _successMessage.value = "Rider ${rider.name} assigned to Order #${orderId}"
        }
    }

    fun recordSellerPayout(seller: SellerProfileEntity, amount: Double, notes: String) {
        if (amount <= 0 || amount > seller.pendingPayout) {
            _errorMessage.value = "Invalid payout amount. Must be between ₹1 and ₹${seller.pendingPayout}"
            return
        }
        viewModelScope.launch {
            repository.recordSellerPayout(seller.sellerId, seller.restaurantName, amount, notes)
            _successMessage.value = "Payout of ₹$amount recorded for ${seller.restaurantName}"
        }
    }

    fun recordRiderPayout(rider: RiderProfileEntity, amount: Double, notes: String) {
        if (amount <= 0 || amount > rider.pendingPayout) {
            _errorMessage.value = "Invalid payout amount. Must be between ₹1 and ₹${rider.pendingPayout}"
            return
        }
        viewModelScope.launch {
            repository.recordRiderPayout(rider.riderId, rider.name, amount, notes)
            _successMessage.value = "Payout of ₹$amount recorded for ${rider.name}"
        }
    }

    fun addCoupon(
        code: String,
        title: String,
        discountPercent: Int,
        maxDiscount: Double,
        minOrder: Double
    ) {
        if (code.isBlank() || discountPercent <= 0) {
            _errorMessage.value = "Please enter valid coupon details"
            return
        }
        viewModelScope.launch {
            repository.addCoupon(
                CouponEntity(
                    code = code.trim().uppercase(),
                    title = title.trim(),
                    discountPercent = discountPercent,
                    maxDiscountAmount = maxDiscount,
                    minOrderAmount = minOrder,
                    isActive = true
                )
            )
            _successMessage.value = "Coupon ${code.uppercase()} created!"
        }
    }

    fun toggleCoupon(code: String, currentActive: Boolean) {
        viewModelScope.launch {
            repository.toggleCouponStatus(code, !currentActive)
        }
    }

    fun deleteCoupon(code: String) {
        viewModelScope.launch {
            repository.deleteCoupon(code)
            _successMessage.value = "Coupon deleted"
        }
    }

    fun updateSettings(newSettings: AdminSettingsEntity) {
        viewModelScope.launch {
            repository.updateAdminSettings(newSettings)
            _successMessage.value = "Platform & Delivery settings saved!"
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
