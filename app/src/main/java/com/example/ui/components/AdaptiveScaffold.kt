package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.entity.UserEntity
import com.example.domain.model.UserRole
import com.example.i18n.LocalStrings
import com.example.ui.navigation.Screen
import com.example.ui.navigation.mainNavigationItems
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveScaffold(
    currentScreen: Screen,
    currentUser: UserEntity?,
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    if (isTablet) {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(
                    modifier = Modifier.width(280.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surface
                ) {
                    DrawerHeader(
                        currentUser = currentUser,
                        onLogout = onLogout
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "SIDE NAVIGATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )

                        mainNavigationItems.forEach { screen ->
                            if (!screen.adminOnly || currentUser?.role == UserRole.ADMIN) {
                                val selected = currentScreen.route == screen.route
                                NavigationDrawerItem(
                                    label = {
                                        Text(
                                            text = screen.getLocalizedTitle(),
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    selected = selected,
                                    onClick = { onNavigate(screen) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) screen.activeIcon else screen.inactiveIcon,
                                            contentDescription = screen.title
                                        )
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier
                                        .padding(vertical = 2.dp)
                                        .testTag("nav_item_${screen.route}")
                                )
                            }
                        }
                    }
                }
            }
        ) {
            MainScaffoldContent(
                currentScreen = currentScreen,
                currentUser = currentUser,
                isTablet = true,
                onOpenDrawer = {},
                onLogout = onLogout,
                content = content
            )
        }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(280.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surface
                ) {
                    DrawerHeader(
                        currentUser = currentUser,
                        onLogout = onLogout
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "NAVIGATION MENU",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )

                        mainNavigationItems.forEach { screen ->
                            if (!screen.adminOnly || currentUser?.role == UserRole.ADMIN) {
                                val selected = currentScreen.route == screen.route
                                NavigationDrawerItem(
                                    label = {
                                        Text(
                                            text = screen.getLocalizedTitle(),
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    selected = selected,
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        onNavigate(screen)
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) screen.activeIcon else screen.inactiveIcon,
                                            contentDescription = screen.title
                                        )
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier
                                        .padding(vertical = 2.dp)
                                        .testTag("nav_item_${screen.route}")
                                )
                            }
                        }
                    }
                }
            }
        ) {
            MainScaffoldContent(
                currentScreen = currentScreen,
                currentUser = currentUser,
                isTablet = false,
                onOpenDrawer = {
                    coroutineScope.launch { drawerState.open() }
                },
                onLogout = onLogout,
                content = content
            )
        }
    }
}

@Composable
private fun DrawerHeader(
    currentUser: UserEntity?,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "NanoNux Application Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Column {
                Text(
                    text = "NanoNux",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Repair POS System",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (currentUser != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("drawer_logout_card")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (currentUser.role == UserRole.ADMIN) Icons.Filled.Lock else Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = currentUser.name,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Role: ${currentUser.role.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Logout",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffoldContent(
    currentScreen: Screen,
    currentUser: UserEntity?,
    isTablet: Boolean,
    onOpenDrawer: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (!isTablet) {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("open_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Open Navigation Menu"
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = currentScreen.getLocalizedTitle(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (currentUser != null) {
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier.testTag("top_bar_logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}

@Composable
fun Screen.getLocalizedTitle(): String {
    val strings = LocalStrings.current
    return when (this) {
        Screen.ServicesRepairs -> "Services & Repairs"
        Screen.Dashboard -> strings.dashboard
        Screen.Intake -> strings.newRepairTicket
        Screen.Orders -> strings.repairTickets
        Screen.PosCheckout -> strings.posCheckout
        Screen.Inventory -> strings.inventory
        Screen.Services -> strings.serviceCatalog
        Screen.Reports -> strings.reports
        Screen.Settings -> strings.settings
        else -> this.title
    }
}
