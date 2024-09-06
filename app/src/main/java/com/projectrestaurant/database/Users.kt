package com.projectrestaurant.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

@Entity(tableName = "Users", indices = [Index(value = ["email"])])
data class User(
    @ColumnInfo(name = "user_id") @PrimaryKey val userId: String,
    var name: String,
    var surname: String,
    var email: String,
    @ColumnInfo(name = "accepted_privacy_policy") var acceptedPrivacyPolicy: Boolean
)

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(vararg users: User)
    @Update suspend fun update(user: User)
    @Delete suspend fun delete(user: User)
    @Query(value = "Select * from Users where user_id = :uid") suspend fun getUserById(uid: String): User?
    @Query(value = "Delete from Users") suspend fun deleteAllUsers()

    @Query(value = "Select case when exists(Select user_id from Users where user_id = :id) then 1 else 0 end")
    suspend fun exists(id: String): Boolean

    @Query(value = "Update Users set name = :newName where user_id = :id")
    suspend fun changeName(newName: String, id: String)

    @Query(value = "Update Users set name = :newSurname where user_id = :id")
    suspend fun changeSurname(newSurname: String, id: String)

    @Query(value = "Update Users set name = :newEmail where user_id = :id")
    suspend fun changeEmail(newEmail: String, id: String)


    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert2(vararg users: User)
}