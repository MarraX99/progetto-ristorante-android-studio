package com.projectrestaurant.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.projectrestaurant.database.RestaurantDB

class SettingsViewModel(private val application: Application): AndroidViewModel(application) {
    private val firestoreDB: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val restaurantDB: RestaurantDB = RestaurantDB.getInstance(application)
    private val sharedPrefs: SharedPreferences = application.getSharedPreferences(application.resources.getString(com.projectrestaurant.R.string.preference_file_key), Context.MODE_PRIVATE)
    private val editor : SharedPreferences.Editor = sharedPrefs.edit()
    private val _deliveryAddressString = MutableLiveData<String?>(null)
    val deliveryAddressString: LiveData<String?>
        get() = _deliveryAddressString

    fun getThemeFromPreferences(): Int =
        sharedPrefs.getInt(application.resources.getString(com.projectrestaurant.R.string.saved_theme_key), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    fun setThemeInPreferences(mode: Int) {
        editor.putInt(application.resources.getString(com.projectrestaurant.R.string.saved_theme_key), mode)
        editor.apply()
    }

    fun changeTheme(mode: Int) = AppCompatDelegate.setDefaultNightMode(mode)




}