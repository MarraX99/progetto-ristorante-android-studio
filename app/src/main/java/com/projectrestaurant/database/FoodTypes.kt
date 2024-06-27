package com.projectrestaurant.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "FoodTypes")
data class FoodType(
    @ColumnInfo(name = "type_id") @PrimaryKey val typeId: Int,
    @Ignore var name: String = "",
    @ColumnInfo(name = "image_uri") var imageUri: String = ""
)
{ constructor(typeId: Int, imageUri: String): this(typeId,"", imageUri) }

@Dao
interface FoodTypeDao {
    @Insert suspend fun insert(vararg types: FoodType)
    @Delete suspend fun delete(type: FoodType)
    @Query(value = "Select * from FoodTypes") suspend fun getAllFoodTypes(): List<FoodType>?
    @Query(value = "Select case when exists(Select 1 from FoodTypes) then 1 else 0 end") suspend fun exists(): Boolean
}