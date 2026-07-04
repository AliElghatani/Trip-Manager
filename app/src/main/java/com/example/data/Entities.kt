package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val isSetupCompleted: Boolean = false,
    
    // User info
    val ownerName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val logoPath: String = "",
    
    // Bank Details
    val bankName: String = "",
    val branch: String = "",
    val accountName: String = "",
    val accountNumber: String = "",
    val currency: String = "د.ل",
    
    // Theme & Visual Identity
    val themePaletteIndex: Int = 0, // 0 to 4
    val themeMode: String = "DARK", // LIGHT, DARK, DIM
    val fontName: String = "Cairo", // Cairo, Dubai, Hacen Tunisia, etc.
    val language: String = "ar", // ar, en
    
    // Development Fund
    val developmentFundPercent: Float = 5.0f // % of Net Profit
)

@Entity(
    tableName = "vehicles",
    indices = [Index(value = ["plateNumber"], unique = true)]
)
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plateNumber: String,
    val type: String,
    val capacity: String,
    val ownerPercent: Float, // e.g. 65.0f
    val vehiclePercent: Float // e.g. 35.0f
)

@Entity(tableName = "drivers")
data class Driver(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val contractType: String, // FIXED_PERCENT, MONTHLY_SALARY, FIXED_TRIP
    val contractValue: Double // can be percentage rate (e.g. 10.0 for 10%), monthly rate, or fixed trip rate
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = ""
)

@Entity(
    tableName = "movement_orders",
    foreignKeys = [
        ForeignKey(entity = Customer::class, parentColumns = ["id"], childColumns = ["customerId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Vehicle::class, parentColumns = ["id"], childColumns = ["vehicleId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Driver::class, parentColumns = ["id"], childColumns = ["driverId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index("customerId"),
        Index("vehicleId"),
        Index("driverId"),
        Index(value = ["orderNumber"], unique = true)
    ]
)
data class MovementOrder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val movementType: String, // INTERNAL, EXTERNAL
    val date: Long = System.currentTimeMillis(),
    val customerId: Long,
    val vehicleId: Long,
    val driverId: Long,
    val origin: String,
    val destination: String,
    val tripType: String, // ONE_WAY, ROUND_TRIP
    val returnDetails: String = "",
    val basePrice: Double,
    val status: String, // UNPAID, PARTIALLY_PAID, FULLY_PAID, CANCELLED
    val notes: String = ""
)

@Entity(
    tableName = "extra_services",
    foreignKeys = [
        ForeignKey(entity = MovementOrder::class, parentColumns = ["id"], childColumns = ["movementOrderId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("movementOrderId")]
)
data class ExtraService(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val movementOrderId: Long,
    val name: String,
    val price: Double
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(entity = Driver::class, parentColumns = ["id"], childColumns = ["driverId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = MovementOrder::class, parentColumns = ["id"], childColumns = ["movementOrderId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("driverId"), Index("movementOrderId")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // OPERATIONAL, DRIVER
    val date: Long = System.currentTimeMillis(),
    val categoryOrItem: String,
    val quantity: Int = 1,
    val unitPrice: Double,
    val total: Double,
    val driverId: Long? = null,
    val movementOrderId: Long? = null
)

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(entity = Customer::class, parentColumns = ["id"], childColumns = ["customerId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = MovementOrder::class, parentColumns = ["id"], childColumns = ["movementOrderId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("customerId"), Index("movementOrderId")]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val movementOrderId: Long,
    val amountPaid: Double,
    val paymentMethod: String, // CASH, BANK_TRANSFER, SPLIT
    val cashAmount: Double = 0.0,
    val bankAmount: Double = 0.0,
    val discount: Double = 0.0,
    val date: Long = System.currentTimeMillis()
)
