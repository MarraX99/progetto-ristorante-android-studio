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

@Entity(tableName = "Cart_Product_Edits", foreignKeys = [
    ForeignKey(Ingredient::class, ["ingredient_id"], ["ingredient_id"], ForeignKey.SET_NULL),
    ForeignKey(CartProduct::class, ["cart_product_id"], ["cart_product_id"], ForeignKey.SET_NULL)],
    primaryKeys = ["cart_product_id", "ingredient_id"],
    indices = [Index(value = ["ingredient_id"]), Index(value = ["cart_product_id"])])
data class CartProductEdit(
    @ColumnInfo(name = "cart_product_id") val cartProductId: String,
    @ColumnInfo(name = "ingredient_id") val ingredientId: Int,
    @ColumnInfo(name = "ingredient_quantity") val ingredientQuantity: IngredientQuantity
)

@Dao
interface CartProductEditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(vararg cartProductEdit: CartProductEdit)
    @Delete suspend fun delete(cartProductEdit: CartProductEdit)
    @Query(value = "Select * from Cart_Product_Edits where cart_product_id = :id") suspend fun getCartProductEditsById(id: String): List<CartProductEdit>
}