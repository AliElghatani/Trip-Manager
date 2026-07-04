package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.Driver
import com.example.data.Vehicle
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetScreen(
    viewModel: AppViewModel,
    isArabic: Boolean,
    onMenuClicked: () -> Unit
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    var tabIndex by remember { mutableStateOf(0) } // 0: Vehicles, 1: Drivers

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (isArabic) "إدارة الأسطول والسائقين" else "Fleet & Drivers",
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

        // Tab Selector
        TabRow(selectedTabIndex = tabIndex, containerColor = MaterialTheme.colorScheme.surface) {
            Tab(
                selected = tabIndex == 0,
                onClick = { tabIndex = 0 },
                text = { Text(if (isArabic) "المركبات (${vehicles.size})" else "Vehicles (${vehicles.size})") }
            )
            Tab(
                selected = tabIndex == 1,
                onClick = { tabIndex = 1 },
                text = { Text(if (isArabic) "السائقين (${drivers.size})" else "Drivers (${drivers.size})") }
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (tabIndex == 0) {
                VehiclesTab(
                    vehicles = vehicles,
                    isArabic = isArabic,
                    onAddVehicle = { plate, type, cap, ownerPct, vehPct ->
                        viewModel.addVehicle(plate, type, cap, ownerPct, vehPct)
                    },
                    onDeleteVehicle = { viewModel.deleteVehicle(it) }
                )
            } else {
                DriversTab(
                    drivers = drivers,
                    isArabic = isArabic,
                    onAddDriver = { name, phone, contractType, value ->
                        viewModel.addDriver(name, phone, contractType, value)
                    },
                    onDeleteDriver = { viewModel.deleteDriver(it) }
                )
            }
        }
    }
}

@Composable
fun VehiclesTab(
    vehicles: List<Vehicle>,
    isArabic: Boolean,
    onAddVehicle: (String, String, String, Float, Float) -> Unit,
    onDeleteVehicle: (Vehicle) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var plate by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var ownerPct by remember { mutableStateOf("65") }
    var vehiclePct by remember { mutableStateOf("35") }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (vehicles.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(16.dp))
                Text(if (isArabic) "لا توجد مركبات مسجلة حالياً." else "No vehicles registered yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vehicles) { vehicle ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                    text = vehicle.plateNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${vehicle.type} • حمولة: ${vehicle.capacity}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isArabic) {
                                        "توزيع الأرباح: المالك %${vehicle.ownerPercent} | الشاحنة %${vehicle.vehiclePercent}"
                                    } else {
                                        "Split: Owner ${vehicle.ownerPercent}% | Truck ${vehicle.vehiclePercent}%"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(onClick = { onDeleteVehicle(vehicle) }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFE04F5F))
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // Add Vehicle FAB
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, "Add Vehicle", tint = Color.White)
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (isArabic) "إضافة مركبة جديدة" else "Add New Vehicle") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = plate,
                            onValueChange = { plate = it },
                            label = { Text(if (isArabic) "رقم اللوحة / الترخيص" else "License Plate") },
                            placeholder = { Text("مثال: 16-777") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = type,
                            onValueChange = { type = it },
                            label = { Text(if (isArabic) "نوع المركبة" else "Vehicle Type") },
                            placeholder = { Text("مثال: شاحنة مرسيدس") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = capacity,
                            onValueChange = { capacity = it },
                            label = { Text(if (isArabic) "السعة / الحمولة" else "Capacity") },
                            placeholder = { Text("مثال: 40 طن") },
                            singleLine = true
                        )
                        Text(
                            text = if (isArabic) "توزيع الأرباح التلقائي للرحلة (يجب أن يساوي %100)" else "Auto Split Percentages (Must total 100%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = ownerPct,
                                onValueChange = { ownerPct = it },
                                label = { Text(if (isArabic) "المالك %" else "Owner %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = vehiclePct,
                                onValueChange = { vehiclePct = it },
                                label = { Text(if (isArabic) "المركبة %" else "Vehicle %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val oPct = ownerPct.toFloatOrNull() ?: 65f
                            val vPct = vehiclePct.toFloatOrNull() ?: 35f
                            if (plate.isNotBlank() && type.isNotBlank()) {
                                onAddVehicle(plate, type, capacity, oPct, vPct)
                                showDialog = false
                                plate = ""
                                type = ""
                                capacity = ""
                            }
                        }
                    ) {
                        Text(if (isArabic) "إضافة" else "Add")
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

@Composable
fun DriversTab(
    drivers: List<Driver>,
    isArabic: Boolean,
    onAddDriver: (String, String, String, Double) -> Unit,
    onDeleteDriver: (Driver) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedContractType by remember { mutableStateOf("FIXED_PERCENT") } // FIXED_PERCENT, MONTHLY_SALARY, FIXED_TRIP
    var contractValue by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (drivers.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.People, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(16.dp))
                Text(if (isArabic) "لا يوجد سائقين مسجلين حالياً." else "No drivers registered yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(drivers) { driver ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                    text = driver.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "الهاتف: ${driver.phone}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val typeDesc = when (driver.contractType) {
                                    "FIXED_PERCENT" -> if (isArabic) "نسبة ثابتة من الرحلة (%${driver.contractValue})" else "Fixed Trip Percentage (${driver.contractValue}%)"
                                    "MONTHLY_SALARY" -> if (isArabic) "راتب شهري ثابت (د.ل ${driver.contractValue})" else "Fixed Monthly Salary (LYD ${driver.contractValue})"
                                    else -> if (isArabic) "أجر ثابت لكل رحلة (د.ل ${driver.contractValue})" else "Fixed Rate Per Specific Trip (LYD ${driver.contractValue})"
                                }
                                Text(
                                    text = typeDesc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(onClick = { onDeleteDriver(driver) }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFE04F5F))
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // Add Driver FAB
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, "Add Driver", tint = Color.White)
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (isArabic) "إضافة سائق جديد" else "Add New Driver") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(if (isArabic) "اسم السائق بالكامل" else "Driver Full Name") },
                            placeholder = { Text("مثال: عبد المطلب محمد") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(if (isArabic) "رقم الهاتف" else "Phone Number") },
                            placeholder = { Text("مثال: 091-XXXXXXX") },
                            singleLine = true
                        )

                        Text(if (isArabic) "طبيعة العقد" else "Contract Type", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("FIXED_PERCENT", "MONTHLY_SALARY", "FIXED_TRIP").forEach { t ->
                                val label = when(t) {
                                    "FIXED_PERCENT" -> if (isArabic) "نسبة رحلة" else "Trip %"
                                    "MONTHLY_SALARY" -> if (isArabic) "راتب" else "Salary"
                                    else -> if (isArabic) "مقطوع" else "Fixed"
                                }
                                FilterChip(
                                    selected = selectedContractType == t,
                                    onClick = { selectedContractType = t },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = contractValue,
                            onValueChange = { contractValue = it },
                            label = {
                                val valLabel = when(selectedContractType) {
                                    "FIXED_PERCENT" -> if (isArabic) "النسبة المستقطعة (% - من الأرباح)" else "Percentage Rate (%)"
                                    "MONTHLY_SALARY" -> if (isArabic) "قيمة الراتب الشهري (د.ل)" else "Monthly Salary (LYD)"
                                    else -> if (isArabic) "القيمة الثابتة للرحلة (د.ل)" else "Fixed Trip Rate (LYD)"
                                }
                                Text(valLabel)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cVal = contractValue.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                onAddDriver(name, phone, selectedContractType, cVal)
                                showDialog = false
                                name = ""
                                phone = ""
                                contractValue = ""
                            }
                        }
                    ) {
                        Text(if (isArabic) "إضافة" else "Add")
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
