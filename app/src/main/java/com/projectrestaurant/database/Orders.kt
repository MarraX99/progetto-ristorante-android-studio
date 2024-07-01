package com.projectrestaurant.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

private class DateConverter {
    // Timestamp = number of milliseconds passed since January 1, 1970, 00:00:00 UTC
    @TypeConverter fun dateFromTimestamp(timestamp: Long): Date = Date(timestamp)

    @TypeConverter fun timestampFromDate(date: Date): Long = date.time
}

@Entity(tableName = "Orders", foreignKeys = [
    ForeignKey(User::class, ["user_id"], ["user_id"], ForeignKey.SET_NULL)],
    indices = [Index(value = ["user_id"])])
@TypeConverters(DateConverter::class)
data class Order(
    @ColumnInfo(name = "order_id") @PrimaryKey val orderId: String,
    @ColumnInfo(name = "user_id") var userId: String?,
    @ColumnInfo(name = "order_date") val orderDate: Date,
    @ColumnInfo(name = "delivery_date") val deliveryDate: Date,
    @ColumnInfo(name = "total_price") val totalPrice: Double
)

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(vararg orders: Order)
    @Delete suspend fun delete(order: Order)
    @Query(value = "Select * from Orders where order_id = :id") suspend fun getOrderById(id: String): Order?
    @Query(value = "Delete from Orders") suspend fun deleteAllOrders()
}