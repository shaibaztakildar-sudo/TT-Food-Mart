package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.RiderProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RiderDao {
    @Query("SELECT * FROM riders WHERE riderId = :riderId")
    fun getRiderById(riderId: String): Flow<RiderProfileEntity?>

    @Query("SELECT * FROM riders WHERE riderId = :riderId LIMIT 1")
    suspend fun getRiderByIdSync(riderId: String): RiderProfileEntity?

    @Query("SELECT * FROM riders ORDER BY createdAt DESC")
    fun getAllRiders(): Flow<List<RiderProfileEntity>>

    @Query("SELECT * FROM riders WHERE status IN ('APPROVED', 'ACTIVE') ORDER BY createdAt DESC")
    fun getEligibleRiders(): Flow<List<RiderProfileEntity>>

    @Query("SELECT * FROM riders WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingRiders(): Flow<List<RiderProfileEntity>>

    @Query("SELECT COUNT(*) FROM riders")
    fun countAllRiders(): Flow<Int>

    @Query("SELECT COUNT(*) FROM riders WHERE status = :status")
    fun countRidersByStatus(status: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRider(rider: RiderProfileEntity)

    @Update
    suspend fun updateRider(rider: RiderProfileEntity)

    @Query("UPDATE riders SET status = :status WHERE riderId = :riderId")
    suspend fun updateRiderStatus(riderId: String, status: String)

    @Query("UPDATE riders SET earnings = earnings + :amount, pendingPayout = pendingPayout + :amount, totalDeliveries = totalDeliveries + 1 WHERE riderId = :riderId")
    suspend fun addDeliveryEarnings(riderId: String, amount: Double)

    @Query("UPDATE riders SET pendingPayout = pendingPayout - :payoutAmount, paidPayout = paidPayout + :payoutAmount WHERE riderId = :riderId")
    suspend fun recordPayout(riderId: String, payoutAmount: Double)
}
