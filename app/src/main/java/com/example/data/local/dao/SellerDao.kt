package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.SellerProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SellerDao {
    @Query("SELECT * FROM sellers WHERE sellerId = :sellerId")
    fun getSellerById(sellerId: String): Flow<SellerProfileEntity?>

    @Query("SELECT * FROM sellers WHERE sellerId = :sellerId LIMIT 1")
    suspend fun getSellerByIdSync(sellerId: String): SellerProfileEntity?

    @Query("SELECT * FROM sellers ORDER BY createdAt DESC")
    fun getAllSellers(): Flow<List<SellerProfileEntity>>

    @Query("SELECT * FROM sellers WHERE status = 'APPROVED' ORDER BY rating DESC, createdAt DESC")
    fun getApprovedSellers(): Flow<List<SellerProfileEntity>>

    @Query("SELECT * FROM sellers WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingSellers(): Flow<List<SellerProfileEntity>>

    @Query("SELECT COUNT(*) FROM sellers")
    fun countAllSellers(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sellers WHERE status = :status")
    fun countSellersByStatus(status: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeller(seller: SellerProfileEntity)

    @Update
    suspend fun updateSeller(seller: SellerProfileEntity)

    @Query("UPDATE sellers SET status = :status WHERE sellerId = :sellerId")
    suspend fun updateSellerStatus(sellerId: String, status: String)

    @Query("UPDATE sellers SET earnings = earnings + :amount, pendingPayout = pendingPayout + :amount, totalOrders = totalOrders + 1 WHERE sellerId = :sellerId")
    suspend fun addOrderEarnings(sellerId: String, amount: Double)

    @Query("UPDATE sellers SET pendingPayout = pendingPayout - :payoutAmount, paidPayout = paidPayout + :payoutAmount WHERE sellerId = :sellerId")
    suspend fun recordPayout(sellerId: String, payoutAmount: Double)
}
