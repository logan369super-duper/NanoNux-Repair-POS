package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ProductEntity
import com.example.data.repository.PosRepositoryImpl
import com.example.domain.model.CartItem
import com.example.domain.model.CartItemType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PosRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: PosRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PosRepositoryImpl(database)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun processPosSale_calculatesTotalsAndDeductsStock() = runBlocking {
        // 1. Insert product
        val prodId = database.productDao().insertProduct(
            ProductEntity(
                sku = "TEST-SCR",
                name = "Test OLED Screen",
                brand = "Apple",
                model = "iPhone 13",
                type = "PART",
                unitPrice = 100.0,
                costPrice = 50.0,
                stockQuantity = 10.0,
                minStockThreshold = 2.0
            )
        )

        // 2. Prepare cart item
        val cartItems = listOf(
            CartItem(
                id = "PROD_$prodId",
                name = "Test OLED Screen",
                type = CartItemType.PRODUCT_PART,
                unitPrice = 100.0,
                costPrice = 50.0,
                quantity = 2,
                referenceId = prodId
            )
        )

        // 3. Process POS sale
        val transaction = repository.processPosSale(
            items = cartItems,
            discount = 10.0,
            taxPercent = 10.0,
            paymentMethod = "CASH",
            cashierName = "John Manager",
            customerName = "Test Customer",
            customerPhone = "555-0192",
            notes = "Test Note"
        )

        // Assert transaction values
        assertNotNull(transaction)
        assertEquals(200.0, transaction.subtotal, 0.01) // 2 * 100
        assertEquals(10.0, transaction.discount, 0.01)
        assertEquals(19.0, transaction.tax, 0.01) // (200 - 10) * 0.10
        assertEquals(209.0, transaction.totalAmount, 0.01) // 190 + 19
        assertEquals(100.0, transaction.cogs, 0.01) // 2 * 50

        // Assert stock quantity deducted from 10 to 8
        val updatedProduct = database.productDao().getProductById(prodId)
        assertNotNull(updatedProduct)
        assertEquals(8.0, updatedProduct!!.stockQuantity, 0.01)
    }
}
