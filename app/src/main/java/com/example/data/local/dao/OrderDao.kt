package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.OrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun getOrderById(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderByIdSync(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getOrdersByCustomer(customerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    fun getOrdersBySeller(sellerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE riderId = :riderId ORDER BY createdAt DESC")
    fun getOrdersByRider(riderId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE (orderStatus = 'READY' AND riderId IS NULL) OR riderId = :riderId ORDER BY createdAt DESC")
    fun getOrdersForRiderDashboard(riderId: String): Flow<List<OrderEntity>>

    @Query("SELECT COUNT(*) FROM orders")
    fun countAllOrders(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE orderStatus = :status")
    fun countOrdersByStatus(status: String): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM orders WHERE orderStatus = 'DELIVERED'")
    fun getTotalRevenue(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET orderStatus = :status, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET riderId = :riderId, riderName = :riderName, riderPhone = :riderPhone, orderStatus = 'RIDER_ASSIGNED', updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun assignRider(orderId: String, riderId: String, riderName: String, riderPhone: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET isOtpVerified = 1, orderStatus = 'DELIVERED', paymentStatus = 'PAID', updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun markDeliveredWithOtp(orderId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET orderStatus = 'CANCELLED', cancellationReason = :reason, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun cancelOrder(orderId: String, reason: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE orders SET rating = :rating, reviewText = :reviewText WHERE id = :orderId")
    suspend fun setOrderReview(orderId: String, rating: Int, reviewText: String)

    // Order Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: String): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItemsSync(orderId: String): List<OrderItemEntity>
}
