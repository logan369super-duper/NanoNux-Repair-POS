package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.settings.ApplicationSettingsScreen
import com.example.ui.screens.settings.LanguageSettingsScreen
import com.example.ui.screens.settings.PrinterSettingsScreen
import com.example.ui.screens.settings.SettingsHubScreen
import com.example.ui.screens.settings.ShopProfileScreen
import com.example.ui.screens.settings.TaxAndNotesSettingsScreen
import com.example.ui.screens.settings.StaffSettingsScreen
import com.example.ui.screens.settings.UserProfileScreen
import com.example.viewmodel.ShopSettingsViewModel
import com.example.viewmodel.AuthViewModel
import com.example.data.local.entity.UserEntity

object SettingsSubRoute {
    const val HUB = "settings_hub"
    const val PROFILE = "settings_profile"
    const val USER_PROFILE = "settings_user_profile"
    const val APP_SETTINGS = "settings_app_settings"
    const val LANGUAGES = "settings_languages"
    const val PRINTER = "settings_printer"
    const val TAX_NOTES = "settings_tax_notes"
    const val STAFF = "settings_staff"
}

@Composable
fun SettingsScreen(
    viewModel: ShopSettingsViewModel,
    authViewModel: AuthViewModel,
    currentUser: UserEntity? = null,
    onResetDone: () -> Unit = {}
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SettingsSubRoute.HUB,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(SettingsSubRoute.HUB) {
            SettingsHubScreen(
                viewModel = viewModel,
                currentUser = currentUser,
                onNavigateToProfile = { navController.navigate(SettingsSubRoute.PROFILE) },
                onNavigateToUserProfile = { navController.navigate(SettingsSubRoute.USER_PROFILE) },
                onNavigateToAppSettings = { navController.navigate(SettingsSubRoute.APP_SETTINGS) },
                onNavigateToLanguages = { navController.navigate(SettingsSubRoute.LANGUAGES) },
                onNavigateToPrinter = { navController.navigate(SettingsSubRoute.PRINTER) },
                onNavigateToTaxNotes = { navController.navigate(SettingsSubRoute.TAX_NOTES) },
                onNavigateToStaff = { navController.navigate(SettingsSubRoute.STAFF) },
                onResetDone = onResetDone
            )
        }

        composable(SettingsSubRoute.USER_PROFILE) {
            UserProfileScreen(
                currentUser = currentUser,
                onUpdatePin = { newPin, onSuccess, onError ->
                    authViewModel.updateCurrentUserPin(newPin, onSuccess, onError)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(SettingsSubRoute.PROFILE) {
            ShopProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(SettingsSubRoute.APP_SETTINGS) {
            ApplicationSettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onResetDone = onResetDone
            )
        }

        composable(SettingsSubRoute.LANGUAGES) {
            LanguageSettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(SettingsSubRoute.PRINTER) {
            PrinterSettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(SettingsSubRoute.TAX_NOTES) {
            TaxAndNotesSettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(SettingsSubRoute.STAFF) {
            StaffSettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

