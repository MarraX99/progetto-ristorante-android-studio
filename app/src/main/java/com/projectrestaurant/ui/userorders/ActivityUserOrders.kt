package com.projectrestaurant.ui.userorders

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.projectrestaurant.databinding.ActivityUserOrdersBinding
import com.projectrestaurant.viewmodel.OrdersViewModel

class ActivityUserOrders: AppCompatActivity() {
    private lateinit var binding: ActivityUserOrdersBinding
    private lateinit var navController: NavController
    private lateinit var viewModel: OrdersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = OrdersViewModel(application)
        binding = ActivityUserOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarUserOrders)
        binding.lifecycleOwner = this
        navController = findNavController(com.projectrestaurant.R.id.nav_host_fragment_user_orders)
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}