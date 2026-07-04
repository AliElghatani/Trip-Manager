package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {
    
    // DAOs
    private val settingsDao = db.appSettingsDao()
    private val vehicleDao = db.vehicleDao()
    private val driverDao = db.driverDao()
    private val customerDao = db.customerDao()
    private val orderDao = db.movementOrderDao()
    private val extraServiceDao = db.extraServiceDao()
    private val expenseDao = db.expenseDao()
    private val paymentDao = db.paymentDao()

    // Flow Streams
    val settingsFlow: Flow<AppSettings?> = settingsDao.getSettingsFlow()
    val allVehicles: Flow<List<Vehicle>> = vehicleDao.getAllVehicles()
    val allDrivers: Flow<List<Driver>> = driverDao.getAllDrivers()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allOrders: Flow<List<MovementOrder>> = orderDao.getAllOrders()
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()

    // Suspend Operations - App Settings
    suspend fun getSettings(): AppSettings? = settingsDao.getSettings()
    suspend fun saveSettings(settings: AppSettings) = settingsDao.insertOrUpdate(settings)

    // Vehicles
    suspend fun insertVehicle(vehicle: Vehicle): Long = vehicleDao.insert(vehicle)
    suspend fun updateVehicle(vehicle: Vehicle) = vehicleDao.update(vehicle)
    suspend fun deleteVehicle(vehicle: Vehicle) = vehicleDao.delete(vehicle)
    suspend fun getVehicleById(id: Long): Vehicle? = vehicleDao.getVehicleById(id)

    // Drivers
    suspend fun insertDriver(driver: Driver): Long = driverDao.insert(driver)
    suspend fun updateDriver(driver: Driver) = driverDao.update(driver)
    suspend fun deleteDriver(driver: Driver) = driverDao.delete(driver)
    suspend fun getDriverById(id: Long): Driver? = driverDao.getDriverById(id)

    // Customers
    suspend fun insertCustomer(customer: Customer): Long = customerDao.insert(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.update(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.delete(customer)
    suspend fun getCustomerById(id: Long): Customer? = customerDao.getCustomerById(id)

    // Movement Orders
    suspend fun insertOrder(order: MovementOrder): Long = orderDao.insert(order)
    suspend fun updateOrder(order: MovementOrder) = orderDao.update(order)
    suspend fun deleteOrder(order: MovementOrder) = orderDao.delete(order)
    suspend fun getOrderById(id: Long): MovementOrder? = orderDao.getOrderById(id)
    suspend fun getMaxOrderId(): Long? = orderDao.getMaxId()

    // Extra Services
    fun getExtraServicesFlow(orderId: Long): Flow<List<ExtraService>> = extraServiceDao.getServicesForOrder(orderId)
    suspend fun getExtraServicesList(orderId: Long): List<ExtraService> = extraServiceDao.getServicesForOrderList(orderId)
    suspend fun insertExtraService(service: ExtraService) = extraServiceDao.insert(service)
    suspend fun deleteExtraServicesForOrder(orderId: Long) = extraServiceDao.deleteForOrder(orderId)

    // Expenses
    suspend fun insertExpense(expense: Expense): Long = expenseDao.insert(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)

    // Payments
    fun getPaymentByOrderId(orderId: Long): Flow<Payment?> = paymentDao.getPaymentByOrderId(orderId)
    suspend fun getPaymentByOrderIdSync(orderId: Long): Payment? = paymentDao.getPaymentByOrderIdSync(orderId)
    suspend fun insertPayment(payment: Payment): Long = paymentDao.insert(payment)
    suspend fun updatePayment(payment: Payment) = paymentDao.update(payment)
    suspend fun deletePayment(payment: Payment) = paymentDao.delete(payment)
}
