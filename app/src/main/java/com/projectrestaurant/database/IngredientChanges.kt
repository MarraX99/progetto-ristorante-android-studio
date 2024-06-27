package com.projectrestaurant.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey

@Entity(tableName = "IngredientChanges", foreignKeys = [
    ForeignKey(Order::class, arrayOf("order_id"), arrayOf("order_id"), ForeignKey.CASCADE),
    ForeignKey(Food::class, arrayOf("food_id"), arrayOf("food_id"), ForeignKey.SET_NULL),
    ForeignKey(Ingredient::class, arrayOf("ingredient_id"), arrayOf("ingredient_id"), ForeignKey.SET_NULL)],
    indices = [Index(value = ["food_id"]), Index(value = ["ingredient_id"]), Index(value = ["order_id"])])
data class IngredientChange (
    @ColumnInfo(name = "change_id") @PrimaryKey val changeId: String,
    @ColumnInfo(name = "order_id") var orderId: String?,
    @ColumnInfo(name = "food_id") var foodId: Int?,
    @ColumnInfo(name = "ingredient_id") var ingredientId: Int?
)

@Dao
interface IngredientChangeDao {
    @Insert suspend fun insert(vararg changes: IngredientChange)
    @Delete suspend fun delete(change: IngredientChange)
}