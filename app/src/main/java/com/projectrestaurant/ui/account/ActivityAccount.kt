package com.projectrestaurant.ui.account

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.projectrestaurant.databinding.ActivityAccountBinding
import com.projectrestaurant.viewmodel.AccountViewModel

class ActivityAccount: AppCompatActivity() {
    private lateinit var binding: ActivityAccountBinding
    private val viewModel: AccountViewModel by viewModels<AccountViewModel>()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolbarAccount)
        navController = findNavController(com.projectrestaurant.R.id.nav_host_fragment_account)
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean { return navController.navigateUp() || super.onSupportNavigateUp() }
}