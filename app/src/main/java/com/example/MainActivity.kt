package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.FoodDeliveryRepository
import com.example.ui.admin.*
import com.example.ui.auth.*
import com.example.ui.common.ThermalStickerDialog
import com.example.ui.customer.*
import com.example.ui.rider.RiderDashboardScreen
import com.example.ui.seller.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = FoodDeliveryRepository(db)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TTFoodDeliveryApp(repository = repository)
                }
            }
        }
    }
}

@Composable
fun TTFoodDeliveryApp(repository: FoodDeliveryRepository) {
    val authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(repository))
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val authError by authViewModel.errorMessage.collectAsStateWithLifecycle()
    val authLoading by authViewModel.isLoading.collectAsStateWithLifecycle()

    when (val state = authState) {
        is AuthScreenState.RoleSelect -> {
            RoleSelectionScreen(
                onSelectRole = { role ->
                    authViewModel.navigateTo(AuthScreenState.Login(role))
                }
            )
        }
        is AuthScreenState.Login -> {
            LoginScreen(
                role = state.role,
                onLoginClick = { id, pass -> authViewModel.login(id, pass, state.role) },
                onSignupClick = {
                    when (state.role) {
                        "CUSTOMER" -> authViewModel.navigateTo(AuthScreenState.CustomerSignup)
                        "SELLER" -> authViewModel.navigateTo(AuthScreenState.SellerSignup)
                        "RIDER" -> authViewModel.navigateTo(AuthScreenState.RiderSignup)
                    }
                },
                onBackClick = { authViewModel.navigateTo(AuthScreenState.RoleSelect) },
                isLoading = authLoading,
                errorMessage = authError
            )
        }
        is AuthScreenState.CustomerSignup -> {
            CustomerSignupScreen(
                onSignupSubmit = { name, email, phone, pass ->
                    authViewModel.registerCustomer(name, email, phone, pass)
                },
                onBackClick = { authViewModel.navigateTo(AuthScreenState.Login("CUSTOMER")) },
                isLoading = authLoading,
                errorMessage = authError
            )
        }
        is AuthScreenState.SellerSignup -> {
            SellerSignupScreen(
                onSellerSubmit = { restName, owner, phone, email, pass, addr, pan, bankAcc, ifsc, bankName, docProof, img ->
                    authViewModel.registerSeller(
                        restName, owner, phone, email, pass, addr, pan,
                        bankAcc, ifsc, bankName, docProof, img
                    )
                },
                onBackClick = { authViewModel.navigateTo(AuthScreenState.Login("SELLER")) },
                isLoading = authLoading,
                errorMessage = authError
            )
        }
        is AuthScreenState.RiderSignup -> {
            RiderSignupScreen(
                onRiderSubmit = { name, phone, email, pass, vehicle, lic ->
                    authViewModel.registerRider(name, phone, email, pass, vehicle, lic)
                },
                onBackClick = { authViewModel.navigateTo(AuthScreenState.Login("RIDER")) },
                isLoading = authLoading,
                errorMessage = authError
            )
        }
        is AuthScreenState.OtpVerification -> {
            OtpVerificationScreen(
                phone = state.phone,
                generatedOtp = state.generatedOtp,
                onVerifySuccess = { state.onVerified() },
                onBackClick = { authViewModel.navigateTo(AuthScreenState.RoleSelect) }
            )
        }
        is AuthScreenState.ForgotPassword -> {
            RoleSelectionScreen(onSelectRole = { authViewModel.navigateTo(AuthScreenState.Login(it)) })
        }
        is AuthScreenState.Authenticated -> {
            val user = state.user
            when (user.role) {
                "CUSTOMER" -> {
                    CustomerRootScreen(
                        repository = repository,
                        user = user,
                        onLogout = { authViewModel.logout() },
                        onUpdateProfile = { n, e, p -> authViewModel.updateProfile(n, e, p) },
                        onUpdatePhoto = { authViewModel.updateProfilePhoto(it) }
                    )
                }
                "SELLER" -> {
                    SellerRootScreen(
                        repository = repository,
                        sellerId = user.id,
                        onLogout = { authViewModel.logout() }
                    )
                }
                "RIDER" -> {
                    RiderRootScreen(
                        repository = repository,
                        riderId = user.id,
                        onLogout = { authViewModel.logout() }
                    )
                }
                "ADMIN" -> {
                    AdminRootScreen(
                        repository = repository,
                        onLogout = { authViewModel.logout() }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerRootScreen(
    repository: FoodDeliveryRepository,
    user: com.example.data.local.entities.UserEntity,
    onLogout: () -> Unit,
    onUpdateProfile: (String, String, String) -> Unit,
    onUpdatePhoto: (String?) -> Unit
) {
    val customerViewModel: CustomerViewModel = viewModel(
        key = "cust_${user.id}",
        factory = ViewModelFactory(repository, user.id)
    )

    val currentScreen by customerViewModel.currentScreen.collectAsStateWithLifecycle()
    val restaurants by customerViewModel.restaurants.collectAsStateWithLifecycle()
    val products by customerViewModel.allAvailableProducts.collectAsStateWithLifecycle()
    val coupons by customerViewModel.activeCoupons.collectAsStateWithLifecycle()
    val cart by customerViewModel.cart.collectAsStateWithLifecycle()
    val cartRestaurant by customerViewModel.cartRestaurant.collectAsStateWithLifecycle()
    val addresses by customerViewModel.addresses.collectAsStateWithLifecycle()
    val selectedAddress by customerViewModel.selectedAddress.collectAsStateWithLifecycle()
    val appliedCoupon by customerViewModel.appliedCoupon.collectAsStateWithLifecycle()
    val discount by customerViewModel.discount.collectAsStateWithLifecycle()
    val customerOrders by customerViewModel.customerOrders.collectAsStateWithLifecycle()
    val activeOrder by customerViewModel.activeOrder.collectAsStateWithLifecycle()
    val orderItems by customerViewModel.orderItems.collectAsStateWithLifecycle()
    val searchQuery by customerViewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by customerViewModel.searchResults.collectAsStateWithLifecycle()
    val errorMessage by customerViewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by customerViewModel.successMessage.collectAsStateWithLifecycle()
    val adminSettings by customerViewModel.adminSettings.collectAsStateWithLifecycle()

    val selectedRestaurant by customerViewModel.selectedRestaurant.collectAsStateWithLifecycle()

    val cartCount = cart.values.sumOf { it.quantity }
    val cartTotal = customerViewModel.getTotalAmount()
    val subtotal = customerViewModel.getSubtotal()
    val deliveryFee = customerViewModel.getDeliveryFee()

    when (currentScreen) {
        CustomerScreen.HOME -> {
            CustomerHomeScreen(
                restaurants = restaurants,
                products = products,
                coupons = coupons,
                cartCount = cartCount,
                cartTotal = cartTotal,
                onRestaurantClick = { rest -> customerViewModel.selectRestaurant(rest) },
                onAddToCart = { prod, rest -> customerViewModel.addToCart(prod, rest) },
                onCartClick = { customerViewModel.navigateTo(CustomerScreen.CART) },
                onSearchClick = { customerViewModel.navigateTo(CustomerScreen.SEARCH) },
                onProfileClick = { customerViewModel.navigateTo(CustomerScreen.PROFILE) },
                onOrdersClick = { customerViewModel.navigateTo(CustomerScreen.ORDERS_HISTORY) }
            )
        }
        CustomerScreen.RESTAURANT_DETAIL -> {
            selectedRestaurant?.let { rest ->
                CustomerRestaurantScreen(
                    restaurant = rest,
                    products = products,
                    cart = cart,
                    onAddToCart = { prod -> customerViewModel.addToCart(prod, rest) },
                    onRemoveFromCart = { prodId -> customerViewModel.removeFromCart(prodId) },
                    onViewCartClick = { customerViewModel.navigateTo(CustomerScreen.CART) },
                    onBackClick = { customerViewModel.navigateTo(CustomerScreen.HOME) }
                )
            } ?: run { customerViewModel.navigateTo(CustomerScreen.HOME) }
        }
        CustomerScreen.CART -> {
            CustomerCartScreen(
                cart = cart,
                restaurant = cartRestaurant,
                addresses = addresses,
                selectedAddress = selectedAddress,
                appliedCoupon = appliedCoupon,
                discount = discount,
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                totalAmount = cartTotal,
                onAddToCart = { prod -> cartRestaurant?.let { customerViewModel.addToCart(prod, it) } },
                onRemoveFromCart = { prodId -> customerViewModel.removeFromCart(prodId) },
                onApplyCoupon = { code -> customerViewModel.applyCoupon(code) },
                onRemoveCoupon = { customerViewModel.removeCoupon() },
                onSelectAddress = { addr -> customerViewModel.selectAddress(addr) },
                onAddNewAddressClick = { customerViewModel.navigateTo(CustomerScreen.CHECKOUT) },
                onProceedToCheckout = { customerViewModel.navigateTo(CustomerScreen.CHECKOUT) },
                onBackClick = { customerViewModel.navigateTo(CustomerScreen.HOME) },
                errorMessage = errorMessage,
                successMessage = successMessage
            )
        }
        CustomerScreen.CHECKOUT -> {
            CustomerCheckoutScreen(
                customer = user,
                restaurant = cartRestaurant,
                cart = cart,
                addresses = addresses,
                selectedAddress = selectedAddress,
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                discount = discount,
                totalAmount = cartTotal,
                adminSettings = adminSettings,
                onSelectAddress = { addr -> customerViewModel.selectAddress(addr) },
                onAddNewAddress = { title, n, p, st, lm, ar, c, pin, def ->
                    customerViewModel.addNewAddress(title, n, p, st, lm, ar, c, pin, def)
                },
                onPlaceOrder = { cust, payMethod, instructions ->
                    customerViewModel.placeOrder(cust, payMethod, instructions)
                },
                onBackClick = { customerViewModel.navigateTo(CustomerScreen.CART) },
                errorMessage = errorMessage
            )
        }
        CustomerScreen.ORDER_STATUS -> {
            activeOrder?.let { order ->
                CustomerOrderStatusScreen(
                    order = order,
                    items = orderItems,
                    onSubmitReview = { oId, stars, review ->
                        customerViewModel.submitRating(oId, stars, review)
                    },
                    onCancelOrder = { oId, reason ->
                        customerViewModel.cancelOrder(oId, reason)
                    },
                    onBackClick = { customerViewModel.navigateTo(CustomerScreen.ORDERS_HISTORY) }
                )
            } ?: run { customerViewModel.navigateTo(CustomerScreen.ORDERS_HISTORY) }
        }
        CustomerScreen.ORDERS_HISTORY -> {
            CustomerOrdersHistoryScreen(
                orders = customerOrders,
                onOrderClick = { order -> customerViewModel.viewOrderDetails(order) },
                onBackClick = { customerViewModel.navigateTo(CustomerScreen.HOME) }
            )
        }
        CustomerScreen.PROFILE -> {
            CustomerProfileScreen(
                user = user,
                addresses = addresses,
                onUpdateProfile = onUpdateProfile,
                onUpdatePhoto = onUpdatePhoto,
                onDeleteAddress = { id ->
                    customerViewModel.deleteAddress(id)
                },
                onLogoutClick = onLogout,
                onBackClick = { customerViewModel.navigateTo(CustomerScreen.HOME) }
            )
        }
        CustomerScreen.SEARCH -> {
            CustomerSearchScreen(
                searchQuery = searchQuery,
                searchResults = searchResults,
                restaurants = restaurants,
                onQueryChange = { customerViewModel.setSearchQuery(it) },
                onAddToCart = { prod, rest -> customerViewModel.addToCart(prod, rest) },
                onBackClick = { customerViewModel.navigateTo(CustomerScreen.HOME) }
            )
        }
        CustomerScreen.ADDRESSES -> {
            customerViewModel.navigateTo(CustomerScreen.PROFILE)
        }
    }
}

@Composable
private fun SellerRootScreen(
    repository: FoodDeliveryRepository,
    sellerId: String,
    onLogout: () -> Unit
) {
    val sellerViewModel: SellerViewModel = viewModel(
        key = "seller_$sellerId",
        factory = ViewModelFactory(repository, sellerId)
    )

    val currentScreen by sellerViewModel.currentScreen.collectAsStateWithLifecycle()
    val profile by sellerViewModel.sellerProfile.collectAsStateWithLifecycle()
    val orders by sellerViewModel.sellerOrders.collectAsStateWithLifecycle()
    val products by sellerViewModel.sellerProducts.collectAsStateWithLifecycle()
    val selectedOrderForSticker by sellerViewModel.selectedOrderForSticker.collectAsStateWithLifecycle()
    val stickerItems by sellerViewModel.stickerItems.collectAsStateWithLifecycle()
    val errorMessage by sellerViewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by sellerViewModel.successMessage.collectAsStateWithLifecycle()

    when (currentScreen) {
        SellerScreen.DASHBOARD -> {
            SellerDashboardScreen(
                profile = profile,
                orders = orders,
                onNavigate = { sellerViewModel.navigateTo(it) },
                onLogoutClick = onLogout
            )
        }
        SellerScreen.ORDERS -> {
            SellerOrdersScreen(
                orders = orders,
                onAcceptOrder = { sellerViewModel.acceptOrder(it) },
                onRejectOrder = { oId, reason -> sellerViewModel.rejectOrder(oId, reason) },
                onStartPreparing = { sellerViewModel.startPreparing(it) },
                onMarkReady = { sellerViewModel.markReady(it) },
                onPrintSticker = { order -> sellerViewModel.openStickerDialog(order) },
                onBackClick = { sellerViewModel.navigateTo(SellerScreen.DASHBOARD) },
                successMessage = successMessage,
                errorMessage = errorMessage
            )
        }
        SellerScreen.PRODUCTS -> {
            SellerProductsScreen(
                products = products,
                onAddProduct = { n, d, p, cat, veg, img -> sellerViewModel.addProduct(n, d, p, cat, veg, img) },
                onUpdateProduct = { sellerViewModel.updateProduct(it) },
                onDeleteProduct = { sellerViewModel.deleteProduct(it) },
                onToggleAvailability = { id, cur -> sellerViewModel.toggleProductAvailability(id, cur) },
                onBackClick = { sellerViewModel.navigateTo(SellerScreen.DASHBOARD) },
                successMessage = successMessage,
                errorMessage = errorMessage
            )
        }
        SellerScreen.EARNINGS -> {
            SellerEarningsScreen(
                profile = profile,
                onBackClick = { sellerViewModel.navigateTo(SellerScreen.DASHBOARD) }
            )
        }
    }

    // THE THERMAL STICKER PRINT DIALOG
    if (selectedOrderForSticker != null) {
        ThermalStickerDialog(
            order = selectedOrderForSticker!!,
            items = stickerItems,
            onDismiss = { sellerViewModel.closeStickerDialog() }
        )
    }
}

@Composable
private fun RiderRootScreen(
    repository: FoodDeliveryRepository,
    riderId: String,
    onLogout: () -> Unit
) {
    val riderViewModel: RiderViewModel = viewModel(
        key = "rider_$riderId",
        factory = ViewModelFactory(repository, riderId)
    )

    val profile by riderViewModel.riderProfile.collectAsStateWithLifecycle()
    val orders by riderViewModel.riderOrders.collectAsStateWithLifecycle()
    val selectedOrderForOtp by riderViewModel.selectedOrderForOtp.collectAsStateWithLifecycle()
    val errorMessage by riderViewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by riderViewModel.successMessage.collectAsStateWithLifecycle()

    RiderDashboardScreen(
        rider = profile,
        orders = orders,
        selectedOrderForOtp = selectedOrderForOtp,
        onAcceptOrder = { riderViewModel.acceptOrder(it) },
        onPickUpOrder = { riderViewModel.pickUpOrder(it) },
        onOutForDelivery = { riderViewModel.outForDelivery(it) },
        onPromptOtp = { riderViewModel.promptDeliveryOtp(it) },
        onVerifyOtp = { oId, otp -> riderViewModel.verifyDeliveryOtp(oId, otp) },
        onDismissOtp = { riderViewModel.dismissDeliveryOtp() },
        onLogoutClick = onLogout,
        errorMessage = errorMessage,
        successMessage = successMessage
    )
}

@Composable
private fun AdminRootScreen(
    repository: FoodDeliveryRepository,
    onLogout: () -> Unit
) {
    val adminViewModel: AdminViewModel = viewModel(factory = ViewModelFactory(repository))

    val currentScreen by adminViewModel.currentScreen.collectAsStateWithLifecycle()
    val customers by adminViewModel.customers.collectAsStateWithLifecycle()
    val sellers by adminViewModel.sellers.collectAsStateWithLifecycle()
    val riders by adminViewModel.riders.collectAsStateWithLifecycle()
    val eligibleRiders by adminViewModel.eligibleRiders.collectAsStateWithLifecycle()
    val orders by adminViewModel.orders.collectAsStateWithLifecycle()
    val coupons by adminViewModel.coupons.collectAsStateWithLifecycle()
    val payouts by adminViewModel.payouts.collectAsStateWithLifecycle()
    val settings by adminViewModel.settings.collectAsStateWithLifecycle()
    val revenue by adminViewModel.totalRevenue.collectAsStateWithLifecycle()
    val successMessage by adminViewModel.successMessage.collectAsStateWithLifecycle()
    val errorMessage by adminViewModel.errorMessage.collectAsStateWithLifecycle()

    when (currentScreen) {
        AdminScreen.DASHBOARD -> {
            AdminDashboardScreen(
                customers = customers,
                sellers = sellers,
                riders = riders,
                orders = orders,
                revenue = revenue ?: 0.0,
                onNavigate = { adminViewModel.navigateTo(it) },
                onLogoutClick = onLogout
            )
        }
        AdminScreen.SELLERS -> {
            AdminSellersScreen(
                sellers = sellers,
                onApproveSeller = { adminViewModel.approveSeller(it) },
                onRejectSeller = { adminViewModel.rejectSeller(it) },
                onSuspendSeller = { adminViewModel.suspendSeller(it) },
                onActivateSeller = { adminViewModel.activateSeller(it) },
                onRecordPayout = { seller, amount, notes -> adminViewModel.recordSellerPayout(seller, amount, notes) },
                onBackClick = { adminViewModel.navigateTo(AdminScreen.DASHBOARD) },
                successMessage = successMessage,
                errorMessage = errorMessage
            )
        }
        AdminScreen.RIDERS -> {
            AdminRidersScreen(
                riders = riders,
                onApproveRider = { adminViewModel.approveRider(it) },
                onRejectRider = { adminViewModel.rejectRider(it) },
                onToggleActive = { rId, status -> adminViewModel.toggleRiderStatus(rId, status) },
                onRecordPayout = { rider, amount, notes -> adminViewModel.recordRiderPayout(rider, amount, notes) },
                onBackClick = { adminViewModel.navigateTo(AdminScreen.DASHBOARD) },
                successMessage = successMessage,
                errorMessage = errorMessage
            )
        }
        AdminScreen.CUSTOMERS -> {
            AdminCustomersScreen(
                customers = customers,
                onToggleBlock = { cId, status -> adminViewModel.toggleCustomerBlock(cId, status) },
                onBackClick = { adminViewModel.navigateTo(AdminScreen.DASHBOARD) }
            )
        }
        AdminScreen.ORDERS -> {
            AdminOrdersScreen(
                orders = orders,
                eligibleRiders = eligibleRiders,
                onAssignRider = { oId, rider -> adminViewModel.assignRiderToOrder(oId, rider) },
                onBackClick = { adminViewModel.navigateTo(AdminScreen.DASHBOARD) },
                successMessage = successMessage,
                errorMessage = errorMessage
            )
        }
        AdminScreen.COUPONS -> {
            AdminCouponsScreen(
                coupons = coupons,
                onAddCoupon = { code, title, pct, maxD, minO -> adminViewModel.addCoupon(code, title, pct, maxD, minO) },
                onToggleActive = { code, cur -> adminViewModel.toggleCoupon(code, cur) },
                onDeleteCoupon = { adminViewModel.deleteCoupon(it) },
                onBackClick = { adminViewModel.navigateTo(AdminScreen.DASHBOARD) },
                successMessage = successMessage,
                errorMessage = errorMessage
            )
        }
        AdminScreen.SETTINGS -> {
            AdminSettingsScreen(
                settings = settings,
                onSaveSettings = { adminViewModel.updateSettings(it) },
                onBackClick = { adminViewModel.navigateTo(AdminScreen.DASHBOARD) },
                successMessage = successMessage
            )
        }
        AdminScreen.PAYOUTS -> {
            AdminPayoutsScreen(
                payouts = payouts,
                onBackClick = { adminViewModel.navigateTo(AdminScreen.DASHBOARD) }
            )
        }
    }
}
