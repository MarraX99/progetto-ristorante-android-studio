package com.projectrestaurant

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.navigation.ui.AppBarConfiguration
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import com.projectrestaurant.databinding.ActivityMainBinding
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.projectrestaurant.database.RestaurantDB

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var sharedPrefs: SharedPreferences
    lateinit var auth: FirebaseAuth
    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var restaurantDB: RestaurantDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(application)
        auth = FirebaseAuth.getInstance()
        sharedPrefs = getSharedPreferences(getString(com.projectrestaurant.R.string.preference_file_key), Context.MODE_PRIVATE)
        firestoreDB = FirebaseFirestore.getInstance()
        restaurantDB = RestaurantDB.getInstance(application)
        val themeMode = sharedPrefs.getInt(getString(R.string.saved_theme_key), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbarMain)
        navController = findNavController(R.id.nav_host_fragment_main)
        //passing each menu ID as a set of Ids because each menu should be considered as top level destinations
        appBarConfiguration = AppBarConfiguration(setOf(R.id.fragment_main, R.id.nav_settings, R.id.nav_about_us, R.id.nav_privacy_policy, R.id.nav_login_register ,R.id.nav_account), binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener {
            if(it.currentUser != null) {
                Log.i("FirebaseAuth", "${MainActivity::class.java.name} - Authentication state changed to ${it.currentUser?.uid}")
                binding.navView.menu.findItem(R.id.nav_login_register).isVisible = false
                binding.navView.menu.findItem(R.id.nav_account).isVisible = true

            } else {
                Log.i("FirebaseAuth", "${MainActivity::class.java.name} - Authentication state changed to null")
                binding.navView.menu.findItem(R.id.nav_login_register).isVisible = true
                binding.navView.menu.findItem(R.id.nav_account).isVisible = false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
