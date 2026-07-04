package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettings)
}

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY plateNumber ASC")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    suspend fun getVehicleById(id: Long): Vehicle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vehicle: Vehicle): Long

    @Update
    suspend fun update(vehicle: Vehicle)

    @Delete
    suspend fun delete(vehicle: Vehicle)
}

@Dao
interface DriverDao {
    @Query("SELECT * FROM drivers ORDER BY name ASC")
    fun getAllDrivers(): Flow<List<Driver>>

    @Query("SELECT * FROM drivers WHERE id = :id LIMIT 1")
    suspend fun getDriverById(id: Long): Driver?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(driver: Driver): Long

    @Update
    suspend fun update(driver: Driver)

    @Delete
    suspend fun delete(driver: Driver)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer): Long

    @Update
    suspend fun update(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)
}

@Dao
interface MovementOrderDao {
    @Query("SELECT * FROM movement_orders ORDER BY date DESC")
    fun getAllOrders(): Flow<List<MovementOrder>>

    @Query("SELECT * FROM movement_orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Long): MovementOrder?

    @Query("SELECT MAX(id) FROM movement_orders")
    suspend fun getMaxId(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: MovementOrder): Long

    @Update
    suspend fun update(order: MovementOrder)

    @Delete
    suspend fun delete(order: MovementOrder)
}

@Dao
interface ExtraServiceDao {
    @Query("SELECT * FROM extra_services WHERE movementOrderId = :orderId")
    fun getServicesForOrder(orderId: Long): Flow<List<ExtraService>>

    @Query("SELECT * FROM extra_services WHERE movementOrderId = :orderId")
    suspend fun getServicesForOrderList(orderId: Long): List<ExtraService>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(service: ExtraService): Long

    @Query("DELETE FROM extra_services WHERE movementOrderId = :orderId")
    suspend fun deleteForOrder(orderId: Long)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE movementOrderId = :orderId LIMIT 1")
    fun getPaymentByOrderId(orderId: Long): Flow<Payment?>

    @Query("SELECT * FROM payments WHERE movementOrderId = :orderId LIMIT 1")
    suspend fun getPaymentByOrderIdSync(orderId: Long): Payment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment): Long

    @Update
    suspend fun update(payment: Payment)

    @Delete
    suspend fun delete(payment: Payment)
}
