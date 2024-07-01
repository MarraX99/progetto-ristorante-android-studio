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

@Entity(tableName = "Cart_Products", foreignKeys = [
    ForeignKey(Order::class, ["order_id"], ["order_id"], ForeignKey.SET_NULL)],
    indices = [Index(value = ["order_id"])])
data class CartProduct(
    @ColumnInfo(name = "cart_product_id") @PrimaryKey val cartProductId: String,
    @ColumnInfo(name = "order_id") val orderId: String,
    val quantity: Int,
    val price: Double
)

@Dao
interface CartProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(vararg cartProduct: CartProduct)
    @Delete suspend fun delete(cartProduct: CartProduct)
    @Query(value = "Select * from Cart_Products where order_id = :id") suspend fun findCartProductsByOrderId(id: String): List<CartProduct>
}