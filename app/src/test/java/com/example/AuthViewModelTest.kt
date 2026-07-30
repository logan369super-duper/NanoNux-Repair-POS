package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.UserEntity
import com.example.data.repository.PosRepositoryImpl
import com.example.domain.model.UserRole
import com.example.viewmodel.AuthViewModel
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
class AuthViewModelTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: PosRepositoryImpl
    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PosRepositoryImpl(database)
        viewModel = AuthViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        database.close()
    }

    @Test
    fun updatePin_failsWhenNotSixDigits() = runTest {
        val admin = UserEntity(id = 1, name = "Admin User", role = UserRole.ADMIN, pinCode = "123456")
        viewModel.setAuthenticatedUser(admin)

        var error: String? = null
        viewModel.updateCurrentUserPin("1234", {}, { error = it })

        assertEquals("PIN must be exactly 6 digits.", error)
    }

    @Test
    fun updatePin_failsWhenDuplicate() = runTest {
        val otherUser = UserEntity(id = 2, name = "Other User", role = UserRole.STAFF, pinCode = "654321")
        repository.insertUser(otherUser)

        val admin = UserEntity(id = 1, name = "Admin User", role = UserRole.ADMIN, pinCode = "123456")
        repository.insertUser(admin)
        viewModel.setAuthenticatedUser(admin)

        var error: String? = null
        viewModel.updateCurrentUserPin("654321", {}, { error = it })

        kotlinx.coroutines.runBlocking {
            kotlinx.coroutines.withTimeoutOrNull(2000) {
                while (error == null) {
                    kotlinx.coroutines.delay(20)
                }
            }
        }

        assertEquals("This PIN code is already in use by another user.", error)
    }

    @Test
    fun updatePin_succeedsWithValidNewPin() = runTest {
        val admin = UserEntity(id = 1, name = "Admin User", role = UserRole.ADMIN, pinCode = "123456")
        repository.insertUser(admin)
        viewModel.setAuthenticatedUser(admin)

        var success = false
        viewModel.updateCurrentUserPin("999999", { success = true }, {})

        kotlinx.coroutines.runBlocking {
            kotlinx.coroutines.withTimeoutOrNull(2000) {
                while (!success) {
                    kotlinx.coroutines.delay(20)
                }
            }
        }

        assertTrue(success)
        assertEquals("999999", viewModel.uiState.value.currentUser?.pinCode)
    }
}
