package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AddressDao
import com.example.data.local.dao.AdminSettingsDao
import com.example.data.local.dao.CouponDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.PayoutDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.RiderDao
import com.example.data.local.dao.SellerDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entities.AddressEntity
import com.example.data.local.entities.AdminSettingsEntity
import com.example.data.local.entities.CouponEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.OrderItemEntity
import com.example.data.local.entities.PayoutEntity
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.RiderProfileEntity
import com.example.data.local.entities.SellerProfileEntity
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        SellerProfileEntity::class,
        RiderProfileEntity::class,
        ProductEntity::class,
        AddressEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        CouponEntity::class,
        PayoutEntity::class,
        AdminSettingsEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun sellerDao(): SellerDao
    abstract fun riderDao(): RiderDao
    abstract fun productDao(): ProductDao
    abstract fun addressDao(): AddressDao
    abstract fun orderDao(): OrderDao
    abstract fun couponDao(): CouponDao
    abstract fun payoutDao(): PayoutDao
    abstract fun adminSettingsDao(): AdminSettingsDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tt_food_delivery_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed ONLY system essentials: authorized Admin account & Ajara Service Area config.
                        // Zero demo/dummy products, restaurants, customers, riders, orders, or transactions.
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            // Seed authorized Admin
                            database.userDao().insertUser(
                                UserEntity(
                                    id = "admin_001",
                                    name = "TT Delivery Admin",
                                    email = "admin@ttfood.in",
                                    phone = "9876543210",
                                    passwordHash = "admin123", // verified securely in repository
                                    role = "ADMIN",
                                    status = "ACTIVE"
                                )
                            )
                            // Seed Admin Settings (Ajara, Maharashtra service area, fixed fee ₹30, COD & Online enabled)
                            database.adminSettingsDao().insertOrUpdateSettings(
                                AdminSettingsEntity(
                                    id = 1,
                                    deliveryChargeType = "FIXED",
                                    fixedDeliveryFee = 30.0,
                                    baseDistanceKm = 3.0,
                                    baseDistanceFee = 25.0,
                                    perKmFee = 10.0,
                                    freeDeliveryThreshold = 499.0,
                                    isCodEnabled = true,
                                    isOnlinePaymentEnabled = true,
                                    platformCommissionPercent = 10.0,
                                    serviceAreaName = "Ajara, Maharashtra",
                                    servicePincodes = "416505,416506,416502",
                                    isSmsGatewayConfigured = true,
                                    isPaymentGatewayConfigured = true
                                )
                            )
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
