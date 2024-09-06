package com.projectrestaurant.database;

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns

@Entity(tableName = "FoodIngredients", foreignKeys = [
    ForeignKey(Food::class, ["food_id"], ["food_id"], ForeignKey.CASCADE, ForeignKey.CASCADE),
    ForeignKey(Ingredient::class, ["ingredient_id"], ["ingredient_id"], ForeignKey.CASCADE, ForeignKey.CASCADE)],
    primaryKeys = ["food_id", "ingredient_id"],
    indices = [Index(value = ["food_id"]), Index(value = ["ingredient_id"])])
data class FoodIngredient(
    @ColumnInfo(name = "food_id") val foodId: Int,
    @ColumnInfo(name = "ingredient_id") val ingredientId: Int
)

@Dao
interface FoodIngredientDao {
    @Insert suspend fun insert(vararg foodIngredients: FoodIngredient)
    @Delete suspend fun delete(foodIngredient: FoodIngredient)
    @RewriteQueriesToDropUnusedColumns
    @Query("Select * from Ingredients i inner join FoodIngredients fi on i.ingredient_id = fi.ingredient_id where fi.food_id = :id")
    suspend fun getIngredientsByFoodId(id: Int): List<Ingredient>
}
