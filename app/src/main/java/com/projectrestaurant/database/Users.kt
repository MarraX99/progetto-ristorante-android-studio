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
    val name: String,
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
}