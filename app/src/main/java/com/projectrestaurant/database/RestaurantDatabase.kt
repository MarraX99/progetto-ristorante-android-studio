package com.projectrestaurant.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, Order::class, Ingredient::class, Food::class, FoodIngredient::class,
                     IngredientChange::class, FoodType::class], version = 1)
abstract class RestaurantDB: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    abstract fun foodTypeDao(): FoodTypeDao
    abstract fun orderDao(): OrderDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun foodIngredientDao(): FoodIngredientDao

    companion object {
        @Volatile
        private var _instance: RestaurantDB? = null

        fun getInstance(context: Context): RestaurantDB {
            synchronized(this) {
                var instance = _instance
                if(instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext, RestaurantDB::class.java,
                        "restaurant_database").fallbackToDestructiveMigration().build()
                    _instance = instance }
                return instance
            }
        }
    }
}