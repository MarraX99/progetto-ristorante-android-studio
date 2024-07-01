package com.projectrestaurant.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "Ingredients")
data class Ingredient(
    @ColumnInfo(name = "ingredient_id") @PrimaryKey val ingredientId: Int,
    @Ignore var name: String = "",
    @ColumnInfo(name = "unit_price") var unitPrice: Double,
    @ColumnInfo(name = "image_uri") var imageUri: String
): Parcelable
{
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readString()!!
    ) {}

    constructor(ingredientId: Int, unitPrice: Double, imageUri: String): this(ingredientId, "", unitPrice, imageUri)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(this.ingredientId)
        parcel.writeString(this.name)
        parcel.writeDouble(this.unitPrice)
        parcel.writeString(this.imageUri)
    }

    override fun describeContents(): Int { return 0 }

    companion object CREATOR : Parcelable.Creator<Ingredient> {
        override fun createFromParcel(parcel: Parcel): Ingredient {
            return Ingredient(parcel)
        }

        override fun newArray(size: Int): Array<Ingredient?> {return arrayOfNulls(size) }
    }
}

@Dao
interface IngredientDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(vararg ingredients: Ingredient)
    @Delete suspend fun delete(ingredient: Ingredient)
    @Query(value = "Select * from Ingredients where ingredient_id = :id") suspend fun getIngredientById(id: Int): Ingredient?
    @Query(value = "Select case when exists(Select 1 from Ingredients) then 1 else 0 end") suspend fun exists(): Boolean
}