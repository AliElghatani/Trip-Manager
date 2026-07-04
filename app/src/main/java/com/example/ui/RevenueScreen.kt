package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MovementOrder
import com.example.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueScreen(
    viewModel: AppViewModel,
    isArabic: Boolean,
    onMenuClicked: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val metrics by viewModel.dashboardMetrics.collectAsState()

    var devFundPercent by remember { mutableStateOf(settings.developmentFundPercent) }

    // Synchronize slider with settings when loaded
    LaunchedEffect(settings.developmentFundPercent) {
        devFundPercent = settings.developmentFundPercent
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (isArabic) "الإيرادات وتوزيع الأرباح" else "Revenue & Profit Splits",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            navigationIcon = {
                IconButton(onClick = onMenuClicked) {
                    Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.primary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Development Fund interactive Slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "صندوق التطوير المستقطع (من صافي الأرباح)" else "Development Fund Allocation (% of Net Profit)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic) {
                            "استقطاع نسبة مخصصة لإعادة الاستثمار وتطوير الأسطول."
                        } else {
                            "Reserve a customizable portion of net profits for long-term fleet growth."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${devFundPercent.toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isArabic) "المبلغ المقتطع: ${viewModel.formatCurrency(metrics.developmentFund)}" else "Reserved Cash: ${viewModel.formatCurrency(metrics.developmentFund)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00A86B)
                        )
                    }

                    Slider(
                        value = devFundPercent,
                        onValueChange = {
                            devFundPercent = it
                            viewModel.updateDevelopmentFund(it)
                        },
                        valueRange = 0f..25f,
                        steps = 24
                    )
                }
            }

            // Ledger title
            Text(
                text = if (isArabic) "التوزيع التلقائي لحسابات مركبات الأسطول" else "Automatic Splits on Registered Fleet Vehicles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val activeRevenues = orders.filter { it.status != "CANCELLED" }
            if (activeRevenues.isEmpty()) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.MonetizationOn, null, modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(if (isArabic) "لا توجد إيرادات مسجلة للتقسيم حالياً." else "No active revenues found for splits yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activeRevenues) { order ->
                        val veh = vehicles.firstOrNull { it.id == order.vehicleId }
                        val plate = veh?.plateNumber ?: "شاحنة مستقلة"
                        val ownerPct = veh?.ownerPercent ?: 65.0f
                        val vehiclePct = veh?.vehiclePercent ?: 35.0f

                        val ownerAmount = order.basePrice * (ownerPct / 100.0)
                        val vehicleAmount = order.basePrice * (vehiclePct / 100.0)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(1.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${order.orderNumber} [${order.origin} - ${order.destination}]",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "المركبة: $plate (شروط العقد: %${ownerPct.toInt()} المالك vs %${vehiclePct.toInt()} الشاحنة)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "+ ${viewModel.formatCurrency(order.basePrice)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00A86B)
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                // Split breakdown details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(if (isArabic) "حصة المالك (%${ownerPct.toInt()})" else "Owner Share (${ownerPct.toInt()}%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(viewModel.formatCurrency(ownerAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(if (isArabic) "حصة الشاحنة (%${vehiclePct.toInt()})" else "Vehicle Share (${vehiclePct.toInt()}%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(viewModel.formatCurrency(vehicleAmount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                Text(df.format(Date(order.date)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
