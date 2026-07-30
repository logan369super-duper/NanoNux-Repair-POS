package com.example.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.domain.model.PrinterConnectionType
import com.example.i18n.LocalStrings
import com.example.viewmodel.ShopSettingsViewModel
import com.example.data.local.entity.UserEntity
import com.example.domain.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHubScreen(
    viewModel: ShopSettingsViewModel,
    currentUser: UserEntity? = null,
    onNavigateToProfile: () -> Unit,
    onNavigateToUserProfile: () -> Unit,
    onNavigateToAppSettings: () -> Unit,
    onNavigateToLanguages: () -> Unit,
    onNavigateToPrinter: () -> Unit,
    onNavigateToTaxNotes: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onResetDone: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current
    var showResetDialog by remember { mutableStateOf(false) }
    val isAdmin = currentUser == null || currentUser.role == UserRole.ADMIN

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wipe All Store Data & Accounts?")
                }
            },
            text = { Text("This will permanently wipe all local database tables, repair tickets, inventory, transactions, user accounts, and settings. You will be returned to the initial onboarding screen.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetStoreData { onResetDone() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_wipe_data_button")
                ) {
                    Text("Wipe All Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Store Header Preview Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(64.dp)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    if (!state.logoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = state.logoUri,
                            contentDescription = "Shop Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "Shop Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.shopName.ifBlank { "My Repair Store" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (state.address.isNotBlank()) {
                        Text(
                            text = state.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (state.phone.isNotBlank()) {
                        Text(
                            text = "Tel: ${state.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isAdmin) {
                    AssistChip(
                        onClick = onNavigateToProfile,
                        label = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.testTag("edit_header_profile_chip")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "STORE SETTINGS CATEGORIES",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Shop Profile Card
        if (isAdmin) {
            SettingsCategoryNavCard(
                title = strings.shopProfile,
                subtitle = "Shop Name, Address, Contact Phone & Store Logo",
                statusBadge = if (state.shopName.isNotBlank()) "Configured" else "Action Needed",
                badgeColor = if (state.shopName.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                icon = Icons.Filled.Storefront,
                onClick = onNavigateToProfile,
                testTag = "nav_card_shop_profile"
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // My Profile & PIN Settings Card
        SettingsCategoryNavCard(
            title = "My Profile & PIN",
            subtitle = if (isAdmin) "View your account details and update your security PIN" else "View your profile (PIN changes restricted to Admin)",
            statusBadge = if (isAdmin) "Admin" else "Staff View",
            badgeColor = if (isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            icon = Icons.Filled.Person,
            onClick = onNavigateToUserProfile,
            testTag = "nav_card_user_profile"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Application Settings Card (Theme Preferences)
        SettingsCategoryNavCard(
            title = strings.applicationSettings,
            subtitle = "Switch between Dark Mode and Light Mode theme",
            statusBadge = if (state.isDarkMode) "Dark Mode" else "Light Mode",
            badgeColor = MaterialTheme.colorScheme.secondary,
            icon = Icons.Filled.Palette,
            onClick = onNavigateToAppSettings,
            testTag = "nav_card_app_settings"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Language Settings Card
        val langDisplay = when (state.language) {
            "my" -> "Myanmar (မြန်မာ)"
            "th" -> "Thai (ภาษาไทย)"
            else -> "English (Default)"
        }
        SettingsCategoryNavCard(
            title = strings.languagesSettings,
            subtitle = strings.languageSubtitle,
            statusBadge = langDisplay,
            badgeColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Filled.Language,
            onClick = onNavigateToLanguages,
            testTag = "nav_card_language_settings"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Printer Settings Card
        val printerBadge = when (state.printerType) {
            PrinterConnectionType.NONE -> "No Printer"
            PrinterConnectionType.BLUETOOTH -> "Bluetooth (${state.paperSizeMm}mm)"
            PrinterConnectionType.WIFI -> "Wi-Fi (${state.paperSizeMm}mm)"
        }
        SettingsCategoryNavCard(
            title = strings.thermalPrinterSettings,
            subtitle = "ESC/POS interface (Bluetooth / Wi-Fi IP), paper size (58/80mm) & connection test",
            statusBadge = printerBadge,
            badgeColor = if (state.printerType != PrinterConnectionType.NONE) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
            icon = Icons.Filled.Print,
            onClick = onNavigateToPrinter,
            testTag = "nav_card_printer_settings"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Tax & Receipt Notes Card
        if (isAdmin) {
            val taxBadge = if (state.isTaxEnabled) "${state.defaultTaxRate}% Tax Enabled" else "Tax Disabled"
            SettingsCategoryNavCard(
                title = "Tax & Receipt Footer Notes",
                subtitle = "Global tax percentage rate and default policy footer notes for receipts",
                statusBadge = taxBadge,
                badgeColor = if (state.isTaxEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                icon = Icons.Filled.ReceiptLong,
                onClick = onNavigateToTaxNotes,
                testTag = "nav_card_tax_notes"
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // 5. Staff & User Accounts Card
        if (isAdmin) {
            SettingsCategoryNavCard(
                title = "Staff & User Accounts",
                subtitle = "Manage secure logins, roles, and 6-digit unique PINs",
                statusBadge = "Secure PINs",
                badgeColor = MaterialTheme.colorScheme.primary,
                icon = Icons.Filled.People,
                onClick = onNavigateToStaff,
                testTag = "nav_card_staff_settings"
            )
        } else {
            SettingsCategoryNavCard(
                title = "Staff & User Accounts",
                subtitle = "Administrator role required to manage accounts, logins, and permissions.",
                statusBadge = "Admin Locked",
                badgeColor = MaterialTheme.colorScheme.error,
                icon = Icons.Filled.Lock,
                onClick = {},
                testTag = "nav_card_staff_settings_locked"
            )
        }

        if (isAdmin) {
            Spacer(modifier = Modifier.height(28.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SYSTEM & DATA MANAGEMENT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Data Reset & Clean Install Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showResetDialog = true }
                    .testTag("wipe_data_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reset All Store Data & Accounts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Wipe database, repair orders, inventory & user accounts to re-test clean onboarding",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsCategoryNavCard(
    title: String,
    subtitle: String,
    statusBadge: String,
    badgeColor: androidx.compose.ui.graphics.Color,
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
                        modifier = Modifier.size(26.dp)
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
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusBadge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
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
