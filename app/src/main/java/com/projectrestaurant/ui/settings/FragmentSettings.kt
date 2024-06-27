package com.projectrestaurant.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.projectrestaurant.databinding.FragmentSettingsBinding

class FragmentSettings : Fragment() {
    private lateinit var navController: NavController
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = view.findNavController()
        if(activity is ActivitySettings) {
            (activity as ActivitySettings).supportActionBar?.setDisplayHomeAsUpEnabled(true)
            val v = (activity as ActivitySettings).sharedPrefs.getInt(getString(com.projectrestaurant.R.string.saved_theme_key), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            binding.textViewCurrentTheme.text = when(v) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> getString(com.projectrestaurant.R.string.theme_system)
                AppCompatDelegate.MODE_NIGHT_NO -> getString(com.projectrestaurant.R.string.theme_light)
                AppCompatDelegate.MODE_NIGHT_YES -> getString(com.projectrestaurant.R.string.theme_dark)
                else -> getString(com.projectrestaurant.R.string.theme_system)
            }
        }
        binding.cardViewTheme.setOnClickListener {
            navController.navigate(com.projectrestaurant.R.id.action_fragment_settings_to_fragment_theme) }
        binding.cardViewAppInfo.setOnClickListener{
            navController.navigate(com.projectrestaurant.R.id.action_fragment_settings_to_fragment_app_info) }
    }
}