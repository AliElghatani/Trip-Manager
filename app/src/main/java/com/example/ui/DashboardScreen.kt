package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    isArabic: Boolean,
    onMenuClicked: () -> Unit,
    onNavigateToAssistant: () -> Unit
) {
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Custom App Bar (Action to open menu and toggles)
        TopAppBar(
            title = {
                Text(
                    text = if (isArabic) "إدارة الأسطول" else "Fleet Management",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B) // Dark Slate
                )
            },
            navigationIcon = {
                IconButton(onClick = onMenuClicked) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.toggleLanguage() }) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Banner with Business Health KPI & Financial Breakdown (matching design HTML exactly)
            BusinessHealthBanner(
                viewModel = viewModel,
                isArabic = isArabic,
                metrics = metrics
            )

            // Primary KPI grid (2x2)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    title = if (isArabic) "إجمالي الإيرادات" else "Total Revenues",
                    value = viewModel.formatCurrency(metrics.totalRevenues),
                    icon = Icons.Default.TrendingUp,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = if (isArabic) "إجمالي المصروفات" else "Total Expenses",
                    value = viewModel.formatCurrency(metrics.totalExpenses),
                    icon = Icons.Default.TrendingDown,
                    accentColor = Color(0xFFE11D48), // Rose 600
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    title = if (isArabic) "أصول الأسطول" else "Fleet Assets",
                    value = viewModel.formatCurrency(metrics.assets),
                    icon = Icons.Default.DirectionsCar,
                    accentColor = Color(0xFF10B981), // Emerald 500
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = if (isArabic) "ديون العملاء" else "Customer Debts",
                    value = viewModel.formatCurrency(metrics.customerDebts),
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = Color(0xFFD97706), // Amber 600
                    modifier = Modifier.weight(1f)
                )
            }

            // Direct Financial Analysis (Debts adjacent to Profit as requested)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "التحليل المالي المباشر" else "Direct Financial Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Net Profit Section
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isArabic) "صافي الربح" else "Net Profit",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = viewModel.formatCurrency(metrics.netProfit),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                        }

                        // Vertical Separator
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(50.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        )

                        // Debts Section (Directly adjacent to Net Profit)
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isArabic) "ديون العملاء" else "Customer Debts",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = viewModel.formatCurrency(metrics.customerDebts),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706), // Amber 600
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isArabic) {
                            "• مؤشر الأرباح والديون المتجاورة يضمن الرؤية اللحظية للمستحقات المباشرة للتأكد من سلامة التدفق النقدي."
                        } else {
                            "• Adjacent Profit & Debts view ensures real-time tracking of cash-flow health."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Smart Assistant Bot Promotion Card (Interactive)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAssistant() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Assistant Bot",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "مساعد مسار اللوجستي" else "Masar Logistics Bot",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isArabic) {
                                "اسأل عن أسعار الرحلات والمسافات واحصل على تذكير بالنواقص اليومية."
                            } else {
                                "Ask about road distances, trip pricing, and check pending fleet reminders."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Navigate",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Fleet Allocation / Quick Information Fund card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.04f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = "Development Fund",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (isArabic) "صندوق التطوير المستقطع" else "Allocated Development Fund",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isArabic) "تخصيص مستقبلي للتوسع: ${viewModel.formatCurrency(metrics.developmentFund)}" else "Future growth reserves: ${viewModel.formatCurrency(metrics.developmentFund)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessHealthBanner(
    viewModel: AppViewModel,
    isArabic: Boolean,
    metrics: AppViewModel.DashboardMetrics
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)), // slate-100 equivalent
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Current Net Profit and Growth Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isArabic) "صافي الربح الحالي" else "Current Net Profit",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B) // Slate 500
                    )
                    Text(
                        text = viewModel.formatCurrency(metrics.netProfit),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A), // Slate 900
                        fontSize = 28.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val healthPercent = metrics.businessHealthPercent
                    val isThriving = healthPercent >= 100.0 || metrics.totalExpenses == 0.0
                    val badgeBg = if (isThriving) Color(0xFFDCFCE7) else Color(0xFFFEE2E2) // Green 100 vs Red 100
                    val badgeText = if (isThriving) Color(0xFF15803D) else Color(0xFF991B1B) // Green 700 vs Red 800
                    val trendIcon = if (isThriving) Icons.Default.TrendingUp else Icons.Default.TrendingDown
                    val statusText = if (isThriving) {
                        if (isArabic) "مزدهر 📈" else "Thriving 📈"
                    } else {
                        if (isArabic) "متراجع ⚠️" else "Declining ⚠️"
                    }

                    // Growth Badge
                    Row(
                        modifier = Modifier
                            .background(badgeBg, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.1f", healthPercent)}%",
                            color = badgeText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Icon(
                            imageVector = trendIcon,
                            contentDescription = null,
                            tint = badgeText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "${if (isArabic) "صحة الأعمال" else "Business Health"}: $statusText",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Dual Progress Bar (representing visual division of finances)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFF1F5F9)) // Slate 100
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Safe operational profit ratio (represented by Indigo)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.7f)
                            .background(Color(0xFF4F46E5)) // Indigo 500
                    )
                    // Secondary safety margin (represented by lighter Indigo)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.3f)
                            .background(Color(0xFFC7D2FE)) // Indigo 200
                    )
                }
            }

            // Split metrics bottom row (Debts and Assets)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FB), shape = RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customer Debts
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "ديون العملاء" else "Customer Debts",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = viewModel.formatCurrency(metrics.customerDebts),
                        fontSize = 13.sp,
                        color = Color(0xFFD97706), // Amber 600
                        fontWeight = FontWeight.Bold
                    )
                }

                // Vertical Separator
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(Color(0xFFE2E8F0))
                )

                // Fleet Assets Valuation
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (isArabic) "أصول الأسطول" else "Fleet Assets",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = viewModel.formatCurrency(metrics.assets),
                        fontSize = 13.sp,
                        color = Color(0xFF334155), // Slate 700
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF64748B) // Slate 500
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A), // Slate 900
                fontSize = 16.sp
            )
        }
    }
}
