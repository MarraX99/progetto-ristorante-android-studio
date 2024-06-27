package com.projectrestaurant.ui.aboutus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.projectrestaurant.databinding.ActivityAboutUsBinding

class ActivityAboutUs : AppCompatActivity() {

    private lateinit var binding : ActivityAboutUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarAboutUs)
    }
}