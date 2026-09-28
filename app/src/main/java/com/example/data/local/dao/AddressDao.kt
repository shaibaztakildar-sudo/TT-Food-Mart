package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AddressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses WHERE customerId = :customerId ORDER BY isDefault DESC, createdAt DESC")
    fun getAddressesByCustomer(customerId: String): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE customerId = :customerId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultAddress(customerId: String): AddressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)

    @Update
    suspend fun updateAddress(address: AddressEntity)

    @Query("UPDATE addresses SET isDefault = 0 WHERE customerId = :customerId")
    suspend fun resetDefaultAddress(customerId: String)

    @Query("UPDATE addresses SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultAddress(id: String)

    @Query("DELETE FROM addresses WHERE id = :id")
    suspend fun deleteAddress(id: String)
}
