package com.projectrestaurant.ui.order

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.projectrestaurant.database.RestaurantDB
import com.projectrestaurant.databinding.ActivityOrderBinding
import com.projectrestaurant.viewmodel.FoodOrderViewModel

class ActivityOrder: AppCompatActivity() {
    private lateinit var binding: ActivityOrderBinding
    private lateinit var navController: NavController
    private val viewModel: FoodOrderViewModel by viewModels<FoodOrderViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarOrder)
        viewModel.setRestaurantDB(RestaurantDB.getInstance(application))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        if(viewModel.isOnline(application)) {
            navController = findNavController(com.projectrestaurant.R.id.nav_host_fragment_order)
            setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            AlertDialog.Builder(this).setTitle(com.projectrestaurant.R.string.order_connection_error_title)
                .setMessage(com.projectrestaurant.R.string.order_connection_error_message)
                .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> this.finish() }.show()
        }
    }

    override fun onSupportNavigateUp(): Boolean { return navController.navigateUp() || super.onSupportNavigateUp() }
}