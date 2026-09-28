package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.OrderItemEntity
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.SellerProfileEntity
import com.example.data.repository.FoodDeliveryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SellerScreen {
    DASHBOARD,
    ORDERS,
    PRODUCTS,
    EARNINGS
}

class SellerViewModel(
    private val repository: FoodDeliveryRepository,
    val sellerId: String
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(SellerScreen.DASHBOARD)
    val currentScreen: StateFlow<SellerScreen> = _currentScreen.asStateFlow()

    val sellerProfile: StateFlow<SellerProfileEntity?> = repository.getSellerProfile(sellerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val sellerProducts: StateFlow<List<ProductEntity>> = repository.getProductsBySeller(sellerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sellerOrders: StateFlow<List<OrderEntity>> = repository.getSellerOrders(sellerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedOrderForSticker = MutableStateFlow<OrderEntity?>(null)
    val selectedOrderForSticker: StateFlow<OrderEntity?> = _selectedOrderForSticker.asStateFlow()

    private val _stickerItems = MutableStateFlow<List<OrderItemEntity>>(emptyList())
    val stickerItems: StateFlow<List<OrderItemEntity>> = _stickerItems.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun navigateTo(screen: SellerScreen) {
        _currentScreen.value = screen
    }

    fun addProduct(
        name: String,
        description: String,
        price: Double,
        category: String,
        isVeg: Boolean,
        imageUri: String?
    ) {
        val restName = sellerProfile.value?.restaurantName ?: "Restaurant"
        if (name.isBlank() || price <= 0) {
            _errorMessage.value = "Please provide valid product name and price"
            return
        }
        viewModelScope.launch {
            repository.addProduct(
                sellerId = sellerId,
                restaurantName = restName,
                name = name,
                description = description,
                price = price,
                category = category,
                isVeg = isVeg,
                imageUri = imageUri
            )
            _successMessage.value = "Product added successfully!"
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
            _successMessage.value = "Product updated!"
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            repository.deleteProduct(id)
            _successMessage.value = "Product deleted"
        }
    }

    fun toggleProductAvailability(id: String, current: Boolean) {
        viewModelScope.launch {
            repository.updateProductAvailability(id, !current)
        }
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            repository.sellerAcceptOrder(orderId)
            _successMessage.value = "Order #$orderId accepted!"
        }
    }

    fun rejectOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            repository.sellerRejectOrder(orderId, reason)
            _successMessage.value = "Order rejected"
        }
    }

    fun startPreparing(orderId: String) {
        viewModelScope.launch {
            repository.sellerStartPreparing(orderId)
            _successMessage.value = "Order in preparation"
        }
    }

    fun markReady(orderId: String) {
        viewModelScope.launch {
            repository.sellerMarkReady(orderId)
            _successMessage.value = "Order marked ready for delivery pickup!"
        }
    }

    fun openStickerDialog(order: OrderEntity) {
        viewModelScope.launch {
            val items = repository.getOrderItemsSync(order.id)
            _stickerItems.value = items
            _selectedOrderForSticker.value = order
        }
    }

    fun closeStickerDialog() {
        _selectedOrderForSticker.value = null
        _stickerItems.value = emptyList()
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
