package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.repository.PosRepositoryImpl
import com.example.i18n.LocalStrings
import com.example.i18n.getDictionary
import com.example.ui.components.AdaptiveScaffold
import com.example.ui.components.PinInputDialog
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.screens.repair.RepairSpecsSettingsScreen
import com.example.ui.theme.RepairPosTheme
import com.example.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PosRepositoryImpl(database)

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository) as T
                    modelClass.isAssignableFrom(FirstTimeSetupViewModel::class.java) -> FirstTimeSetupViewModel(repository) as T
                    modelClass.isAssignableFrom(ShopSetupViewModel::class.java) -> ShopSetupViewModel(repository) as T
                    modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(repository) as T
                    modelClass.isAssignableFrom(ServiceOrderViewModel::class.java) -> ServiceOrderViewModel(repository) as T
                    modelClass.isAssignableFrom(PosViewModel::class.java) -> PosViewModel(repository) as T
                    modelClass.isAssignableFrom(InventoryViewModel::class.java) -> InventoryViewModel(repository) as T
                    modelClass.isAssignableFrom(TransactionLogsViewModel::class.java) -> TransactionLogsViewModel(repository) as T
                    modelClass.isAssignableFrom(ServiceCatalogViewModel::class.java) -> ServiceCatalogViewModel(repository) as T
                    modelClass.isAssignableFrom(PromotionsViewModel::class.java) -> PromotionsViewModel(repository) as T
                    modelClass.isAssignableFrom(ReportsViewModel::class.java) -> ReportsViewModel(repository) as T
                    modelClass.isAssignableFrom(CustomersViewModel::class.java) -> CustomersViewModel(repository) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class $modelClass")
                }
            }
        }

        setContent {
            val shopSetupViewModel: ShopSetupViewModel = viewModel(factory = factory)
            val shopSettingsState by shopSetupViewModel.uiState.collectAsState()
            val dictionary = getDictionary(shopSettingsState.language)

            CompositionLocalProvider(LocalStrings provides dictionary) {
                RepairPosTheme(darkTheme = shopSettingsState.isDarkMode) {
                AppPermissionRequester()
                val navController = rememberNavController()

                val authViewModel: AuthViewModel = viewModel(factory = factory)
                val firstTimeSetupViewModel: FirstTimeSetupViewModel = viewModel(factory = factory)
                val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
                val serviceOrderViewModel: ServiceOrderViewModel = viewModel(factory = factory)
                val posViewModel: PosViewModel = viewModel(factory = factory)
                val inventoryViewModel: InventoryViewModel = viewModel(factory = factory)
                val transactionLogsViewModel: TransactionLogsViewModel = viewModel(factory = factory)
                val serviceCatalogViewModel: ServiceCatalogViewModel = viewModel(factory = factory)
                val promotionsViewModel: PromotionsViewModel = viewModel(factory = factory)
                val reportsViewModel: ReportsViewModel = viewModel(factory = factory)
                val customersViewModel: CustomersViewModel = viewModel(factory = factory)

                val authState by authViewModel.uiState.collectAsState()
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
                var showPinDialog by remember { mutableStateOf(false) }

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (authState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (!authState.hasAdminUser && authState.availableUsers.isEmpty()) {
                        FirstTimeSetupScreen(
                            viewModel = firstTimeSetupViewModel,
                            onSetupComplete = { adminUser ->
                                authViewModel.setAuthenticatedUser(adminUser)
                                firstTimeSetupViewModel.resetSetupState()
                            }
                        )
                    } else if (!authState.isAuthenticated) {
                        AuthScreen(
                            authViewModel = authViewModel,
                            onAuthSuccess = {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Auth.route) { inclusive = true }
                                }
                            }
                        )
                    } else {
                        if (showPinDialog) {
                            PinInputDialog(
                                title = "Switch Cashier / Role PIN",
                                errorMessage = authState.errorMessage,
                                onPinSubmitted = { pin ->
                                    authViewModel.authenticatePin(pin)
                                    showPinDialog = false
                                },
                                onDismiss = { showPinDialog = false }
                            )
                        }

                        AdaptiveScaffold(
                            currentScreen = currentScreen,
                            currentUser = authState.currentUser,
                            onNavigate = { screen ->
                                currentScreen = screen
                                navController.navigate(screen.route) {
                                    launchSingleTop = true
                                }
                            },
                            onLogout = { authViewModel.logout() }
                        ) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Dashboard.route,
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                composable(Screen.Dashboard.route) {
                                    currentScreen = Screen.Dashboard
                                    DashboardScreen(
                                        viewModel = dashboardViewModel,
                                        currentUser = authState.currentUser,
                                        onNavigateToIntake = {
                                            currentScreen = Screen.ServicesRepairs
                                            navController.navigate(Screen.ServicesRepairs.route)
                                        },
                                        onNavigateToPos = {
                                            currentScreen = Screen.PosCheckout
                                            navController.navigate(Screen.PosCheckout.route)
                                        },
                                        onNavigateToInventory = {
                                            currentScreen = Screen.Inventory
                                            navController.navigate(Screen.Inventory.route)
                                        },
                                        onNavigateToOrders = {
                                            currentScreen = Screen.ServicesRepairs
                                            navController.navigate(Screen.ServicesRepairs.route)
                                        }
                                    )
                                }

                                composable(Screen.ServicesRepairs.route) {
                                    currentScreen = Screen.ServicesRepairs
                                    ServicesAndRepairsScreen(
                                        serviceOrderViewModel = serviceOrderViewModel,
                                        serviceCatalogViewModel = serviceCatalogViewModel,
                                        shopSetupViewModel = shopSetupViewModel,
                                        currentUser = authState.currentUser
                                    )
                                }

                                composable(Screen.Customers.route) {
                                    currentScreen = Screen.Customers
                                    CustomersScreen(viewModel = customersViewModel)
                                }

                                composable(Screen.PosCheckout.route) {
                                    currentScreen = Screen.PosCheckout
                                    PosCheckoutScreen(
                                        viewModel = posViewModel,
                                        onCheckoutFinished = {
                                            currentScreen = Screen.Transactions
                                            navController.navigate(Screen.Transactions.route)
                                        }
                                    )
                                }

                                composable(Screen.Inventory.route) {
                                    currentScreen = Screen.Inventory
                                    InventoryScreen(
                                        viewModel = inventoryViewModel,
                                        currentUser = authState.currentUser
                                    )
                                }

                                composable(Screen.Transactions.route) {
                                    currentScreen = Screen.Transactions
                                    TransactionLogsScreen(viewModel = transactionLogsViewModel)
                                }

                                composable(Screen.Services.route) {
                                    currentScreen = Screen.Services
                                    ServiceCatalogScreen(viewModel = serviceCatalogViewModel)
                                }

                                composable(Screen.Promotions.route) {
                                    currentScreen = Screen.Promotions
                                    PromotionsScreen(
                                        viewModel = promotionsViewModel,
                                        currentUser = authState.currentUser
                                    )
                                }

                                composable(Screen.Reports.route) {
                                    currentScreen = Screen.Reports
                                    ReportsScreen(viewModel = reportsViewModel)
                                }

                                composable(Screen.Settings.route) {
                                    currentScreen = Screen.Settings
                                    SettingsScreen(
                                        viewModel = shopSetupViewModel,
                                        authViewModel = authViewModel,
                                        currentUser = authState.currentUser,
                                        onResetDone = { authViewModel.resetAuthState() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun AppPermissionRequester() {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()

        // Bluetooth Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // File / Storage Access Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        // Only request permissions that have not been granted yet
        val ungrantedPermissions = permissionsToRequest.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (ungrantedPermissions.isNotEmpty()) {
            permissionLauncher.launch(ungrantedPermissions.toTypedArray())
        }
    }
}
