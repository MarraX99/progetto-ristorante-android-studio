package com.projectrestaurant.ui.order

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.projectrestaurant.databinding.ActivityOrderBinding
import com.projectrestaurant.viewmodel.AccountViewModel
import com.projectrestaurant.viewmodel.FoodOrderViewModel

class ActivityOrder: AppCompatActivity() {
    lateinit var binding: ActivityOrderBinding
    private lateinit var navController: NavController
    private val foodOrderViewModel: FoodOrderViewModel by viewModels<FoodOrderViewModel>()
    private val accountViewModel: AccountViewModel by viewModels<AccountViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarOrder)
        binding.lifecycleOwner = this
        if(foodOrderViewModel.isOnline(application)) {
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