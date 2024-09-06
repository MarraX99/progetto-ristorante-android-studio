package com.projectrestaurant.ui.order

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.FirebaseApp
import com.projectrestaurant.databinding.ActivityOrderBinding
import com.projectrestaurant.viewmodel.AccountViewModel
import com.projectrestaurant.viewmodel.FoodOrderViewModel

class ActivityOrder: AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sharedPrefs: SharedPreferences
    lateinit var binding: ActivityOrderBinding
    private lateinit var navController: NavController
    private val foodOrderViewModel: FoodOrderViewModel by viewModels<FoodOrderViewModel>()
    private val accountViewModel: AccountViewModel by viewModels<AccountViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(application)
        sharedPrefs = getSharedPreferences(getString(com.projectrestaurant.R.string.preference_file_key), Context.MODE_PRIVATE)
        if(foodOrderViewModel.isOnline(application)) {

        } else {
            AlertDialog.Builder(this).setTitle(com.projectrestaurant.R.string.order_connection_error_title)
                .setMessage(com.projectrestaurant.R.string.order_connection_error_message)
                .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> this.finish() }.show()
        }
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
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
        binding.navView.setupWithNavController(navController)
//        appBarConfiguration = AppBarConfiguration(setOf(com.projectrestaurant.R.id.fragment_food_type,
//            com.projectrestaurant.R.id.nav_settings, com.projectrestaurant.R.id.nav_about_us,
//            com.projectrestaurant.R.id.nav_privacy_policy, com.projectrestaurant.R.id.nav_login_register ,
//            com.projectrestaurant.R.id.nav_account), binding.drawerLayout)
//        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.lifecycleOwner = this
    }

    override fun onSupportNavigateUp(): Boolean { return navController.navigateUp() || super.onSupportNavigateUp() }
}