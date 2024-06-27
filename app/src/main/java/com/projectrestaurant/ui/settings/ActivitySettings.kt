package com.projectrestaurant.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.projectrestaurant.databinding.ActivitySettingsBinding

class ActivitySettings : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivitySettingsBinding
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefs = this.getSharedPreferences(getString(com.projectrestaurant.R.string.preference_file_key), Context.MODE_PRIVATE)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarSettings)
        navController = findNavController(com.projectrestaurant.R.id.nav_host_fragment_settings)
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean { return navController.navigateUp() || super.onSupportNavigateUp() }
}