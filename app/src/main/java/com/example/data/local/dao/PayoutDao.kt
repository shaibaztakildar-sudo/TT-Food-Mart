package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.PayoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PayoutDao {
    @Query("SELECT * FROM payouts ORDER BY timestamp DESC")
    fun getAllPayouts(): Flow<List<PayoutEntity>>

    @Query("SELECT * FROM payouts WHERE recipientId = :recipientId ORDER BY timestamp DESC")
    fun getPayoutsByRecipient(recipientId: String): Flow<List<PayoutEntity>>

    @Query("SELECT SUM(amount) FROM payouts WHERE recipientType = 'SELLER'")
    fun getTotalSellerPayouts(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM payouts WHERE recipientType = 'RIDER'")
    fun getTotalRiderPayouts(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayout(payout: PayoutEntity)
}
