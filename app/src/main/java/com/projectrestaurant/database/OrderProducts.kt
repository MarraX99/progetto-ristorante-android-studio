package com.projectrestaurant.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.Index
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "Order_Products", foreignKeys = [
    ForeignKey(Order::class, ["order_id"], ["order_id"], ForeignKey.CASCADE, ForeignKey.CASCADE),
    ForeignKey(Food::class, ["food_id"], ["food_id"], ForeignKey.SET_NULL, ForeignKey.CASCADE)],
    indices = [Index(value = ["order_id"]), Index(value = ["food_id"])])
data class OrderProduct(
    @ColumnInfo(name = "order_product_id") @PrimaryKey val orderProductId: String,
    @ColumnInfo(name = "order_id") val orderId: String?,
    @ColumnInfo(name = "food_id") val foodId: Int,
    val quantity: Int,
    val price: Double,
)

@Dao
interface OrderProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(vararg cartProduct: OrderProduct)
    @Delete suspend fun delete(cartProduct: OrderProduct)
    @Query(value = "Select * from Order_Products where order_id = :id") suspend fun getOrderProductsByOrderId(id: String): List<OrderProduct>
}