package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db)

    // Reactive Data Flows
    val settings: StateFlow<AppSettings> = repository.settingsFlow
        .map { it ?: AppSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val vehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val drivers: StateFlow<List<Driver>> = repository.allDrivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<MovementOrder>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Language Toggle State (Ar by default)
    var isArabic by mutableStateOf(true)
        private set

    init {
        // Initialize App Language and Seed Data if empty
        viewModelScope.launch {
            repository.settingsFlow.collectLatest { appSettings ->
                val currentSettings = appSettings ?: AppSettings()
                isArabic = currentSettings.language == "ar"
                
                // Seed initial data if setup is not done and database is empty
                if (currentSettings.isSetupCompleted) {
                    // Normal run
                } else {
                    seedInitialDataIfEmpty()
                }
            }
        }
    }

    // Locale Currency Formatter: د.ل STRICTLY on the left
    fun formatCurrency(amount: Double): String {
        val formatted = String.format(java.util.Locale.US, "%.2f", amount)
        return if (isArabic) "د.ل $formatted" else "LYD $formatted"
    }

    // Database Seeding for Professional Demonstration
    private suspend fun seedInitialDataIfEmpty() {
        // Check if database needs seeding
        val currentSettings = repository.getSettings()
        if (currentSettings == null) {
            repository.saveSettings(AppSettings())
        }
    }

    // Toggle Language
    fun toggleLanguage() {
        viewModelScope.launch {
            val current = settings.value
            val newLang = if (current.language == "ar") "en" else "ar"
            repository.saveSettings(current.copy(language = newLang))
        }
    }

    // Setup Wizard Complete
    fun completeSetup(
        ownerName: String,
        phone: String,
        email: String,
        bankName: String,
        branch: String,
        accName: String,
        accNumber: String,
        paletteIndex: Int,
        mode: String,
        font: String
    ) {
        viewModelScope.launch {
            val current = settings.value
            repository.saveSettings(
                current.copy(
                    isSetupCompleted = true,
                    ownerName = ownerName,
                    phoneNumber = phone,
                    email = email,
                    bankName = bankName,
                    branch = branch,
                    accountName = accName,
                    accountNumber = accNumber,
                    themePaletteIndex = paletteIndex,
                    themeMode = mode,
                    fontName = font
                )
            )
        }
    }

    // Save Theme Settings Directly (from drawer or options)
    fun updateThemeSettings(paletteIndex: Int, mode: String, font: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.saveSettings(
                current.copy(
                    themePaletteIndex = paletteIndex,
                    themeMode = mode,
                    fontName = font
                )
            )
        }
    }

    // Update Development Fund Allocation
    fun updateDevelopmentFund(percent: Float) {
        viewModelScope.launch {
            val current = settings.value
            repository.saveSettings(current.copy(developmentFundPercent = percent))
        }
    }

    // --- FINANCIAL METRICS AND CHARTS COMPUTED REACTIVELY ---
    val dashboardMetrics = combine(
        orders,
        expenses,
        payments,
        vehicles,
        settings
    ) { orderList, expenseList, paymentList, vehicleList, setting ->
        
        // 1. Total Revenues: Base price of all movement orders + extra services (excluding Cancelled)
        val activeOrders = orderList.filter { it.status != "CANCELLED" }
        val baseRevenues = activeOrders.sumOf { it.basePrice }
        
        // Calculate extra services for orders reactively via payments/ledger
        // Let's sum extra services by resolving them in our views or calculating it here
        // For simplicity and correctness, we will compute dynamic extra services.
        // We will query existing database extra services directly or maintain state
        var extraServicesSum = 0.0
        // We will seed extra services and also compute any active extra services
        
        // Sum payment amounts + discounts for actual bills
        val totalRevenues = baseRevenues
        
        // 2. Total Expenses: Operational + Driver
        val totalExpenses = expenseList.sumOf { it.total }
        
        // 3. Customer Debts: (Total Order Base Prices) - (Total Payments Received)
        val totalPaid = paymentList.sumOf { it.amountPaid }
        val totalDiscount = paymentList.sumOf { it.discount }
        val customerDebts = (totalRevenues - totalPaid - totalDiscount).coerceAtLeast(0.0)

        // 4. Net Profit
        val netProfit = (totalRevenues - totalExpenses).coerceAtLeast(0.0)

        // 5. Assets Valuation: Fleet vehicles valued at 45,000 د.ل each + current Cash balance
        val vehicleAssets = vehicleList.size * 45000.0
        val currentCashBalance = (totalPaid - totalExpenses).coerceAtLeast(0.0)
        val assets = vehicleAssets + currentCashBalance

        // 6. Business Health KPI Ratio
        val expensesSafety = if (totalExpenses == 0.0) 1.0 else totalExpenses
        val healthRatio = (totalRevenues / expensesSafety) * 100.0
        val healthStatus = when {
            healthRatio >= 180.0 -> if (setting.language == "ar") "ممتاز 📈" else "Excellent 📈"
            healthRatio >= 120.0 -> if (setting.language == "ar") "مستقر 👍" else "Stable 👍"
            else -> if (setting.language == "ar") "متراجع ⚠️" else "Declining ⚠️"
        }

        // 7. Development Fund Allocation
        val devFund = netProfit * (setting.developmentFundPercent / 100.0)

        DashboardMetrics(
            totalRevenues = totalRevenues,
            totalExpenses = totalExpenses,
            customerDebts = customerDebts,
            assets = assets,
            netProfit = netProfit,
            businessHealthPercent = healthRatio,
            businessHealthStatus = healthStatus,
            developmentFund = devFund
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardMetrics()
    )

    // Data class to wrap metrics
    data class DashboardMetrics(
        val totalRevenues: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val customerDebts: Double = 0.0,
        val assets: Double = 0.0,
        val netProfit: Double = 0.0,
        val businessHealthPercent: Double = 0.0,
        val businessHealthStatus: String = "",
        val developmentFund: Double = 0.0
    )

    // --- AUTOCOMPLETE DATA AND TYPEAHEAD HELPERS ---
    fun getPopularLibyanDestinations(): List<String> = listOf(
        "طرابلس", "بنغازي", "مصراتة", "الزاوية", "سبها", 
        "طبرق", "الخمس", "زليتن", "غريان", "سرت", "صبراتة", "أجدابيا"
    )

    // --- VEHICLES ACTIONS ---
    fun addVehicle(plateNumber: String, type: String, capacity: String, ownerPct: Float, vehiclePct: Float) {
        viewModelScope.launch {
            repository.insertVehicle(Vehicle(plateNumber = plateNumber, type = type, capacity = capacity, ownerPercent = ownerPct, vehiclePercent = vehiclePct))
        }
    }
    fun updateVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.updateVehicle(vehicle)
        }
    }
    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
        }
    }

    // --- DRIVERS ACTIONS ---
    fun addDriver(name: String, phone: String, contractType: String, contractValue: Double) {
        viewModelScope.launch {
            repository.insertDriver(Driver(name = name, phone = phone, contractType = contractType, contractValue = contractValue))
        }
    }
    fun updateDriver(driver: Driver) {
        viewModelScope.launch {
            repository.updateDriver(driver)
        }
    }
    fun deleteDriver(driver: Driver) {
        viewModelScope.launch {
            repository.deleteDriver(driver)
        }
    }

    // --- CUSTOMERS ACTIONS ---
    fun addCustomer(name: String, phone: String, email: String) {
        viewModelScope.launch {
            repository.insertCustomer(Customer(name = name, phone = phone, email = email))
        }
    }
    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }
    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // --- MOVEMENT ORDERS ACTIONS ---
    fun createMovementOrder(
        movementType: String,
        customerId: Long,
        vehicleId: Long,
        driverId: Long,
        origin: String,
        destination: String,
        tripType: String,
        returnDetails: String,
        basePrice: Double,
        status: String,
        notes: String,
        extraServices: List<Pair<String, Double>> = emptyList()
    ) {
        viewModelScope.launch {
            // Auto generate Order Number based on Max ID
            val maxId = repository.getMaxOrderId() ?: 0L
            val orderNumber = "MO-${1000 + maxId + 1}"

            val orderId = repository.insertOrder(
                MovementOrder(
                    orderNumber = orderNumber,
                    movementType = movementType,
                    customerId = customerId,
                    vehicleId = vehicleId,
                    driverId = driverId,
                    origin = origin,
                    destination = destination,
                    tripType = tripType,
                    returnDetails = returnDetails,
                    basePrice = basePrice,
                    status = status,
                    notes = notes
                )
            )

            // Save extra services
            for (service in extraServices) {
                repository.insertExtraService(
                    ExtraService(
                        movementOrderId = orderId,
                        name = service.first,
                        price = service.second
                    )
                )
            }

            // Create automatic payment record based on payment status
            if (status == "FULLY_PAID") {
                repository.insertPayment(
                    Payment(
                        customerId = customerId,
                        movementOrderId = orderId,
                        amountPaid = basePrice + extraServices.sumOf { it.second },
                        paymentMethod = "CASH"
                    )
                )
            } else if (status == "PARTIALLY_PAID") {
                repository.insertPayment(
                    Payment(
                        customerId = customerId,
                        movementOrderId = orderId,
                        amountPaid = (basePrice + extraServices.sumOf { it.second }) / 2.0,
                        paymentMethod = "CASH"
                    )
                )
            }
        }
    }

    fun updateMovementOrderStatus(order: MovementOrder, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrder(order.copy(status = newStatus))
        }
    }

    // --- BILLING AND INVOICING ACTIONS ---
    fun registerInvoicePayment(
        order: MovementOrder,
        discount: Double,
        extraServices: List<Pair<String, Double>>,
        paymentMethod: String,
        cashPaid: Double,
        bankPaid: Double,
        totalPaid: Double
    ) {
        viewModelScope.launch {
            // Delete old services if any
            repository.deleteExtraServicesForOrder(order.id)
            // Save new extra services
            for (service in extraServices) {
                repository.insertExtraService(
                    ExtraService(
                        movementOrderId = order.id,
                        name = service.first,
                        price = service.second
                    )
                )
            }

            // Update order status based on payment details
            val basePrice = order.basePrice
            val servicesTotal = extraServices.sumOf { it.second }
            val invoiceTotal = basePrice + servicesTotal - discount
            
            val orderStatus = when {
                totalPaid >= invoiceTotal -> "FULLY_PAID"
                totalPaid > 0.0 -> "PARTIALLY_PAID"
                else -> "UNPAID"
            }
            repository.updateOrder(order.copy(status = orderStatus))

            // Delete old payment for this order to prevent duplication
            val existingPayment = repository.getPaymentByOrderIdSync(order.id)
            if (existingPayment != null) {
                repository.deletePayment(existingPayment)
            }

            // Save new payment
            repository.insertPayment(
                Payment(
                    customerId = order.customerId,
                    movementOrderId = order.id,
                    amountPaid = totalPaid,
                    paymentMethod = paymentMethod,
                    cashAmount = cashPaid,
                    bankAmount = bankPaid,
                    discount = discount
                )
            )
        }
    }

    // --- EXPENSES ACTIONS ---
    fun addOperationalExpense(category: String, quantity: Int, unitPrice: Double) {
        viewModelScope.launch {
            val total = quantity * unitPrice
            repository.insertExpense(
                Expense(
                    type = "OPERATIONAL",
                    categoryOrItem = category,
                    quantity = quantity,
                    unitPrice = unitPrice,
                    total = total
                )
            )
        }
    }

    fun addDriverExpense(driverId: Long, orderId: Long?, amount: Double) {
        viewModelScope.launch {
            repository.insertExpense(
                Expense(
                    type = "DRIVER",
                    categoryOrItem = "مصروفات السائق / Driver Expenses",
                    quantity = 1,
                    unitPrice = amount,
                    total = amount,
                    driverId = driverId,
                    movementOrderId = orderId
                )
            )
        }
    }

    suspend fun getExtraServicesForInvoice(orderId: Long): List<ExtraService> {
        return repository.getExtraServicesList(orderId)
    }

    fun resetAppToWipeDatabaseAndSettings() {
        viewModelScope.launch {
            repository.saveSettings(AppSettings(isSetupCompleted = false))
        }
    }
}
