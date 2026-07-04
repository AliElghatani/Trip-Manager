package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

sealed class Screen(val route: String, val titleAr: String, val titleEn: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "الرئيسية", "Dashboard", Icons.Default.Dashboard)
    object MovementOrders : Screen("orders", "أوامر الحركة", "Movement Orders", Icons.Default.LocalShipping)
    object Fleet : Screen("fleet", "الأسطول والسائقين", "Fleet & Drivers", Icons.Default.DirectionsCar)
    object Expenses : Screen("expenses", "المصروفات", "Expenses Ledger", Icons.Default.MoneyOff)
    object Revenues : Screen("revenues", "توزيع الأرباح", "Revenue Splits", Icons.Default.MonetizationOn)
    object Customers : Screen("customers", "حسابات العملاء", "Customers Ledger", Icons.Default.People)
    object Billing : Screen("billing", "الفواتير والدفع", "Billing & Invoices", Icons.Default.ReceiptLong)
    object AssistantBot : Screen("bot", "المساعد اللوجستي", "Logistics Assistant", Icons.Default.SmartToy)
}

@Composable
fun AppNavigationDrawer(
    isOpen: Boolean,
    onClose: BooleanLambda,
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    isArabic: Boolean,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Main Screen Content
        content()

        // Semitransparent dim background overlay when drawer is open
        if (isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onClose() }
                    .zIndex(99f)
            )
        }

        // Custom Navigation Drawer Sliding from Right
        AnimatedVisibility(
            visible = isOpen,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(durationMillis = 250)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(durationMillis = 250)
            ),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(280.dp)
                .zIndex(100f)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
            ) {
                // Layout Direction Wrapper to ensure proper alignment inside drawer
                CompositionLocalProvider(
                    LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .padding(16.dp)
                    ) {
                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "مسار اللوجستي" else "Masar Logistics",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (isArabic) "إدارة الأسطول والرحلات" else "Fleet & Trip Manager",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Navigation Items
                        val screens = listOf(
                            Screen.Dashboard,
                            Screen.MovementOrders,
                            Screen.Fleet,
                            Screen.Expenses,
                            Screen.Revenues,
                            Screen.Customers,
                            Screen.Billing,
                            Screen.AssistantBot
                        )

                        screens.forEach { screen ->
                            val isSelected = currentScreen == screen
                            NavigationItem(
                                title = if (isArabic) screen.titleAr else screen.titleEn,
                                icon = screen.icon,
                                isSelected = isSelected,
                                onClick = {
                                    onScreenSelected(screen)
                                    onClose()
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Swipe Drawer Edge Handle Indicator (Subtle grab-handle at the right edge)
        if (!isOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(16.dp)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            if (delta < -5) {
                                onClose(true) // trigger open (sliding left decreases delta)
                            }
                        }
                    )
                    .zIndex(50f),
                contentAlignment = Alignment.CenterEnd
            ) {
                // Handle visual pointer
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(60.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(2.dp)
                        )
                        .padding(end = 4.dp)
                )
            }
        }
    }
}

// Custom functional interface type to allow boolean updates
typealias BooleanLambda = (Boolean) -> Unit

// Helper to provide a parameterless onClose invocation
operator fun BooleanLambda.invoke() = this(false)

@Composable
fun NavigationItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f) else Color.Transparent,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) else null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp
            )
        }
    }
}
