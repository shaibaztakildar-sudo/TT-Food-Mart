package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random

class FoodDeliveryRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val sellerDao = db.sellerDao()
    private val riderDao = db.riderDao()
    private val productDao = db.productDao()
    private val addressDao = db.addressDao()
    private val orderDao = db.orderDao()
    private val couponDao = db.couponDao()
    private val payoutDao = db.payoutDao()
    private val adminSettingsDao = db.adminSettingsDao()
    private val notificationDao = db.notificationDao()

    // -------------------------------------------------------------
    // AUTHENTICATION & USERS
    // -------------------------------------------------------------

    suspend fun login(identifier: String, password: String, expectedRole: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val user = if (identifier.contains("@")) {
            userDao.getUserByEmail(identifier.trim().lowercase())
        } else {
            userDao.getUserByPhone(identifier.trim())
        }

        if (user == null) {
            return@withContext Result.failure(Exception("Account not found with this email/phone"))
        }

        if (user.role != expectedRole && expectedRole != "ANY") {
            return@withContext Result.failure(Exception("This account is registered as ${user.role}. Please login through the ${user.role.lowercase().replaceFirstChar { it.uppercase() }} portal."))
        }

        if (user.passwordHash != password && user.passwordHash != hashPassword(password)) {
            return@withContext Result.failure(Exception("Invalid password. Please check your credentials."))
        }

        if (user.status == "BLOCKED") {
            return@withContext Result.failure(Exception("This account has been suspended by Admin. Please contact support."))
        }

        if (user.role == "SELLER") {
            val seller = sellerDao.getSellerByIdSync(user.id)
            if (seller?.status == "PENDING") {
                return@withContext Result.failure(Exception("Your restaurant application is currently under review by Admin. You can access the panel once approved."))
            } else if (seller?.status == "REJECTED") {
                return@withContext Result.failure(Exception("Your restaurant application was rejected. Please contact administration."))
            } else if (seller?.status == "SUSPENDED") {
                return@withContext Result.failure(Exception("Your restaurant account has been suspended."))
            }
        }

        if (user.role == "RIDER") {
            val rider = riderDao.getRiderByIdSync(user.id)
            if (rider?.status == "PENDING") {
                return@withContext Result.failure(Exception("Your rider application is awaiting Admin approval."))
            } else if (rider?.status == "REJECTED") {
                return@withContext Result.failure(Exception("Your rider registration was rejected."))
            }
        }

        Result.success(user)
    }

    suspend fun registerCustomer(name: String, email: String, phone: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        if (userDao.getUserByEmail(email.trim().lowercase()) != null) {
            return@withContext Result.failure(Exception("An account with this email already exists"))
        }
        if (userDao.getUserByPhone(phone.trim()) != null) {
            return@withContext Result.failure(Exception("An account with this phone number already exists"))
        }

        val newUser = UserEntity(
            id = "cust_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            email = email.trim().lowercase(),
            phone = phone.trim(),
            passwordHash = password,
            role = "CUSTOMER",
            status = "ACTIVE"
        )
        userDao.insertUser(newUser)
        Result.success(newUser)
    }

    suspend fun registerSeller(
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
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        if (userDao.getUserByEmail(email.trim().lowercase()) != null) {
            return@withContext Result.failure(Exception("An account with this email already exists"))
        }
        if (userDao.getUserByPhone(phone.trim()) != null) {
            return@withContext Result.failure(Exception("An account with this phone number already exists"))
        }

        val userId = "seller_${UUID.randomUUID().toString().take(8)}"
        val user = UserEntity(
            id = userId,
            name = ownerName.trim(),
            email = email.trim().lowercase(),
            phone = phone.trim(),
            passwordHash = password,
            role = "SELLER",
            profilePhotoUri = imageUri,
            status = "PENDING"
        )
        val sellerProfile = SellerProfileEntity(
            sellerId = userId,
            restaurantName = restaurantName.trim(),
            ownerName = ownerName.trim(),
            phone = phone.trim(),
            email = email.trim().lowercase(),
            address = address.trim(),
            panNumber = panNumber.trim(),
            bankAccount = bankAccount.trim(),
            bankIfsc = bankIfsc.trim().uppercase(),
            bankName = bankName.trim(),
            documentProofUri = documentProofUri,
            imageUri = imageUri,
            status = "PENDING"
        )
        userDao.insertUser(user)
        sellerDao.insertSeller(sellerProfile)

        // Notify Admin
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUserId = "ADMIN",
                title = "New Seller Application",
                message = "${restaurantName.trim()} (Owner: ${ownerName.trim()}) has applied for seller registration."
            )
        )
        Result.success(user)
    }

    suspend fun registerRider(
        name: String,
        phone: String,
        email: String,
        password: String,
        vehicleNumber: String,
        licenseUri: String?
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        if (userDao.getUserByEmail(email.trim().lowercase()) != null) {
            return@withContext Result.failure(Exception("An account with this email already exists"))
        }
        if (userDao.getUserByPhone(phone.trim()) != null) {
            return@withContext Result.failure(Exception("An account with this phone number already exists"))
        }

        val userId = "rider_${UUID.randomUUID().toString().take(8)}"
        val user = UserEntity(
            id = userId,
            name = name.trim(),
            email = email.trim().lowercase(),
            phone = phone.trim(),
            passwordHash = password,
            role = "RIDER",
            status = "PENDING"
        )
        val riderProfile = RiderProfileEntity(
            riderId = userId,
            name = name.trim(),
            phone = phone.trim(),
            email = email.trim().lowercase(),
            vehicleNumber = vehicleNumber.trim().uppercase(),
            licenseUri = licenseUri,
            status = "PENDING"
        )
        userDao.insertUser(user)
        riderDao.insertRider(riderProfile)

        // Notify Admin
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUserId = "ADMIN",
                title = "New Rider Application",
                message = "${name.trim()} (Vehicle: ${vehicleNumber.trim()}) applied as delivery partner."
            )
        )
        Result.success(user)
    }

    fun getUser(userId: String): Flow<UserEntity?> = userDao.getUserById(userId)

    suspend fun updateProfile(userId: String, name: String, email: String, phone: String) = withContext(Dispatchers.IO) {
        userDao.updateProfile(userId, name.trim(), email.trim(), phone.trim())
    }

    suspend fun updateProfilePhoto(userId: String, photoUri: String?) = withContext(Dispatchers.IO) {
        userDao.updateProfilePhoto(userId, photoUri)
    }

    // -------------------------------------------------------------
    // RESTAURANTS & PRODUCTS
    // -------------------------------------------------------------

    fun getApprovedSellers(): Flow<List<SellerProfileEntity>> = sellerDao.getApprovedSellers()
    fun getSellerProfile(sellerId: String): Flow<SellerProfileEntity?> = sellerDao.getSellerById(sellerId)

    fun getAvailableProducts(): Flow<List<ProductEntity>> = productDao.getAvailableProducts()
    fun getProductsBySeller(sellerId: String): Flow<List<ProductEntity>> = productDao.getProductsBySeller(sellerId)
    fun getAvailableProductsBySeller(sellerId: String): Flow<List<ProductEntity>> = productDao.getAvailableProductsBySeller(sellerId)
    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)
    fun getProductById(id: String): Flow<ProductEntity?> = productDao.getProductById(id)

    suspend fun addProduct(
        sellerId: String,
        restaurantName: String,
        name: String,
        description: String,
        price: Double,
        category: String,
        isVeg: Boolean,
        imageUri: String?
    ) = withContext(Dispatchers.IO) {
        val product = ProductEntity(
            id = "prod_${UUID.randomUUID().toString().take(8)}",
            sellerId = sellerId,
            restaurantName = restaurantName,
            name = name.trim(),
            description = description.trim(),
            price = price,
            category = category.trim(),
            isVeg = isVeg,
            isAvailable = true,
            imageUri = imageUri
        )
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    suspend fun updateProductAvailability(id: String, isAvailable: Boolean) = withContext(Dispatchers.IO) {
        productDao.updateAvailability(id, isAvailable)
    }

    suspend fun deleteProduct(id: String) = withContext(Dispatchers.IO) {
        productDao.deleteProductById(id)
    }

    // -------------------------------------------------------------
    // ADDRESSES & SERVICE AREA
    // -------------------------------------------------------------

    fun getCustomerAddresses(customerId: String): Flow<List<AddressEntity>> = addressDao.getAddressesByCustomer(customerId)

    suspend fun addAddress(address: AddressEntity) = withContext(Dispatchers.IO) {
        if (address.isDefault) {
            addressDao.resetDefaultAddress(address.customerId)
        }
        addressDao.insertAddress(address)
    }

    suspend fun deleteAddress(id: String) = withContext(Dispatchers.IO) {
        addressDao.deleteAddress(id)
    }

    suspend fun setDefaultAddress(customerId: String, addressId: String) = withContext(Dispatchers.IO) {
        addressDao.resetDefaultAddress(customerId)
        addressDao.setDefaultAddress(addressId)
    }

    suspend fun validateServiceArea(pincode: String): Boolean = withContext(Dispatchers.IO) {
        val settings = adminSettingsDao.getSettingsSync() ?: return@withContext true
        val allowedPincodes = settings.servicePincodes.split(",").map { it.trim() }
        allowedPincodes.isEmpty() || allowedPincodes.contains(pincode.trim())
    }

    // -------------------------------------------------------------
    // COUPONS
    // -------------------------------------------------------------

    fun getActiveCoupons(): Flow<List<CouponEntity>> = couponDao.getActiveCoupons()
    fun getAllCoupons(): Flow<List<CouponEntity>> = couponDao.getAllCoupons()

    suspend fun validateCoupon(code: String, subtotal: Double): Result<Double> = withContext(Dispatchers.IO) {
        val coupon = couponDao.getCouponByCode(code.trim().uppercase())
            ?: return@withContext Result.failure(Exception("Invalid coupon code"))

        if (!coupon.isActive) {
            return@withContext Result.failure(Exception("This coupon has expired or is inactive"))
        }

        if (subtotal < coupon.minOrderAmount) {
            return@withContext Result.failure(Exception("Minimum order amount of ₹${coupon.minOrderAmount} required for this coupon"))
        }

        val calculatedDiscount = (subtotal * coupon.discountPercent) / 100.0
        val finalDiscount = minOf(calculatedDiscount, coupon.maxDiscountAmount)
        Result.success(finalDiscount)
    }

    suspend fun addCoupon(coupon: CouponEntity) = withContext(Dispatchers.IO) {
        couponDao.insertCoupon(coupon)
    }

    suspend fun toggleCouponStatus(code: String, isActive: Boolean) = withContext(Dispatchers.IO) {
        couponDao.updateCouponStatus(code, isActive)
    }

    suspend fun deleteCoupon(code: String) = withContext(Dispatchers.IO) {
        couponDao.deleteCoupon(code)
    }

    // -------------------------------------------------------------
    // ORDERS & CHECKOUT
    // -------------------------------------------------------------

    data class CartItem(
        val product: ProductEntity,
        val quantity: Int
    )

    suspend fun placeOrder(
        customer: UserEntity,
        restaurant: SellerProfileEntity,
        items: List<CartItem>,
        deliveryAddress: AddressEntity,
        paymentMethod: String,
        couponCode: String?,
        discount: Double,
        customerInstructions: String
    ): Result<OrderEntity> = withContext(Dispatchers.IO) {
        if (items.isEmpty()) {
            return@withContext Result.failure(Exception("Cart is empty"))
        }

        // Validate service area pincode
        if (!validateServiceArea(deliveryAddress.pincode)) {
            val settings = adminSettingsDao.getSettingsSync()
            val area = settings?.serviceAreaName ?: "Ajara, Maharashtra"
            return@withContext Result.failure(Exception("Delivery is currently only available in $area (${settings?.servicePincodes})"))
        }

        val settings = adminSettingsDao.getSettingsSync()
        val subtotal = items.sumOf { it.product.price * it.quantity }

        val deliveryCharge = if (subtotal >= (settings?.freeDeliveryThreshold ?: 499.0)) {
            0.0
        } else {
            settings?.fixedDeliveryFee ?: 30.0
        }

        val totalAmount = maxOf(0.0, (subtotal + deliveryCharge) - discount)

        // Generate 4-digit Delivery OTP (strictly verified upon delivery)
        val otp = String.format("%04d", Random.nextInt(1000, 9999))
        val orderNum = String.format("%04d", Random.nextInt(100, 9999))
        val orderId = "TT-${orderNum}"

        val formattedAddress = "${deliveryAddress.receiverName}, ${deliveryAddress.receiverPhone}, ${deliveryAddress.streetAddress}, ${deliveryAddress.area}, ${deliveryAddress.city} - ${deliveryAddress.pincode}"

        val isPaid = paymentMethod == "ONLINE"
        val paymentTransactionId = if (isPaid) "PAY_TXN_${UUID.randomUUID().toString().take(10).uppercase()}" else null

        val order = OrderEntity(
            id = orderId,
            customerId = customer.id,
            customerName = customer.name,
            customerPhone = customer.phone,
            sellerId = restaurant.sellerId,
            restaurantName = restaurant.restaurantName,
            restaurantAddress = restaurant.address,
            deliveryAddress = formattedAddress,
            customerInstructions = customerInstructions,
            subtotal = subtotal,
            deliveryCharge = deliveryCharge,
            discount = discount,
            totalAmount = totalAmount,
            couponCode = couponCode,
            paymentMethod = paymentMethod,
            paymentStatus = if (isPaid) "PAID" else "PENDING",
            paymentTransactionId = paymentTransactionId,
            orderStatus = "PLACED",
            deliveryOtp = otp,
            isOtpVerified = false
        )

        orderDao.insertOrder(order)

        val orderItems = items.map {
            OrderItemEntity(
                id = UUID.randomUUID().toString(),
                orderId = orderId,
                productId = it.product.id,
                productName = it.product.name,
                isVeg = it.product.isVeg,
                price = it.product.price,
                quantity = it.quantity,
                itemTotal = it.product.price * it.quantity
            )
        }
        orderDao.insertOrderItems(orderItems)

        // Notify Seller
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUserId = restaurant.sellerId,
                title = "New Order Received (#$orderId)",
                message = "New order for ₹$totalAmount from ${customer.name}",
                orderId = orderId
            )
        )

        // Notify Admin
        notificationDao.insertNotification(
            NotificationEntity(
                id = UUID.randomUUID().toString(),
                targetUserId = "ADMIN",
                title = "New Order Placed (#$orderId)",
                message = "${customer.name} ordered from ${restaurant.restaurantName}",
                orderId = orderId
            )
        )

        Result.success(order)
    }

    fun getCustomerOrders(customerId: String): Flow<List<OrderEntity>> = orderDao.getOrdersByCustomer(customerId)
    fun getOrderById(orderId: String): Flow<OrderEntity?> = orderDao.getOrderById(orderId)
    suspend fun getOrderByIdSync(orderId: String): OrderEntity? = orderDao.getOrderByIdSync(orderId)
    fun getOrderItems(orderId: String): Flow<List<OrderItemEntity>> = orderDao.getOrderItems(orderId)
    suspend fun getOrderItemsSync(orderId: String): List<OrderItemEntity> = orderDao.getOrderItemsSync(orderId)

    suspend fun submitOrderReview(orderId: String, rating: Int, reviewText: String) = withContext(Dispatchers.IO) {
        orderDao.setOrderReview(orderId, rating, reviewText.trim())
    }

    suspend fun cancelOrder(orderId: String, reason: String) = withContext(Dispatchers.IO) {
        orderDao.cancelOrder(orderId, reason)
    }

    // -------------------------------------------------------------
    // SELLER ACTIONS
    // -------------------------------------------------------------

    fun getSellerOrders(sellerId: String): Flow<List<OrderEntity>> = orderDao.getOrdersBySeller(sellerId)

    suspend fun sellerAcceptOrder(orderId: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, "ACCEPTED")
    }

    suspend fun sellerRejectOrder(orderId: String, reason: String) = withContext(Dispatchers.IO) {
        orderDao.cancelOrder(orderId, "Rejected by Restaurant: $reason")
    }

    suspend fun sellerStartPreparing(orderId: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, "PREPARING")
    }

    suspend fun sellerMarkReady(orderId: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, "READY")
    }

    // -------------------------------------------------------------
    // RIDER ACTIONS
    // -------------------------------------------------------------

    fun getRiderProfile(riderId: String): Flow<RiderProfileEntity?> = riderDao.getRiderById(riderId)
    fun getRiderOrders(riderId: String): Flow<List<OrderEntity>> = orderDao.getOrdersForRiderDashboard(riderId)

    suspend fun riderAcceptOrder(orderId: String, riderId: String, riderName: String, riderPhone: String) = withContext(Dispatchers.IO) {
        orderDao.assignRider(orderId, riderId, riderName, riderPhone)
    }

    suspend fun riderPickUpOrder(orderId: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, "PICKED_UP")
    }

    suspend fun riderOutForDelivery(orderId: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, "OUT_FOR_DELIVERY")
    }

    suspend fun verifyDeliveryOtpAndComplete(orderId: String, enteredOtp: String): Result<Unit> = withContext(Dispatchers.IO) {
        val order = orderDao.getOrderByIdSync(orderId)
            ?: return@withContext Result.failure(Exception("Order not found"))

        if (order.deliveryOtp.trim() != enteredOtp.trim()) {
            return@withContext Result.failure(Exception("Incorrect Delivery OTP! Please ask customer for the correct 4-digit OTP."))
        }

        // Mark delivered and update payment status
        orderDao.markDeliveredWithOtp(orderId)

        // Calculate and add earnings to seller and rider
        val settings = adminSettingsDao.getSettingsSync()
        val commissionRate = (settings?.platformCommissionPercent ?: 10.0) / 100.0
        val sellerEarnings = order.subtotal * (1.0 - commissionRate)
        sellerDao.addOrderEarnings(order.sellerId, sellerEarnings)

        // Rider delivery fee credit (e.g. ₹25 per delivery or delivery charge)
        val riderFee = if (order.deliveryCharge > 0) order.deliveryCharge else 25.0
        if (order.riderId != null) {
            riderDao.addDeliveryEarnings(order.riderId, riderFee)
        }

        Result.success(Unit)
    }

    // -------------------------------------------------------------
    // ADMIN ACTIONS
    // -------------------------------------------------------------

    fun getAllCustomers(): Flow<List<UserEntity>> = userDao.getUsersByRole("CUSTOMER")
    fun getAllSellers(): Flow<List<SellerProfileEntity>> = sellerDao.getAllSellers()
    fun getPendingSellers(): Flow<List<SellerProfileEntity>> = sellerDao.getPendingSellers()
    fun getAllRiders(): Flow<List<RiderProfileEntity>> = riderDao.getAllRiders()
    fun getPendingRiders(): Flow<List<RiderProfileEntity>> = riderDao.getPendingRiders()
    fun getEligibleRiders(): Flow<List<RiderProfileEntity>> = riderDao.getEligibleRiders()
    fun getAllOrders(): Flow<List<OrderEntity>> = orderDao.getAllOrders()

    fun countCustomers() = userDao.countUsersByRole("CUSTOMER")
    fun countSellers() = sellerDao.countAllSellers()
    fun countRiders() = riderDao.countAllRiders()
    fun countOrders() = orderDao.countAllOrders()
    fun countCompletedOrders() = orderDao.countOrdersByStatus("DELIVERED")
    fun countCancelledOrders() = orderDao.countOrdersByStatus("CANCELLED")
    fun countNewOrders() = orderDao.countOrdersByStatus("PLACED")
    fun getTotalRevenue() = orderDao.getTotalRevenue()

    suspend fun approveSeller(sellerId: String) = withContext(Dispatchers.IO) {
        sellerDao.updateSellerStatus(sellerId, "APPROVED")
        userDao.updateUserStatus(sellerId, "ACTIVE")
    }

    suspend fun rejectSeller(sellerId: String) = withContext(Dispatchers.IO) {
        sellerDao.updateSellerStatus(sellerId, "REJECTED")
        userDao.updateUserStatus(sellerId, "REJECTED")
    }

    suspend fun suspendSeller(sellerId: String) = withContext(Dispatchers.IO) {
        sellerDao.updateSellerStatus(sellerId, "SUSPENDED")
        userDao.updateUserStatus(sellerId, "BLOCKED")
    }

    suspend fun activateSeller(sellerId: String) = withContext(Dispatchers.IO) {
        sellerDao.updateSellerStatus(sellerId, "APPROVED")
        userDao.updateUserStatus(sellerId, "ACTIVE")
    }

    suspend fun approveRider(riderId: String) = withContext(Dispatchers.IO) {
        riderDao.updateRiderStatus(riderId, "APPROVED")
        userDao.updateUserStatus(riderId, "ACTIVE")
    }

    suspend fun rejectRider(riderId: String) = withContext(Dispatchers.IO) {
        riderDao.updateRiderStatus(riderId, "REJECTED")
        userDao.updateUserStatus(riderId, "REJECTED")
    }

    suspend fun toggleRiderActive(riderId: String, currentStatus: String) = withContext(Dispatchers.IO) {
        val newStatus = if (currentStatus == "ACTIVE" || currentStatus == "APPROVED") "INACTIVE" else "ACTIVE"
        riderDao.updateRiderStatus(riderId, newStatus)
    }

    suspend fun toggleCustomerBlock(customerId: String, currentStatus: String) = withContext(Dispatchers.IO) {
        val newStatus = if (currentStatus == "BLOCKED") "ACTIVE" else "BLOCKED"
        userDao.updateUserStatus(customerId, newStatus)
    }

    suspend fun assignRiderManual(orderId: String, rider: RiderProfileEntity) = withContext(Dispatchers.IO) {
        orderDao.assignRider(orderId, rider.riderId, rider.name, rider.phone)
    }

    suspend fun recordSellerPayout(sellerId: String, sellerName: String, amount: Double, notes: String) = withContext(Dispatchers.IO) {
        val payout = PayoutEntity(
            id = "PO_SEL_${UUID.randomUUID().toString().take(8)}",
            recipientId = sellerId,
            recipientType = "SELLER",
            recipientName = sellerName,
            amount = amount,
            referenceId = "REF_${System.currentTimeMillis().toString().takeLast(8)}",
            notes = notes
        )
        payoutDao.insertPayout(payout)
        sellerDao.recordPayout(sellerId, amount)
    }

    suspend fun recordRiderPayout(riderId: String, riderName: String, amount: Double, notes: String) = withContext(Dispatchers.IO) {
        val payout = PayoutEntity(
            id = "PO_RID_${UUID.randomUUID().toString().take(8)}",
            recipientId = riderId,
            recipientType = "RIDER",
            recipientName = riderName,
            amount = amount,
            referenceId = "REF_${System.currentTimeMillis().toString().takeLast(8)}",
            notes = notes
        )
        payoutDao.insertPayout(payout)
        riderDao.recordPayout(riderId, amount)
    }

    fun getAllPayouts(): Flow<List<PayoutEntity>> = payoutDao.getAllPayouts()
    fun getTotalSellerPayouts() = payoutDao.getTotalSellerPayouts()
    fun getTotalRiderPayouts() = payoutDao.getTotalRiderPayouts()

    fun getAdminSettings(): Flow<AdminSettingsEntity?> = adminSettingsDao.getSettings()
    suspend fun updateAdminSettings(settings: AdminSettingsEntity) = withContext(Dispatchers.IO) {
        adminSettingsDao.updateSettings(settings)
    }

    fun getNotifications(userId: String): Flow<List<NotificationEntity>> = notificationDao.getNotificationsForUser(userId)
    suspend fun markNotificationRead(id: String) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    private fun hashPassword(password: String): String {
        return password // Simple reversible or standard string compare for local DB
    }
}
