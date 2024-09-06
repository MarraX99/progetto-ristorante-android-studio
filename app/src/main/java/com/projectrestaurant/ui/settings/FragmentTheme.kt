package com.projectrestaurant.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.projectrestaurant.databinding.FragmentSettingsThemeBinding
import com.projectrestaurant.viewmodel.SettingsViewModel

class FragmentTheme : Fragment() {
    private lateinit var binding : FragmentSettingsThemeBinding
    private val viewModel: SettingsViewModel by activityViewModels<SettingsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentSettingsThemeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when(viewModel.getThemeFromPreferences()) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> binding.radioButtonThemeSystem.isChecked = true
            AppCompatDelegate.MODE_NIGHT_NO -> binding.radioButtonThemeLight.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.radioButtonThemeDark.isChecked = true
        }
        binding.radioGroupTheme.setOnCheckedChangeListener { _, itemId ->
            when(itemId) {
                binding.radioButtonThemeLight.id -> {
                    viewModel.setThemeInPreferences(AppCompatDelegate.MODE_NIGHT_NO)
                    viewModel.changeTheme(AppCompatDelegate.MODE_NIGHT_NO)
                }
                binding.radioButtonThemeDark.id -> {
                    viewModel.setThemeInPreferences(AppCompatDelegate.MODE_NIGHT_YES)
                    viewModel.changeTheme(AppCompatDelegate.MODE_NIGHT_YES)
                }
                binding.radioButtonThemeSystem.id -> {
                    viewModel.setThemeInPreferences(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    viewModel.changeTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }
    }
}