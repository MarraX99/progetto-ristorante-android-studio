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

@Entity(tableName = "Orders", foreignKeys = [
    ForeignKey(User::class, arrayOf("user_id"), arrayOf("user_id"), ForeignKey.SET_NULL)],
    indices = [Index(value = ["user_id"])])
data class Order(
    @ColumnInfo(name = "order_id") @PrimaryKey val orderId: String,
    @ColumnInfo(name = "user_id") var userId: String?,
    val date: String,
    val time: String,
    @ColumnInfo(name = "total_price") val totalPrice: Double
)

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(vararg orders: Order)
    @Delete suspend fun delete(order: Order)
    @Query(value = "Select * from Orders where order_id = :id") suspend fun getOrderById(id: String): Order?
    @Query(value = "Delete from Orders") suspend fun deleteAllOrders()
}