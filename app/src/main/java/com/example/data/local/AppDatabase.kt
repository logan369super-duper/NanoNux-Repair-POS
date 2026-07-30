package com.example.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import com.example.domain.model.RepairTicketStatus
import com.example.domain.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        ServiceOrderEntity::class,
        TransactionLogEntity::class,
        ShopSettingsEntity::class,
        RepairServiceEntity::class,
        PromotionEntity::class,
        RepairItemBrandEntity::class,
        RepairIssueTypeEntity::class,
        CatalogCategoryEntity::class,
        UnitMeasurementEntity::class,
        CustomerEntity::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun serviceOrderDao(): ServiceOrderDao
    abstract fun transactionLogDao(): TransactionLogDao
    abstract fun shopSettingsDao(): ShopSettingsDao
    abstract fun repairServiceDao(): RepairServiceDao
    abstract fun promotionDao(): PromotionDao
    abstract fun repairItemBrandDao(): RepairItemBrandDao
    abstract fun repairIssueTypeDao(): RepairIssueTypeDao
    abstract fun catalogCategoryDao(): CatalogCategoryDao
    abstract fun unitMeasurementDao(): UnitMeasurementDao
    abstract fun customerDao(): CustomerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "repair_pos_clean_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
