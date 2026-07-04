package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.data.Customer
import com.example.data.Driver
import com.example.data.MovementOrder
import com.example.data.Vehicle
import com.example.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementOrderScreen(
    viewModel: AppViewModel,
    isArabic: Boolean,
    onMenuClicked: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    val customers by viewModel.customers.collectAsState()

    var showForm by remember { mutableStateOf(false) }

    // Form Fields State
    var movementType by remember { mutableStateOf("INTERNAL") } // INTERNAL, EXTERNAL
    var selectedCustomerId by remember { mutableStateOf<Long?>(null) }
    var customerSearchText by remember { mutableStateOf("") }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    var selectedVehicleId by remember { mutableStateOf<Long?>(null) }
    var vehicleSearchText by remember { mutableStateOf("") }
    var vehicleDropdownExpanded by remember { mutableStateOf(false) }
    var autoVehicleType by remember { mutableStateOf("") }
    var autoVehicleCapacity by remember { mutableStateOf("") }

    var selectedDriverId by remember { mutableStateOf<Long?>(null) }
    var driverSearchText by remember { mutableStateOf("") }
    var driverDropdownExpanded by remember { mutableStateOf(false) }

    var origin by remember { mutableStateOf("") }
    var originDropdownExpanded by remember { mutableStateOf(false) }
    var destination by remember { mutableStateOf("") }
    var destDropdownExpanded by remember { mutableStateOf(false) }

    var tripType by remember { mutableStateOf("ONE_WAY") } // ONE_WAY, ROUND_TRIP
    var returnDetails by remember { mutableStateOf("") }
    var basePrice by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("UNPAID") } // UNPAID, PARTIALLY_PAID, FULLY_PAID, CANCELLED
    var notes by remember { mutableStateOf("") }

    // Simple Customer Addition Dialog state
    var showNewCustDialog by remember { mutableStateOf(false) }
    var newCustName by remember { mutableStateOf("") }
    var newCustPhone by remember { mutableStateOf("") }

    val popularCities = viewModel.getPopularLibyanDestinations()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (isArabic) "أوامر الحركة التشغيلية" else "Movement Work Orders",
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

        if (showForm) {
            // Form View
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isArabic) "إنشاء أمر حركة (أمر تشغيل)" else "Create Order (أمر حركة)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Type Toggle (Internal/External)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("INTERNAL", "EXTERNAL").forEach { t ->
                        val isSel = movementType == t
                        Surface(
                            onClick = { movementType = t },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (t == "INTERNAL") (if (isArabic) "داخلية (Local)" else "Internal") else (if (isArabic) "خارجية (International)" else "External"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Autocomplete Customer Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = customerSearchText,
                        onValueChange = {
                            customerSearchText = it
                            customerDropdownExpanded = true
                            selectedCustomerId = null
                        },
                        label = { Text(if (isArabic) "العميل المستهدف *" else "Target Customer *") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showNewCustDialog = true }) {
                                Icon(Icons.Default.Add, "Add Customer")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = customerDropdownExpanded && customerSearchText.isNotEmpty(),
                        onDismissRequest = { customerDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        val filteredCusts = customers.filter { it.name.contains(customerSearchText, ignoreCase = true) }
                        if (filteredCusts.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(if (isArabic) "+ إضافة كعميل جديد" else "+ Add New Customer") },
                                onClick = {
                                    newCustName = customerSearchText
                                    showNewCustDialog = true
                                    customerDropdownExpanded = false
                                }
                            )
                        } else {
                            filteredCusts.forEach { cust ->
                                DropdownMenuItem(
                                    text = { Text("${cust.name} (${cust.phone})") },
                                    onClick = {
                                        selectedCustomerId = cust.id
                                        customerSearchText = cust.name
                                        customerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Autocomplete Vehicle Selector & Auto Fetch
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = vehicleSearchText,
                        onValueChange = {
                            vehicleSearchText = it
                            vehicleDropdownExpanded = true
                            selectedVehicleId = null
                        },
                        label = { Text(if (isArabic) "لوحة المركبة المخصصة *" else "Assigned Vehicle Plate *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = vehicleDropdownExpanded && vehicleSearchText.isNotEmpty(),
                        onDismissRequest = { vehicleDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        val filteredVehs = vehicles.filter { it.plateNumber.contains(vehicleSearchText, ignoreCase = true) }
                        filteredVehs.forEach { veh ->
                            DropdownMenuItem(
                                text = { Text("${veh.plateNumber} [${veh.type}]") },
                                onClick = {
                                    selectedVehicleId = veh.id
                                    vehicleSearchText = veh.plateNumber
                                    autoVehicleType = veh.type
                                    autoVehicleCapacity = veh.capacity
                                    vehicleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Auto-populated fields (editable as requested)
                if (selectedVehicleId != null) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = autoVehicleType,
                            onValueChange = { autoVehicleType = it },
                            label = { Text(if (isArabic) "نوع الهيكل" else "Structure Type") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = autoVehicleCapacity,
                            onValueChange = { autoVehicleCapacity = it },
                            label = { Text(if (isArabic) "السعة / الحمولة" else "Capacity") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Autocomplete Driver Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = driverSearchText,
                        onValueChange = {
                            driverSearchText = it
                            driverDropdownExpanded = true
                            selectedDriverId = null
                        },
                        label = { Text(if (isArabic) "السائق المكلف بالرحلة *" else "Assigned Driver *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = driverDropdownExpanded && driverSearchText.isNotEmpty(),
                        onDismissRequest = { driverDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        val filteredDrivers = drivers.filter { it.name.contains(driverSearchText, ignoreCase = true) }
                        filteredDrivers.forEach { dr ->
                            DropdownMenuItem(
                                text = { Text(dr.name) },
                                onClick = {
                                    selectedDriverId = dr.id
                                    driverSearchText = dr.name
                                    driverDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Routing Origin & Destination with popular Libyan cities typeahead
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = origin,
                            onValueChange = {
                                origin = it
                                originDropdownExpanded = true
                            },
                            label = { Text(if (isArabic) "نقطة الانطلاق (من) *" else "Origin (From) *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = originDropdownExpanded && origin.isNotEmpty(),
                            onDismissRequest = { originDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.45f)
                        ) {
                            popularCities.filter { it.contains(origin, true) }.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city) },
                                    onClick = {
                                        origin = city
                                        originDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = destination,
                            onValueChange = {
                                destination = it
                                destDropdownExpanded = true
                            },
                            label = { Text(if (isArabic) "نقطة الوصول (إلى) *" else "Destination (To) *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = destDropdownExpanded && destination.isNotEmpty(),
                            onDismissRequest = { destDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.45f)
                        ) {
                            popularCities.filter { it.contains(destination, true) }.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city) },
                                    onClick = {
                                        destination = city
                                        destDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Trip Type Selection (ONE_WAY / ROUND_TRIP)
                Column {
                    Text(if (isArabic) "طبيعة مسار الرحلة" else "Trip Return Mode", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ONE_WAY", "ROUND_TRIP").forEach { tr ->
                            val isSel = tripType == tr
                            Surface(
                                onClick = { tripType = tr },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (tr == "ONE_WAY") (if (isArabic) "ذهاب فقط" else "One-way") else (if (isArabic) "ذهاب وعودة (Round)" else "Round-trip"),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Reveal Return Details if Round Trip as requested
                AnimatedVisibility(visible = tripType == "ROUND_TRIP") {
                    OutlinedTextField(
                        value = returnDetails,
                        onValueChange = { returnDetails = it },
                        label = { Text(if (isArabic) "تفاصيل حمولة وعقد العودة *" else "Return Cargo & Backhaul Contract *") },
                        placeholder = { Text("مثال: حمولة طوب فارغ بنغازي-طرابلس") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Pricing & Payment Initial Status
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = basePrice,
                        onValueChange = { basePrice = it },
                        label = { Text(if (isArabic) "سعر الرحلة الإجمالي (د.ل) *" else "Total Trip Fare (LYD) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it },
                        label = { Text(if (isArabic) "حالة التحصيل" else "Collection Status") },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        trailingIcon = {
                            Box {
                                IconButton(onClick = { /* Status list trigger */ }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                        }
                    )
                }

                // Status dropdown buttons for easy selection
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("UNPAID", "PARTIALLY_PAID", "FULLY_PAID").forEach { st ->
                        val label = when(st) {
                            "UNPAID" -> if (isArabic) "غير خالص" else "Unpaid"
                            "PARTIALLY_PAID" -> if (isArabic) "جزء خالص" else "Partial"
                            else -> if (isArabic) "خالص" else "Paid"
                        }
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isArabic) "ملاحظات إضافية" else "Additional Remarks") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )

                // Actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { showForm = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isArabic) "إلغاء" else "Cancel")
                    }

                    Button(
                        onClick = {
                            val custId = selectedCustomerId
                            val vehId = selectedVehicleId
                            val drId = selectedDriverId
                            val priceVal = basePrice.toDoubleOrNull() ?: 0.0

                            if (custId != null && vehId != null && drId != null && origin.isNotBlank() && destination.isNotBlank()) {
                                viewModel.createMovementOrder(
                                    movementType = movementType,
                                    customerId = custId,
                                    vehicleId = vehId,
                                    driverId = drId,
                                    origin = origin,
                                    destination = destination,
                                    tripType = tripType,
                                    returnDetails = returnDetails,
                                    basePrice = priceVal,
                                    status = status,
                                    notes = notes
                                )
                                showForm = false
                                // reset form variables
                                customerSearchText = ""
                                vehicleSearchText = ""
                                driverSearchText = ""
                                origin = ""
                                destination = ""
                                returnDetails = ""
                                basePrice = ""
                            }
                        },
                        enabled = selectedCustomerId != null && selectedVehicleId != null && selectedDriverId != null && origin.isNotBlank() && destination.isNotBlank(),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text(if (isArabic) "حفظ وإصدار الأمر" else "Issue Movement Order")
                    }
                }
            }
        } else {
            // Trips History / Ledger View (as requested, below/paired)
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = if (isArabic) "سجل الرحلات وأوامر الحركة" else "Trip Ledger & Work Orders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (orders.isEmpty()) {
                        Column(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(if (isArabic) "لا توجد أوامر حركة مسجلة." else "No active movement orders.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(orders) { order ->
                                val cust = customers.firstOrNull { it.id == order.customerId }?.name ?: "عميل غير معروف"
                                val plate = vehicles.firstOrNull { it.id == order.vehicleId }?.plateNumber ?: "شاحنة غير معروفة"
                                val driverName = drivers.firstOrNull { it.id == order.driverId }?.name ?: "سائق غير معروف"

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
                                                text = order.orderNumber,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            OrderStatusBadge(status = order.status, isArabic = isArabic)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = "مسار: ${order.origin} ➔ ${order.destination}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "العميل: $cust | السائق: $driverName | شاحنة: $plate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                            Text(text = df.format(Date(order.date)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = viewModel.formatCurrency(order.basePrice), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Add Order FAB
                FloatingActionButton(
                    onClick = { showForm = true },
                    modifier = Modifier.align(Alignment.BottomEnd),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "Create Order", tint = Color.White)
                }
            }
        }

        // Quick Customer Creation Dialog
        if (showNewCustDialog) {
            AlertDialog(
                onDismissRequest = { showNewCustDialog = false },
                title = { Text(if (isArabic) "تسجيل عميل جديد" else "Register New Customer") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newCustName,
                            onValueChange = { newCustName = it },
                            label = { Text(if (isArabic) "اسم العميل بالكامل *" else "Customer Full Name *") }
                        )
                        OutlinedTextField(
                            value = newCustPhone,
                            onValueChange = { newCustPhone = it },
                            label = { Text(if (isArabic) "رقم الهاتف *" else "Phone Number *") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCustName.isNotBlank() && newCustPhone.isNotBlank()) {
                                viewModel.addCustomer(newCustName, newCustPhone, "")
                                customerSearchText = newCustName
                                showNewCustDialog = false
                            }
                        }
                    ) {
                        Text(if (isArabic) "تسجيل" else "Register")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewCustDialog = false }) {
                        Text(if (isArabic) "إلغاء" else "Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun OrderStatusBadge(status: String, isArabic: Boolean) {
    val (label, bg, text) = when(status) {
        "FULLY_PAID" -> Triple(if (isArabic) "خالص بالكامل" else "Paid", Color(0xFFE2F6EA), Color(0xFF00A86B))
        "PARTIALLY_PAID" -> Triple(if (isArabic) "خالص جزئي" else "Partial", Color(0xFFFEF3D6), Color(0xFFD4AF37))
        "CANCELLED" -> Triple(if (isArabic) "ملغية" else "Cancelled", Color(0xFFFDE8E8), Color(0xFFE04F5F))
        else -> Triple(if (isArabic) "غير خالص" else "Unpaid", Color(0xFFE5E7EB), Color(0xFF4B5563)) // UNPAID
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
