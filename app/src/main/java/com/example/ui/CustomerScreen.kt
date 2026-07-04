package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.Customer
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    viewModel: AppViewModel,
    isArabic: Boolean,
    onMenuClicked: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val payments by viewModel.payments.collectAsState()

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (selectedCustomer == null) {
                        if (isArabic) "دفتر ذمم وديون العملاء" else "Accounts Receivable"
                    } else {
                        if (isArabic) "كشف حساب: ${selectedCustomer?.name}" else "Statement: ${selectedCustomer?.name}"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            navigationIcon = {
                if (selectedCustomer != null) {
                    IconButton(onClick = { selectedCustomer = null }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    IconButton(onClick = onMenuClicked) {
                        Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedCustomer == null) {
                // Master view: lists all customers with their total debts
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Cumulative Debts Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(1.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isArabic) "إجمالي ديون العملاء المستحقة" else "Cumulative Accounts Receivable",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val totalDebts = viewModel.dashboardMetrics.collectAsState().value.customerDebts
                                Text(
                                    text = viewModel.formatCurrency(totalDebts),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37)
                                )
                            }
                            Icon(Icons.Default.HourglassEmpty, null, tint = Color(0xFFD4AF37).copy(alpha = 0.8f), modifier = Modifier.size(36.dp))
                        }
                    }

                    Text(
                        text = if (isArabic) "قائمة العملاء والذمم المفتوحة" else "Customer Ledgers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (customers.isEmpty()) {
                        Column(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.People, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(if (isArabic) "سجل العملاء فارغ." else "No customers registered yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(customers) { customer ->
                                // Calculate debts specifically for this customer
                                val custOrders = orders.filter { it.customerId == customer.id && it.status != "CANCELLED" }
                                val totalBilled = custOrders.sumOf { it.basePrice }
                                val custPayments = payments.filter { it.customerId == customer.id }
                                val totalPaid = custPayments.sumOf { it.amountPaid }
                                val totalDiscount = custPayments.sumOf { it.discount }
                                val netDebt = (totalBilled - totalPaid - totalDiscount).coerceAtLeast(0.0)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedCustomer = customer },
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                    elevation = CardDefaults.cardElevation(1.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = customer.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "الهاتف: ${customer.phone}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = if (isArabic) "الذمة المستحقة" else "Balance Due",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = viewModel.formatCurrency(netDebt),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (netDebt > 0.0) Color(0xFFD4AF37) else Color(0xFF00A86B)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Detail view: Individual Account Statement
                val customer = selectedCustomer!!
                val custOrders = orders.filter { it.customerId == customer.id && it.status != "CANCELLED" }
                val totalBilled = custOrders.sumOf { it.basePrice }
                val custPayments = payments.filter { it.customerId == customer.id }
                val totalPaid = custPayments.sumOf { it.amountPaid }
                val totalDiscount = custPayments.sumOf { it.discount }
                val netDebt = (totalBilled - totalPaid - totalDiscount).coerceAtLeast(0.0)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(1.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = customer.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(text = "هاتف: ${customer.phone}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(if (isArabic) "إجمالي الفواتير" else "Total Billed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(viewModel.formatCurrency(totalBilled), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text(if (isArabic) "إجمالي المدفوع" else "Total Settled", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(viewModel.formatCurrency(totalPaid), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF00A86B))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(if (isArabic) "الصافي المستحق" else "Remaining Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(viewModel.formatCurrency(netDebt), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                                }
                            }
                        }
                    }

                    Text(if (isArabic) "تفاصيل فواتير وحركات العميل" else "Historic Trip Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    if (custOrders.isEmpty()) {
                        Column(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(if (isArabic) "لا توجد معاملات مسجلة." else "No transactions on file for this account.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(custOrders) { ord ->
                                val linkedPayment = custPayments.firstOrNull { it.movementOrderId == ord.id }
                                val discount = linkedPayment?.discount ?: 0.0
                                val amtPaid = linkedPayment?.amountPaid ?: 0.0

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = ord.orderNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            OrderStatusBadge(status = ord.status, isArabic = isArabic)
                                        }
                                        Text(text = "مسار: ${ord.origin} ➔ ${ord.destination}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "مبلغ: ${viewModel.formatCurrency(ord.basePrice)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (discount > 0.0) {
                                                Text(text = "خصم: -${viewModel.formatCurrency(discount)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE04F5F))
                                            }
                                            Text(text = "مدفوع: ${viewModel.formatCurrency(amtPaid)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF00A86B))
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
}
