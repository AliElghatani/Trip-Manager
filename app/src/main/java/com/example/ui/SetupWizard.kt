package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SetupWizardScreen(
    onSetupCompleted: (ownerName: String, phone: String, email: String, bankName: String, branch: String, accName: String, accNumber: String, palette: Int, mode: String, font: String) -> Unit,
    isArabic: Boolean
) {
    var currentPage by remember { mutableStateOf(1) }
    
    // Page 1 Data: User Info
    var ownerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedLogoIndex by remember { mutableStateOf(0) } // stock avatars for Libyan operators

    // Page 2 Data: Bank Details
    var bankName by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }

    // Page 3 Data: Visual Theme Preferences
    var selectedPaletteIndex by remember { mutableStateOf(0) } // Cosmic Slate
    var selectedMode by remember { mutableStateOf("DARK") } // DARK, LIGHT, DIM
    var selectedFont by remember { mutableStateOf("Cairo") } // Cairo, Dubai, Hacen Tunisia

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Progress / Steps Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepCircle(stepNumber = 1, active = currentPage >= 1, label = if (isArabic) "المستخدم" else "User")
                HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (currentPage >= 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                StepCircle(stepNumber = 2, active = currentPage >= 2, label = if (isArabic) "البنك" else "Bank")
                HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (currentPage >= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                StepCircle(stepNumber = 3, active = currentPage >= 3, label = if (isArabic) "الهوية" else "Visual")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic Step Page Content
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (currentPage) {
                    1 -> PageUserInfo(
                        isArabic = isArabic,
                        ownerName = ownerName,
                        onOwnerNameChange = { ownerName = it },
                        phoneNumber = phoneNumber,
                        onPhoneChange = { phoneNumber = it },
                        email = email,
                        onEmailChange = { email = it },
                        selectedLogoIndex = selectedLogoIndex,
                        onLogoSelect = { selectedLogoIndex = it }
                    )
                    2 -> PageBankDetails(
                        isArabic = isArabic,
                        bankName = bankName,
                        onBankNameChange = { bankName = it },
                        branch = branch,
                        onBranchChange = { branch = it },
                        accountName = accountName,
                        onAccountNameChange = { accountName = it },
                        accountNumber = accountNumber,
                        onAccountNumberChange = { accountNumber = it }
                    )
                    3 -> PageVisualIdentity(
                        isArabic = isArabic,
                        selectedPaletteIndex = selectedPaletteIndex,
                        onPaletteSelect = { selectedPaletteIndex = it },
                        selectedMode = selectedMode,
                        onModeSelect = { selectedMode = it },
                        selectedFont = selectedFont,
                        onFontSelect = { selectedFont = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Buttons Bottom Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentPage > 1) {
                    OutlinedButton(
                        onClick = { currentPage-- },
                        modifier = Modifier.width(120.dp).height(48.dp)
                    ) {
                        Text(text = if (isArabic) "السابق" else "Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(120.dp))
                }

                Button(
                    onClick = {
                        if (currentPage < 3) {
                            if (currentPage == 1 && (ownerName.isBlank() || phoneNumber.isBlank())) {
                                // show warning / do not proceed if blank required
                            } else {
                                currentPage++
                            }
                        } else {
                            onSetupCompleted(
                                ownerName, phoneNumber, email,
                                bankName, branch, accountName, accountNumber,
                                selectedPaletteIndex, selectedMode, selectedFont
                            )
                        }
                    },
                    enabled = currentPage != 1 || (ownerName.isNotBlank() && phoneNumber.isNotBlank()),
                    modifier = Modifier.width(140.dp).height(48.dp)
                ) {
                    Text(
                        text = if (currentPage == 3) {
                            if (isArabic) "ابدأ الآن" else "Get Started"
                        } else {
                            if (isArabic) "التالي" else "Next"
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StepCircle(stepNumber: Int, active: Boolean, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PageUserInfo(
    isArabic: Boolean,
    ownerName: String,
    onOwnerNameChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    selectedLogoIndex: Int,
    onLogoSelect: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isArabic) "مرحباً بك! يرجى إدخال بيانات المالك" else "Welcome! Please enter owner details",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = if (isArabic) "هذه المعلومات ستظهر كترويسة معتمدة في كافة الفواتير وكشوفات الحساب اللوجستية." else "This information will appear dynamically as headings in all logistics statements and invoices.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Select stock fleet symbol / logo
        Text(text = if (isArabic) "رمز أسطول الشركة (اختياري)" else "Company Fleet Symbol (Optional)", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val stockIcons = listOf(Icons.Default.LocalShipping, Icons.Default.DirectionsCar, Icons.Default.BusinessCenter, Icons.Default.Dashboard)
            stockIcons.forEachIndexed { index, icon ->
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = if (selectedLogoIndex == index) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = if (selectedLogoIndex == index) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onLogoSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selectedLogoIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = ownerName,
            onValueChange = onOwnerNameChange,
            label = { Text(text = if (isArabic) "اسم المالك / الشركة المسؤول *" else "Owner / Company Name *") },
            placeholder = { Text("مثال: شركة المسار السريع") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneChange,
            label = { Text(text = if (isArabic) "رقم الهاتف المعتمد *" else "Official Phone Number *") },
            placeholder = { Text("مثال: 091XXXXXXX") },
            leadingIcon = { Icon(Icons.Default.Phone, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(text = if (isArabic) "البريد الإلكتروني (اختياري)" else "Email Address (Optional)") },
            placeholder = { Text("contact@company.ly") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PageBankDetails(
    isArabic: Boolean,
    bankName: String,
    onBankNameChange: (String) -> Unit,
    branch: String,
    onBranchChange: (String) -> Unit,
    accountName: String,
    onAccountNameChange: (String) -> Unit,
    accountNumber: String,
    onAccountNumberChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isArabic) "الحساب المصرفي لاستلام المستحقات" else "Banking Details for Revenue Settlements",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = if (isArabic) "بيانات الحساب البنكي تدرج بشكل آلي وتلقائي أسفل كشوفات الدفع للمسارات والفواتير." else "Account statements and invoice settlement directions will automatically display these banking credentials.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = bankName,
            onValueChange = onBankNameChange,
            label = { Text(text = if (isArabic) "اسم المصرف" else "Bank Name") },
            placeholder = { Text("مثال: مصرف الجمهورية") },
            leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = branch,
            onValueChange = onBranchChange,
            label = { Text(text = if (isArabic) "اسم الفرع" else "Branch") },
            placeholder = { Text("مثال: فرع الصريم") },
            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = accountName,
            onValueChange = onAccountNameChange,
            label = { Text(text = if (isArabic) "اسم صاحب الحساب" else "Account Name") },
            placeholder = { Text("الاسم بالكامل") },
            leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = accountNumber,
            onValueChange = onAccountNumberChange,
            label = { Text(text = if (isArabic) "رقم الحساب" else "Account Number") },
            placeholder = { Text("مثال: 01-112-XXXX") },
            leadingIcon = { Icon(Icons.Default.Numbers, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PageVisualIdentity(
    isArabic: Boolean,
    selectedPaletteIndex: Int,
    onPaletteSelect: (Int) -> Unit,
    selectedMode: String,
    onModeSelect: (String) -> Unit,
    selectedFont: String,
    onFontSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isArabic) "تخصيص الهوية البصرية للتطبيق" else "Customize Application Style & Themes",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = if (isArabic) "تحكم بالكامل بخصائص وخطوط وثيمات العرض الفوري للأجهزة اللوجستية." else "Fully configure visual aspects, primary themes, and typography for dynamic rendering.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 1. Color Palettes Selector
        Text(text = if (isArabic) "لوحة الألوان (5 خيارات)" else "Color Palette (5 Themes)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        val palettes = listOf(
            Pair("Cosmic", Color(0xFF5B92E5)),
            Pair("Desert", Color(0xFFD4AF37)),
            Pair("Emerald", Color(0xFF00A86B)),
            Pair("Royal", Color(0xFF6A5ACD)),
            Pair("Sunset", Color(0xFFE04F5F))
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            palettes.forEachIndexed { idx, item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onPaletteSelect(idx) }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(color = item.second, shape = CircleShape)
                            .border(
                                width = 3.dp,
                                color = if (selectedPaletteIndex == idx) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = item.first, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // 2. Mode Selection (LIGHT, DARK, DIM)
        Text(text = if (isArabic) "نمط الإضاءة" else "Display Light Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modes = listOf("LIGHT", "DARK", "DIM")
            modes.forEach { mode ->
                val isSel = selectedMode == mode
                Surface(
                    onClick = { onModeSelect(mode) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when (mode) {
                                "LIGHT" -> if (isArabic) "نهاري" else "Light"
                                "DARK" -> if (isArabic) "ليلي" else "Dark"
                                else -> if (isArabic) "داكن مريح" else "Dim"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Typographical Fonts
        Text(text = if (isArabic) "نوع الخط المعتمد" else "Preferred Arabic Font", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        val fonts = listOf("Cairo", "Dubai", "Hacen Tunisia")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            fonts.forEach { f ->
                val isSel = selectedFont == f
                Surface(
                    onClick = { onFontSelect(f) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = f,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
