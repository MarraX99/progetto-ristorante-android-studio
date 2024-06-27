package com.projectrestaurant.ui.privacypolicy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.projectrestaurant.databinding.ActivityPrivacyPolicyBinding

class ActivityPrivacyPolicy : AppCompatActivity() {

    private lateinit var binding : ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbarPrivacyPolicy)
        setContentView(binding.root)
    }
}