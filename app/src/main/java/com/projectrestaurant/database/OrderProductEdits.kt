package com.projectrestaurant.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.projectrestaurant.IngredientQuantity

@Entity(tableName = "Order_Product_Edits", foreignKeys = [
    ForeignKey(Ingredient::class, ["ingredient_id"], ["ingredient_id"], ForeignKey.CASCADE, ForeignKey.CASCADE),
    ForeignKey(OrderProduct::class, ["order_product_id"], ["order_product_id"], ForeignKey.CASCADE, ForeignKey.CASCADE)],
    primaryKeys = ["order_product_id", "ingredient_id"],
    indices = [Index(value = ["ingredient_id"]), Index(value = ["order_product_id"])])
data class OrderProductEdit(
    @ColumnInfo(name = "order_product_id") val orderProductId: String,
    @ColumnInfo(name = "ingredient_id") val ingredientId: Int,
    @ColumnInfo(name = "ingredient_quantity") val ingredientQuantity: IngredientQuantity
)

@Dao
interface OrderProductEditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(vararg cartProductEdit: OrderProductEdit)
    @Delete suspend fun delete(cartProductEdit: OrderProductEdit)
    @Query(value = "Select * from Order_Product_Edits where order_product_id = :id") suspend fun getOrderProductEditsByProductId(id: String): List<OrderProductEdit>
}