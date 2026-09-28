package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.*
import com.example.data.repository.FoodDeliveryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class CustomerScreen {
    HOME,
    SEARCH,
    CART,
    CHECKOUT,
    ORDER_STATUS,
    ORDERS_HISTORY,
    PROFILE,
    ADDRESSES,
    RESTAURANT_DETAIL
}

class CustomerViewModel(
    private val repository: FoodDeliveryRepository,
    private val customerId: String
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(CustomerScreen.HOME)
    val currentScreen: StateFlow<CustomerScreen> = _currentScreen.asStateFlow()

    // Real data only from database!
    val restaurants: StateFlow<List<SellerProfileEntity>> = repository.getApprovedSellers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAvailableProducts: StateFlow<List<ProductEntity>> = repository.getAvailableProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerOrders: StateFlow<List<OrderEntity>> = repository.getCustomerOrders(customerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val addresses: StateFlow<List<AddressEntity>> = repository.getCustomerAddresses(customerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCoupons: StateFlow<List<CouponEntity>> = repository.getActiveCoupons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminSettings: StateFlow<AdminSettingsEntity?> = repository.getAdminSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Cart
    private val _cart = MutableStateFlow<Map<String, FoodDeliveryRepository.CartItem>>(emptyMap())
    val cart: StateFlow<Map<String, FoodDeliveryRepository.CartItem>> = _cart.asStateFlow()

    private val _cartRestaurant = MutableStateFlow<SellerProfileEntity?>(null)
    val cartRestaurant: StateFlow<SellerProfileEntity?> = _cartRestaurant.asStateFlow()

    private val _selectedAddress = MutableStateFlow<AddressEntity?>(null)
    val selectedAddress: StateFlow<AddressEntity?> = _selectedAddress.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<CouponEntity?>(null)
    val appliedCoupon: StateFlow<CouponEntity?> = _appliedCoupon.asStateFlow()

    private val _discount = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount.asStateFlow()

    private val _selectedRestaurant = MutableStateFlow<SellerProfileEntity?>(null)
    val selectedRestaurant: StateFlow<SellerProfileEntity?> = _selectedRestaurant.asStateFlow()

    private val _activeOrder = MutableStateFlow<OrderEntity?>(null)
    val activeOrder: StateFlow<OrderEntity?> = _activeOrder.asStateFlow()

    private val _orderItems = MutableStateFlow<List<OrderItemEntity>>(emptyList())
    val orderItems: StateFlow<List<OrderItemEntity>> = _orderItems.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<ProductEntity>>(emptyList())
    val searchResults: StateFlow<List<ProductEntity>> = _searchResults.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        // Auto select default address when available
        viewModelScope.launch {
            addresses.collect { list ->
                if (_selectedAddress.value == null && list.isNotEmpty()) {
                    _selectedAddress.value = list.firstOrNull { it.isDefault } ?: list.first()
                }
            }
        }
    }

    fun navigateTo(screen: CustomerScreen) {
        _errorMessage.value = null
        _successMessage.value = null
        _currentScreen.value = screen
    }

    fun selectRestaurant(restaurant: SellerProfileEntity) {
        _selectedRestaurant.value = restaurant
        _currentScreen.value = CustomerScreen.RESTAURANT_DETAIL
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        } else {
            viewModelScope.launch {
                repository.searchProducts(query).collect {
                    _searchResults.value = it
                }
            }
        }
    }

    fun addToCart(product: ProductEntity, restaurant: SellerProfileEntity) {
        val currentCart = _cart.value.toMutableMap()
        val currentRest = _cartRestaurant.value

        if (currentRest != null && currentRest.sellerId != product.sellerId) {
            // New restaurant: reset cart or update restaurant
            currentCart.clear()
        }
        _cartRestaurant.value = restaurant

        val currentItem = currentCart[product.id]
        if (currentItem != null) {
            currentCart[product.id] = currentItem.copy(quantity = currentItem.quantity + 1)
        } else {
            currentCart[product.id] = FoodDeliveryRepository.CartItem(product, 1)
        }
        _cart.value = currentCart
        recalculateCouponDiscount()
    }

    fun removeFromCart(productId: String) {
        val currentCart = _cart.value.toMutableMap()
        val currentItem = currentCart[productId] ?: return
        if (currentItem.quantity > 1) {
            currentCart[productId] = currentItem.copy(quantity = currentItem.quantity - 1)
        } else {
            currentCart.remove(productId)
        }
        if (currentCart.isEmpty()) {
            _cartRestaurant.value = null
            _appliedCoupon.value = null
            _discount.value = 0.0
        }
        _cart.value = currentCart
        recalculateCouponDiscount()
    }

    fun clearCart() {
        _cart.value = emptyMap()
        _cartRestaurant.value = null
        _appliedCoupon.value = null
        _discount.value = 0.0
    }

    fun applyCoupon(code: String) {
        val subtotal = getSubtotal()
        viewModelScope.launch {
            val result = repository.validateCoupon(code, subtotal)
            result.onSuccess { discountVal ->
                val coupon = repository.getActiveCoupons().firstOrNull()?.find { it.code.equals(code, true) }
                _appliedCoupon.value = coupon
                _discount.value = discountVal
                _successMessage.value = "Coupon applied! You saved ₹${discountVal.toInt()}"
            }.onFailure { ex ->
                _errorMessage.value = ex.message ?: "Invalid coupon"
            }
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
        _discount.value = 0.0
    }

    private fun recalculateCouponDiscount() {
        val coupon = _appliedCoupon.value ?: return
        val subtotal = getSubtotal()
        if (subtotal < coupon.minOrderAmount) {
            _appliedCoupon.value = null
            _discount.value = 0.0
        } else {
            val calc = (subtotal * coupon.discountPercent) / 100.0
            _discount.value = minOf(calc, coupon.maxDiscountAmount)
        }
    }

    fun getSubtotal(): Double {
        return _cart.value.values.sumOf { it.product.price * it.quantity }
    }

    fun getDeliveryFee(): Double {
        val subtotal = getSubtotal()
        val settings = adminSettings.value
        val threshold = settings?.freeDeliveryThreshold ?: 499.0
        return if (subtotal >= threshold || subtotal == 0.0) 0.0 else (settings?.fixedDeliveryFee ?: 30.0)
    }

    fun getTotalAmount(): Double {
        val total = (getSubtotal() + getDeliveryFee()) - _discount.value
        return maxOf(0.0, total)
    }

    fun selectAddress(address: AddressEntity) {
        _selectedAddress.value = address
    }

    fun addNewAddress(
        title: String,
        receiverName: String,
        receiverPhone: String,
        streetAddress: String,
        landmark: String,
        area: String,
        city: String,
        pincode: String,
        isDefault: Boolean
    ) {
        viewModelScope.launch {
            val isAllowed = repository.validateServiceArea(pincode)
            if (!isAllowed) {
                val settings = adminSettings.value
                _errorMessage.value = "We currently deliver only in ${settings?.serviceAreaName ?: "Ajara, Maharashtra"} (${settings?.servicePincodes})"
                return@launch
            }

            val newAddress = AddressEntity(
                id = "addr_${java.util.UUID.randomUUID().toString().take(8)}",
                customerId = customerId,
                title = title,
                receiverName = receiverName,
                receiverPhone = receiverPhone,
                streetAddress = streetAddress,
                landmark = landmark,
                area = area,
                city = city,
                pincode = pincode,
                isDefault = isDefault
            )
            repository.addAddress(newAddress)
            _selectedAddress.value = newAddress
            _successMessage.value = "Address saved"
            _currentScreen.value = CustomerScreen.CHECKOUT
        }
    }

    fun deleteAddress(id: String) {
        viewModelScope.launch {
            repository.deleteAddress(id)
            if (_selectedAddress.value?.id == id) {
                _selectedAddress.value = null
            }
            _successMessage.value = "Address deleted"
        }
    }

    fun placeOrder(
        customer: UserEntity,
        paymentMethod: String,
        instructions: String
    ) {
        val address = _selectedAddress.value
        if (address == null) {
            _errorMessage.value = "Please select or add a delivery address"
            return
        }

        val restaurant = _cartRestaurant.value
        if (restaurant == null) {
            _errorMessage.value = "Cart is empty"
            return
        }

        val items = _cart.value.values.toList()
        if (items.isEmpty()) {
            _errorMessage.value = "Cart is empty"
            return
        }

        viewModelScope.launch {
            val result = repository.placeOrder(
                customer = customer,
                restaurant = restaurant,
                items = items,
                deliveryAddress = address,
                paymentMethod = paymentMethod,
                couponCode = _appliedCoupon.value?.code,
                discount = _discount.value,
                customerInstructions = instructions
            )

            result.onSuccess { order ->
                clearCart()
                _activeOrder.value = order
                loadOrderItems(order.id)
                _currentScreen.value = CustomerScreen.ORDER_STATUS
            }.onFailure { ex ->
                _errorMessage.value = ex.message ?: "Failed to place order"
            }
        }
    }

    fun viewOrderDetails(order: OrderEntity) {
        _activeOrder.value = order
        loadOrderItems(order.id)
        _currentScreen.value = CustomerScreen.ORDER_STATUS
    }

    fun loadOrderItems(orderId: String) {
        viewModelScope.launch {
            repository.getOrderItems(orderId).collect {
                _orderItems.value = it
            }
        }
    }

    fun submitRating(orderId: String, rating: Int, reviewText: String) {
        viewModelScope.launch {
            repository.submitOrderReview(orderId, rating, reviewText)
            _successMessage.value = "Thank you for rating your food!"
        }
    }

    fun cancelOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            repository.cancelOrder(orderId, "Cancelled by Customer: $reason")
            _successMessage.value = "Order cancelled"
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }
}
