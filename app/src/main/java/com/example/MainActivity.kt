package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Instantiate central State Management
            val viewModel: AppViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()
            val isArabic = viewModel.isArabic

            // Apply global dynamic Material 3 Theme with selected Palettes, Modes and Fonts
            MyApplicationTheme(
                paletteIndex = settings.themePaletteIndex,
                themeMode = settings.themeMode,
                fontName = settings.fontName
            ) {
                // Layout Direction Wrapper based on Language selection (Arabic default)
                CompositionLocalProvider(
                    LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
                ) {
                    if (!settings.isSetupCompleted) {
                        // Display Setup Wizard first if app is launched for the first time
                        SetupWizardScreen(
                            onSetupCompleted = { owner, phone, email, bName, bBranch, accName, accNum, palette, mode, font ->
                                viewModel.completeSetup(owner, phone, email, bName, bBranch, accName, accNum, palette, mode, font)
                            },
                            isArabic = isArabic
                        )
                    } else {
                        // Main Application View
                        var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
                        var drawerOpen by remember { mutableStateOf(false) }

                        AppNavigationDrawer(
                            isOpen = drawerOpen,
                            onClose = { drawerOpen = it },
                            currentScreen = currentScreen,
                            onScreenSelected = { currentScreen = it },
                            isArabic = isArabic
                        ) {
                            Scaffold(
                                modifier = Modifier.fillMaxSize()
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    when (currentScreen) {
                                        Screen.Dashboard -> DashboardScreen(
                                            onNavigateToAssistant = { currentScreen = Screen.AssistantBot },
                                            viewModel = viewModel,
                                            isArabic = isArabic,
                                            onMenuClicked = { drawerOpen = true }
                                        )
                                        Screen.MovementOrders -> MovementOrderScreen(
                                            viewModel = viewModel,
                                            isArabic = isArabic,
                                            onMenuClicked = { drawerOpen = true }
                                        )
                                        Screen.Fleet -> FleetScreen(
                                            viewModel = viewModel,
                                            isArabic = isArabic,
                                            onMenuClicked = { drawerOpen = true }
                                        )
                                        Screen.Expenses -> ExpenseScreen(
                                            viewModel = viewModel,
                                            isArabic = isArabic,
                                            onMenuClicked = { drawerOpen = true }
                                        )
                                        Screen.Revenues -> RevenueScreen(
                                            viewModel = viewModel,
                                            isArabic = isArabic,
                                            onMenuClicked = { drawerOpen = true }
                                        )
                                        Screen.Customers -> CustomerScreen(
                                            viewModel = viewModel,
                                            isArabic = isArabic,
                                            onMenuClicked = { drawerOpen = true }
                                        )
                                        Screen.AssistantBot -> LogisticsBotScreen(
                                            viewModel = viewModel,
                                            isArabic = isArabic,
                                            onMenuClicked = { drawerOpen = true }
                                        )
                                        Screen.Billing -> BillingInvoiceScreen(
                                            viewModel = viewModel,
                                            isArabic = isArabic,
                                            onMenuClicked = { drawerOpen = true }
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
