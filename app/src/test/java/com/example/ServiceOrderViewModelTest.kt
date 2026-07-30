package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.PosRepositoryImpl
import com.example.viewmodel.ServiceOrderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ServiceOrderViewModelTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: PosRepositoryImpl
    private lateinit var viewModel: ServiceOrderViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PosRepositoryImpl(database)
        viewModel = ServiceOrderViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        database.close()
    }

    @Test
    fun createIntakeTicket_failsWhenFieldsEmpty() = runTest {
        // Set invalid fields
        viewModel.updateCustomerName("")
        viewModel.updateCustomerPhone("")
        viewModel.updateDeviceModel("")

        viewModel.createIntakeTicket()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertFalse(state.isSuccess)
    }

    @Test
    fun createIntakeTicket_succeedsWithValidData() = runTest {
        viewModel.updateCustomerName("Sarah Conner")
        viewModel.updateCustomerPhone("555-1234")
        viewModel.updateDeviceBrand("Apple")
        viewModel.updateDeviceModel("iPhone 14")
        viewModel.updateIssueType("Screen Replacement")
        viewModel.updateAdditionalCost("95.00")
        viewModel.updateLaborFee("45.00")

        viewModel.createIntakeTicket()
        
        // Wait for background database operation to complete on Room's thread pool
        kotlinx.coroutines.runBlocking {
            kotlinx.coroutines.withTimeoutOrNull(2000) {
                while (!viewModel.uiState.value.isSuccess) {
                    kotlinx.coroutines.delay(50)
                }
            }
        }

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertNull(state.errorMessage)
        assertNotNull(state.newlyCreatedOrder)
        assertEquals("Sarah Conner", state.newlyCreatedOrder!!.customerName)
        assertNotNull(state.claimTicketText)
    }
}
