package com.projectrestaurant.ui.loginregister

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.projectrestaurant.MainActivity
import com.projectrestaurant.database.RestaurantDB
import com.projectrestaurant.databinding.ActivityLoginRegisterBinding
import com.projectrestaurant.viewmodel.LoginRegisterViewModel

class ActivityLoginRegister : AppCompatActivity() {
    private lateinit var binding: ActivityLoginRegisterBinding
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private val viewModel: LoginRegisterViewModel by viewModels<LoginRegisterViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.setUserDao(RestaurantDB.getInstance(application).userDao())
        setSupportActionBar(binding.toolbarLoginRegister)
        navController = findNavController(com.projectrestaurant.R.id.nav_host_fragment_login_register)
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener {
            if(auth.currentUser != null) {
                Log.i("FirebaseAuth", "${ActivityLoginRegister::class.java.name} - Authentication state changed to ${it.currentUser?.uid}")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { return navController.navigateUp() || super.onSupportNavigateUp() }
}