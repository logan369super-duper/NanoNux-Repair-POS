package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.local.entity.UserEntity
import com.example.domain.model.UserRole
import com.example.ui.screens.repair.RepairSpecsSettingsScreen
import com.example.viewmodel.ServiceCatalogViewModel
import com.example.viewmodel.ServiceOrderViewModel
import com.example.viewmodel.ShopSetupViewModel

object ServicesRepairsSubRoute {
    const val HUB = "services_repairs_hub"
    const val INTAKE = "services_repairs_intake"
    const val ORDERS = "services_repairs_orders"
    const val CATALOG = "services_repairs_catalog"
    const val BRANDS = "services_repairs_brands"
    const val PRESETS = "services_repairs_presets"
}

@Composable
fun ServicesAndRepairsScreen(
    serviceOrderViewModel: ServiceOrderViewModel,
    serviceCatalogViewModel: ServiceCatalogViewModel,
    shopSetupViewModel: ShopSetupViewModel,
    currentUser: UserEntity?
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ServicesRepairsSubRoute.HUB,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(ServicesRepairsSubRoute.HUB) {
            ServicesRepairsHubScreen(
                currentUser = currentUser,
                onNavigateToIntake = { navController.navigate(ServicesRepairsSubRoute.INTAKE) },
                onNavigateToOrders = { navController.navigate(ServicesRepairsSubRoute.ORDERS) },
                onNavigateToCatalog = { navController.navigate(ServicesRepairsSubRoute.CATALOG) },
                onNavigateToBrands = { navController.navigate(ServicesRepairsSubRoute.BRANDS) },
                onNavigateToPresets = { navController.navigate(ServicesRepairsSubRoute.PRESETS) }
            )
        }

        composable(ServicesRepairsSubRoute.INTAKE) {
            ServiceOrderIntakeScreen(
                viewModel = serviceOrderViewModel,
                onViewTicketList = {
                    navController.navigate(ServicesRepairsSubRoute.ORDERS) {
                        popUpTo(ServicesRepairsSubRoute.HUB)
                    }
                }
            )
        }

        composable(ServicesRepairsSubRoute.ORDERS) {
            ServiceOrderListScreen(
                viewModel = serviceOrderViewModel,
                onNavigateToIntake = {
                    navController.navigate(ServicesRepairsSubRoute.INTAKE)
                },
                onNavigateToBrands = {
                    navController.navigate(ServicesRepairsSubRoute.BRANDS)
                },
                onNavigateToRepairSpecs = {
                    navController.navigate(ServicesRepairsSubRoute.PRESETS)
                }
            )
        }

        composable(ServicesRepairsSubRoute.CATALOG) {
            ServiceCatalogScreen(viewModel = serviceCatalogViewModel)
        }

        composable(ServicesRepairsSubRoute.BRANDS) {
            BrandManagementScreen(
                viewModel = shopSetupViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(ServicesRepairsSubRoute.PRESETS) {
            RepairSpecsSettingsScreen(
                viewModel = shopSetupViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesRepairsHubScreen(
    currentUser: UserEntity?,
    onNavigateToIntake: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToBrands: () -> Unit,
    onNavigateToPresets: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Hero Banner Card for Services & Repairs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Handyman,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Services & Repairs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Manage intake tickets, repair workflows, catalog of services, manufacturers, and preset issues.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "REPAIR WORKFLOWS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. New Repair Ticket Card
        ServicesCategoryCard(
            title = "New Repair Ticket",
            subtitle = "Register a new intake ticket, scan or type IMEI/barcode",
            icon = Icons.Filled.AddCircle,
            onClick = onNavigateToIntake,
            testTag = "nav_card_new_repair_ticket"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Repair Tickets Card
        ServicesCategoryCard(
            title = "Repair Tickets",
            subtitle = "View active repair jobs, update status, and manage tasks",
            icon = Icons.Filled.ConfirmationNumber,
            onClick = onNavigateToOrders,
            testTag = "nav_card_repair_tickets_list"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "CATALOG & CONFIGURATIONS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Service Catalog Card (Admin only or all, with adminOnly check if needed)
        val isAdmin = currentUser?.role == UserRole.ADMIN
        ServicesCategoryCard(
            title = "Service Catalog",
            subtitle = "Configure services, categories, diagnostics, and baseline charges",
            icon = Icons.Filled.Handyman,
            onClick = onNavigateToCatalog,
            testTag = "nav_card_service_catalog_config"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Manage Brands Card
        ServicesCategoryCard(
            title = "Manage Brands",
            subtitle = "Configure list of supported device brands & manufacturers",
            icon = Icons.Filled.Category,
            onClick = onNavigateToBrands,
            testTag = "nav_card_manage_brands"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Manage Presets Card
        ServicesCategoryCard(
            title = "Manage Presets",
            subtitle = "Set up reusable fault/issue descriptions & fast select tags",
            icon = Icons.Filled.Build,
            onClick = onNavigateToPresets,
            testTag = "nav_card_manage_presets"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ServicesCategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
