package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.Customer
import com.example.data.ExtraService
import com.example.data.MovementOrder
import com.example.service.PdfGenerator
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingInvoiceScreen(
    viewModel: AppViewModel,
    isArabic: Boolean,
    onMenuClicked: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val orders by viewModel.orders.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var selectedOrder by remember { mutableStateOf<MovementOrder?>(null) }
    var orderDropdownExpanded by remember { mutableStateOf(false) }

    // Invoicing States
    var discountText by remember { mutableStateOf("0") }
    var paymentMethod by remember { mutableStateOf("CASH") } // CASH, BANK_TRANSFER, SPLIT
    var cashPaidText by remember { mutableStateOf("") }
    var bankPaidText by remember { mutableStateOf("") }

    // Dynamic Extra Services
    val extraServicesList = remember { mutableStateListOf<Pair<String, Double>>() }
    var extraServiceName by remember { mutableStateOf("") }
    var extraServicePrice by remember { mutableStateOf("") }

    // Calculations
    val basePrice = selectedOrder?.basePrice ?: 0.0
    val servicesTotal = extraServicesList.sumOf { it.second }
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val finalTotal = (basePrice + servicesTotal - discount).coerceAtLeast(0.0)

    val cashAmount = if (paymentMethod == "SPLIT") (cashPaidText.toDoubleOrNull() ?: 0.0) else if (paymentMethod == "CASH") finalTotal else 0.0
    val bankAmount = if (paymentMethod == "SPLIT") (bankPaidText.toDoubleOrNull() ?: 0.0) else if (paymentMethod == "BANK_TRANSFER") finalTotal else 0.0
    val totalPaid = cashAmount + bankAmount

    // PDF generation states
    var generatedFile by remember { mutableStateOf<File?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    // Fetch existing extra services on Order Select
    LaunchedEffect(selectedOrder) {
        val order = selectedOrder
        if (order != null) {
            extraServicesList.clear()
            // Pull any existing services from database
            coroutineScope.launch {
                val dbServices = viewModel.getExtraServicesForInvoice(order.id)
                for (srv in dbServices) {
                    extraServicesList.add(Pair(srv.name, srv.price))
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (isArabic) "تسوية الفواتير والدفع والـ PDF" else "Invoicing & PDF Generation",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isArabic) "إنشاء الفاتورة المالية المعتمدة" else "Create Official Financial Invoice",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // 1. Selector for Movement Orders
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedOrder?.orderNumber ?: "",
                    onValueChange = {},
                    label = { Text(if (isArabic) "اختر أمر الحركة المطلوب تسويته *" else "Select Movement Order *") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { orderDropdownExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = orderDropdownExpanded,
                    onDismissRequest = { orderDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    orders.forEach { ord ->
                        DropdownMenuItem(
                            text = { Text("${ord.orderNumber} [${ord.origin} ➔ ${ord.destination}]") },
                            onClick = {
                                selectedOrder = ord
                                orderDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            if (selectedOrder != null) {
                val order = selectedOrder!!
                val customer = customers.firstOrNull { it.id == order.customerId } ?: Customer(name = "غير معروف", phone = "")

                // Render populated order details card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(1.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = if (isArabic) "تفاصيل أمر الحركة المسحوبة" else "Imported Work Order Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "العميل: ${customer.name}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "الهاتف: ${customer.phone}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "مسار الرحلة: من (${order.origin}) إلى (${order.destination})", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "نوع الرحلة: ${if (order.tripType == "ONE_WAY") "ذهاب فقط" else "ذهاب وعودة"}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "السعر الأساسي المعتمد: ${viewModel.formatCurrency(order.basePrice)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // 2. Extra Services Layout (Dynamic fields)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(1.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = if (isArabic) "الخدمات اللوجستية الإضافية (مثل: تنزيل، عتالة)" else "Extra Logistics Services (e.g., Unloading)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Extra services list
                        extraServicesList.forEachIndexed { index, pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "${pair.first}: + ${viewModel.formatCurrency(pair.second)}", style = MaterialTheme.typography.bodyMedium)
                                IconButton(onClick = { extraServicesList.removeAt(index) }) {
                                    Icon(Icons.Default.Close, null, tint = Color(0xFFE04F5F), modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Input fields to add services
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = extraServiceName,
                                onValueChange = { extraServiceName = it },
                                label = { Text(if (isArabic) "اسم الخدمة" else "Service Name") },
                                modifier = Modifier.weight(1.5f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = extraServicePrice,
                                onValueChange = { extraServicePrice = it },
                                label = { Text(if (isArabic) "السعر" else "Price") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Button(
                            onClick = {
                                val sPrice = extraServicePrice.toDoubleOrNull() ?: 0.0
                                if (extraServiceName.isNotBlank() && sPrice > 0.0) {
                                    extraServicesList.add(Pair(extraServiceName, sPrice))
                                    extraServiceName = ""
                                    extraServicePrice = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                        ) {
                            Text(if (isArabic) "إضافة بند" else "Add Item")
                        }
                    }
                }

                // 3. Financial Adjustments: Discount (strictly tax-free as requested)
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text(if (isArabic) "خصم مالي خاص (د.ل)" else "Financial Discount (LYD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. Payment Method Options
                Text(if (isArabic) "طريقة السداد والتحصيل" else "Settlement Method", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("CASH", "BANK_TRANSFER", "SPLIT").forEach { method ->
                        val isSel = paymentMethod == method
                        Surface(
                            onClick = { paymentMethod = method },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = when(method) {
                                        "CASH" -> if (isArabic) "نقداً" else "Cash"
                                        "BANK_TRANSFER" -> if (isArabic) "حوالة مصرفية" else "Bank"
                                        else -> if (isArabic) "دفع مشترك" else "Split"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Reveal inputs if Split Payment as requested
                AnimatedVisibility(visible = paymentMethod == "SPLIT") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cashPaidText,
                            onValueChange = { cashPaidText = it },
                            label = { Text(if (isArabic) "المدفوع نقداً" else "Cash Paid") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bankPaidText,
                            onValueChange = { bankPaidText = it },
                            label = { Text(if (isArabic) "المدفوع مصرفياً" else "Bank Paid") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Real-time financial summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(1.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = if (isArabic) "السعر الأساسي للرحلة:" else "Base Outbound Price:")
                            Text(text = viewModel.formatCurrency(basePrice))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = if (isArabic) "إجمالي الخدمات المضافة:" else "Total Extra Services:")
                            Text(text = viewModel.formatCurrency(servicesTotal))
                        }
                        if (discount > 0.0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = if (isArabic) "الخصم المباشر الممنوح:" else "Discount Granted:", color = Color(0xFFE04F5F))
                                Text(text = "- ${viewModel.formatCurrency(discount)}", color = Color(0xFFE04F5F))
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (isArabic) "المبلغ الكلي المستحق:" else "Final Balanced Total:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(text = viewModel.formatCurrency(finalTotal), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }

                        // Ledger paid indicator
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = if (isArabic) "القيمة المسجلة للسداد المباشر:" else "Direct Recorded Settlement:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = viewModel.formatCurrency(totalPaid), fontSize = 11.sp, color = Color(0xFF00A86B), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 5. Actions: Save, Generate Invoice & Share PDF
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Save and Register Payment
                    Button(
                        onClick = {
                            viewModel.registerInvoicePayment(
                                order = order,
                                discount = discount,
                                extraServices = extraServicesList.map { it },
                                paymentMethod = paymentMethod,
                                cashPaid = cashAmount,
                                bankPaid = bankAmount,
                                totalPaid = totalPaid
                            )
                            Toast.makeText(context, if (isArabic) "تمت تسوية الفاتورة مالياً" else "Invoice registered successfully", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Save, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isArabic) "حفظ وحيازة الدفعة المالية" else "Save & Clear Payments")
                    }

                    // Generate and Share PDF Invoice
                    Button(
                        onClick = {
                            isGeneratingPdf = true
                            coroutineScope.launch {
                                // Save payments first
                                viewModel.registerInvoicePayment(
                                    order = order,
                                    discount = discount,
                                    extraServices = extraServicesList.map { it },
                                    paymentMethod = paymentMethod,
                                    cashPaid = cashAmount,
                                    bankPaid = bankAmount,
                                    totalPaid = totalPaid
                                )

                                val activePayment = viewModel.payments.value.firstOrNull { it.movementOrderId == order.id }

                                PdfGenerator.generateInvoicePdf(
                                    context = context,
                                    settings = settings,
                                    customer = customer,
                                    order = order,
                                    extraServices = extraServicesList.map { ExtraService(movementOrderId = order.id, name = it.first, price = it.second) },
                                    payment = activePayment,
                                    onComplete = { file ->
                                        isGeneratingPdf = false
                                        generatedFile = file
                                        if (file != null) {
                                            Toast.makeText(context, if (isArabic) "تم توليد الفاتورة بنجاح" else "Invoice PDF Generated!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, if (isArabic) "فشل في إنشاء ملف PDF" else "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A86B)),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.ReceiptLong, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isArabic) "توليد كشف الفاتورة PDF" else "Generate Invoice PDF")
                        }
                    }

                    // Share PDF triggers if generated
                    AnimatedVisibility(visible = generatedFile != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    generatedFile?.let { file ->
                                        val uri: Uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/pdf"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, if (isArabic) "مشاركة الفاتورة" else "Share Invoice PDF"))
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Icon(Icons.Default.Share, null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isArabic) "مشاركة" else "Share", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    generatedFile?.let { file ->
                                        val uri: Uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/pdf")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Icon(Icons.Default.Visibility, null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isArabic) "عرض الفاتورة" else "View", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                // Empty view prompt
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.ReceiptLong, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isArabic) "يرجى تحديد أمر حركة لاسترداد البيانات المالية وإنشاء الفاتورة." else "Please select an active Movement Order to compute invoices.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
