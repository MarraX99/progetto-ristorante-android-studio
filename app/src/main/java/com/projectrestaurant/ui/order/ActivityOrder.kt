package com.projectrestaurant.ui.order

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.projectrestaurant.databinding.ActivityOrderBinding
import com.projectrestaurant.viewmodel.FoodOrderViewModel

class ActivityOrder: AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sharedPrefs: SharedPreferences
    lateinit var binding: ActivityOrderBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private lateinit var navController: NavController
    private lateinit var viewModel: FoodOrderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = FoodOrderViewModel(application)
        FirebaseApp.initializeApp(application)
        sharedPrefs = getSharedPreferences(getString(com.projectrestaurant.R.string.preference_file_key), Context.MODE_PRIVATE)
        val themeMode = sharedPrefs.getInt(getString(com.projectrestaurant.R.string.saved_theme_key), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES)
            AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO)
        }
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbarOrder)
        navController = findNavController(com.projectrestaurant.R.id.nav_host_fragment_order)
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        binding.lifecycleOwner = this
    }

    override fun onStart() {
        super.onStart()
        if(auth.currentUser != null) {
            Log.i("FirebaseAuth", "${ActivityOrder::class.java.name} - Authentication state changed to ${auth.currentUser?.uid}")
            binding.navView.menu.findItem(com.projectrestaurant.R.id.nav_login_register).isVisible = false
            binding.navView.menu.findItem(com.projectrestaurant.R.id.nav_account).isVisible = true

        } else {
            Log.i("FirebaseAuth", "${ActivityOrder::class.java.name} - Authentication state changed to null")
            binding.navView.menu.findItem(com.projectrestaurant.R.id.nav_login_register).isVisible = true
            binding.navView.menu.findItem(com.projectrestaurant.R.id.nav_account).isVisible = false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}