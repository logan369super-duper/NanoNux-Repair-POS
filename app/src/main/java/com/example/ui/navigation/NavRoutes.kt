package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    val adminOnly: Boolean = false,
    val shortTitle: String = title
) {
    object Auth : Screen("auth", "Auth PIN", Icons.Filled.Lock, Icons.Outlined.Lock, shortTitle = "Auth")
    object Setup : Screen("setup", "Shop Setup", Icons.Filled.Settings, Icons.Outlined.Settings, shortTitle = "Setup")
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, shortTitle = "Home")
    object Intake : Screen("intake", "Service Order Intake", Icons.Filled.Build, Icons.Outlined.Build, shortTitle = "Intake")
    object Orders : Screen("orders", "Repair Tickets", Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber, shortTitle = "Tickets")
    object PosCheckout : Screen("pos", "Point of Sale", Icons.Filled.PointOfSale, Icons.Outlined.PointOfSale, shortTitle = "POS")
    object Inventory : Screen("inventory", "Inventory & Stock", Icons.Filled.Inventory, Icons.Outlined.Inventory, shortTitle = "Stock")
    object Transactions : Screen("transactions", "Audit History", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, adminOnly = true, shortTitle = "Audit")
    object Services : Screen("services", "Service Catalog", Icons.Filled.Handyman, Icons.Outlined.Handyman, adminOnly = true, shortTitle = "Services")
    object Promotions : Screen("promotions", "Promotions", Icons.Filled.LocalOffer, Icons.Outlined.LocalOffer, shortTitle = "Promos")
    object Reports : Screen("reports", "Financial Reports", Icons.Filled.Assessment, Icons.Outlined.Assessment, adminOnly = true, shortTitle = "Reports")
    object Brands : Screen("brands", "Brand Management", Icons.Filled.Category, Icons.Outlined.Category, shortTitle = "Brands")
    object Customers : Screen("customers", "Customers", Icons.Filled.People, Icons.Outlined.People, shortTitle = "Customers")
    object ServicesRepairs : Screen("services_repairs", "Services & Repairs", Icons.Filled.Handyman, Icons.Outlined.Handyman, shortTitle = "Repairs")
    object Settings : Screen("settings", "Settings & Printer", Icons.Filled.Settings, Icons.Outlined.Settings, shortTitle = "Settings")
    object RepairSpecs : Screen("repair_specs", "Fault Presets", Icons.Filled.Build, Icons.Outlined.Build, shortTitle = "Faults")
}

val mainNavigationItems = listOf(
    Screen.Dashboard,
    Screen.ServicesRepairs,
    Screen.Customers,
    Screen.PosCheckout,
    Screen.Inventory,
    Screen.Transactions,
    Screen.Promotions,
    Screen.Reports,
    Screen.Settings
)
