package com.projectrestaurant.database

import android.os.Parcel
import android.os.Parcelable
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
import androidx.room.Transaction

@Entity(tableName = "Addresses", indices = [Index(value = ["user_id"])], foreignKeys = [
    ForeignKey(User::class, ["user_id"], ["user_id"], ForeignKey.CASCADE, ForeignKey.CASCADE)])
data class Address(
    @PrimaryKey @ColumnInfo(name = "address_id") val addressId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val address: String,
    val cap: String,
    val city: String,
    val province: String,
    @ColumnInfo(name = "default_address") var defaultAddress: Boolean = false
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt() != 0
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(addressId)
        parcel.writeString(userId)
        parcel.writeString(address)
        parcel.writeString(cap)
        parcel.writeString(city)
        parcel.writeString(province)
        parcel.writeInt(if(defaultAddress) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Address> {
        override fun createFromParcel(parcel: Parcel): Address {
            return Address(parcel)
        }

        override fun newArray(size: Int): Array<Address?> {
            return arrayOfNulls(size)
        }
    }
}

@Dao
interface AddressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(vararg addresses: Address)
    @Delete suspend fun delete(address: Address)
    @Query(value = "Select * from Addresses where user_id = :id") suspend fun getAddressesByUserId(id: String): List<Address>

    @Query(value = "Select case when exists(Select address_id from Addresses where user_id = :id) then 1 else 0 end")
    suspend fun exists(id: String): Boolean

    @Query("Select * from Addresses where user_id = :id and default_address = 1")
    suspend fun getDefaultDeliveryAddress(id: String): Address?

    @Query(value = "Update Addresses set default_address = 0 where user_id = :userId and default_address = 1")
    fun removeDefaultAddress(userId: String)

    @Query(value = "Update Addresses set default_address = 1 where user_id = :userId and address_id = :addressId")
    fun addDefaultAddress(userId: String, addressId: String)

    @Transaction
    suspend fun setAsDefaultDeliveryAddress(userId: String, addressId: String) {
        removeDefaultAddress(userId)
        addDefaultAddress(userId, addressId)
    }
}