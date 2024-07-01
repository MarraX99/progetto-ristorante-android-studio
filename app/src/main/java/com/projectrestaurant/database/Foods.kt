package com.projectrestaurant.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "Foods", indices = [Index(value = ["type"])])
data class Food (
    @ColumnInfo(name = "food_id") @PrimaryKey val foodId: Int,
    @Ignore var name: String = "",
    @Ignore var description: String = "",
    val type: Int,
    @ColumnInfo(name = "unit_price") var unitPrice: Double,
    @ColumnInfo(name = "image_uri") var imageUri: String = ""
) : Parcelable
{
    //Parcelling part
    constructor(parcel: Parcel) : this(
        parcel.readInt(),   //foodId
        parcel.readString()!!,  //name
        parcel.readString()!!,  //description
        parcel.readInt(),   //type
        parcel.readDouble(),  //unitPrice
        parcel.readString()!!   //imageUri
    )

    constructor(foodId: Int, type: Int, unitPrice: Double, imageUri: String)
    : this(foodId, "", "", type, unitPrice, imageUri)

    override fun describeContents(): Int { return 0 }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(this.foodId)
        parcel.writeString(this.name)
        parcel.writeString(this.description)
        parcel.writeInt(this.type)
        parcel.writeDouble(this.unitPrice)
        parcel.writeString(this.imageUri)
    }

    companion object CREATOR : Parcelable.Creator<Food> {
        override fun createFromParcel(parcel: Parcel): Food {
            return Food(parcel)
        }

        override fun newArray(size: Int): Array<Food?> { return arrayOfNulls(size) }
    }
}

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(vararg foods: Food)
    @Delete suspend fun delete(food: Food)
    @Query(value = "Select * from Foods where food_id = :id") suspend fun getFoodById(id: Int): Food?
    @Query(value = "Select * from Foods where type = :type") suspend fun getFoodsByType(type: Int): List<Food>
    @Query(value = "Select case when exists(Select 1 from Foods) then 1 else 0 end") suspend fun exists(): Boolean
}