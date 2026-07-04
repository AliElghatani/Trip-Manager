package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Expense
import com.example.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    viewModel: AppViewModel,
    isArabic: Boolean,
    onMenuClicked: () -> Unit
) {
    val expenses by viewModel.expenses.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    val orders by viewModel.orders.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf("OPERATIONAL") } // OPERATIONAL, DRIVER

    // Operational Form State
    var opCategory by remember { mutableStateOf("") }
    var opQuantity by remember { mutableStateOf("1") }
    var opPrice by remember { mutableStateOf("") }

    // Driver Form State
    var selectedDriverId by remember { mutableStateOf<Long?>(null) }
    var selectedOrderId by remember { mutableStateOf<Long?>(null) }
    var driverAmount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (isArabic) "سجل المصروفات الموحد" else "Unified Expense Ledger",
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
                .padding(16.dp)
        ) {
            // Summary Ledger Header
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
                            text = if (isArabic) "إجمالي مصروفات الشركة والرحلات" else "Total Corporate & Trip Expenses",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = viewModel.formatCurrency(expenses.sumOf { it.total }),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE04F5F)
                        )
                    }
                    Icon(Icons.Default.MoneyOff, null, tint = Color(0xFFE04F5F).copy(alpha = 0.8f), modifier = Modifier.size(36.dp))
                }
            }

            // Tabs to filter or just quick action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        dialogType = "OPERATIONAL"
                        showDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isArabic) "+ تشغيلي" else "+ Operational", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        dialogType = "DRIVER"
                        showDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isArabic) "+ مالي للسائق" else "+ Driver cash", fontSize = 11.sp)
                }
            }

            // Ledger List
            Text(
                text = if (isArabic) "دفتر قيد المصروفات الجارية" else "Current Expense Journal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (expenses.isEmpty()) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.MoneyOff, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(if (isArabic) "سجل المصروفات فارغ." else "No registered expenses found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(expenses) { exp ->
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
                                    Text(
                                        text = exp.categoryOrItem,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "- ${viewModel.formatCurrency(exp.total)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE04F5F)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                val typeDesc = if (exp.type == "OPERATIONAL") {
                                    if (isArabic) "نوع: تشغيلي ومشتريات (كمية: ${exp.quantity} • سعر: د.ل ${exp.unitPrice})" else "Type: Corp Operational (Qty: ${exp.quantity} • Unit: LYD ${exp.unitPrice})"
                                } else {
                                    val drName = drivers.firstOrNull { it.id == exp.driverId }?.name ?: "سائق مجهول"
                                    val ordNum = orders.firstOrNull { it.id == exp.movementOrderId }?.orderNumber ?: "رحلة مستقلة"
                                    if (isArabic) "نوع: عهدة سائق ($drName • أمر حركة: $ordNum)" else "Type: Driver trip cash ($drName • Order: $ordNum)"
                                }
                                Text(
                                    text = typeDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                                Text(
                                    text = df.format(Date(exp.date)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        if (dialogType == "OPERATIONAL") {
                            if (isArabic) "قيد مصروفات تشغيلية" else "Record Corporate Expense"
                        } else {
                            if (isArabic) "صرف عهدة مالية لسائق" else "Record Driver Trip Expense"
                        }
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (dialogType == "OPERATIONAL") {
                            OutlinedTextField(
                                value = opCategory,
                                onValueChange = { opCategory = it },
                                label = { Text(if (isArabic) "بيان البند / المشتريات" else "Expense Category / Item") },
                                placeholder = { Text("مثال: إطارات شاحنات") },
                                singleLine = true
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = opQuantity,
                                    onValueChange = { opQuantity = it },
                                    label = { Text(if (isArabic) "الكمية" else "Qty") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = opPrice,
                                    onValueChange = { opPrice = it },
                                    label = { Text(if (isArabic) "سعر الوحدة" else "Unit Price") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.5f)
                                )
                            }
                        } else {
                            // Driver selection
                            var driverExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = drivers.firstOrNull { it.id == selectedDriverId }?.name ?: "",
                                    onValueChange = {},
                                    label = { Text(if (isArabic) "اختر السائق المستلم" else "Select Driver") },
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { driverExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, null)
                                        }
                                    }
                                )
                                DropdownMenu(
                                    expanded = driverExpanded,
                                    onDismissRequest = { driverExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    drivers.forEach { dr ->
                                        DropdownMenuItem(
                                            text = { Text(dr.name) },
                                            onClick = {
                                                selectedDriverId = dr.id
                                                driverExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Order selection
                            var orderExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = orders.firstOrNull { it.id == selectedOrderId }?.orderNumber ?: "",
                                    onValueChange = {},
                                    label = { Text(if (isArabic) "ارتباط بأمر الحركة (اختياري)" else "Link Work Order (Optional)") },
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { orderExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, null)
                                        }
                                    }
                                )
                                DropdownMenu(
                                    expanded = orderExpanded,
                                    onDismissRequest = { orderExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    orders.forEach { ord ->
                                        DropdownMenuItem(
                                            text = { Text("${ord.orderNumber} [${ord.origin}-${ord.destination}]") },
                                            onClick = {
                                                selectedOrderId = ord.id
                                                orderExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = driverAmount,
                                onValueChange = { driverAmount = it },
                                label = { Text(if (isArabic) "قيمة المصروف / العهدة (د.ل)" else "Expense Amount (LYD)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (dialogType == "OPERATIONAL") {
                                val qty = opQuantity.toIntOrNull() ?: 1
                                val price = opPrice.toDoubleOrNull() ?: 0.0
                                if (opCategory.isNotBlank() && price > 0.0) {
                                    viewModel.addOperationalExpense(opCategory, qty, price)
                                    showDialog = false
                                    opCategory = ""
                                    opPrice = ""
                                    opQuantity = "1"
                                }
                            } else {
                                val amt = driverAmount.toDoubleOrNull() ?: 0.0
                                val drId = selectedDriverId
                                if (drId != null && amt > 0.0) {
                                    viewModel.addDriverExpense(drId, selectedOrderId, amt)
                                    showDialog = false
                                    selectedDriverId = null
                                    selectedOrderId = null
                                    driverAmount = ""
                                }
                            }
                        }
                    ) {
                        Text(if (isArabic) "تسجيل قيد" else "Save Entry")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(if (isArabic) "إلغاء" else "Cancel")
                    }
                }
            )
        }
    }
}
