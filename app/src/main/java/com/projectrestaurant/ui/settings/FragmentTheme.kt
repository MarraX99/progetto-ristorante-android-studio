package com.projectrestaurant.ui.settings

import android.content.SharedPreferences
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
    private lateinit var editor: SharedPreferences.Editor
    private val viewModel: SettingsViewModel by activityViewModels<SettingsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentSettingsThemeBinding.inflate(inflater, container, false)
        if(activity is ActivitySettings) editor = (activity as ActivitySettings).sharedPrefs.edit()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when((activity as ActivitySettings).sharedPrefs.getInt(getString(com.projectrestaurant.R.string.saved_theme_key), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> binding.radioButtonThemeSystem.isChecked = true
            AppCompatDelegate.MODE_NIGHT_NO -> binding.radioButtonThemeLight.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> binding.radioButtonThemeDark.isChecked = true
        }
        binding.radioGroupTheme.setOnCheckedChangeListener { _, itemId ->
            when(itemId) {
                binding.radioButtonThemeLight.id -> {
                    editor.putInt(getString(com.projectrestaurant.R.string.saved_theme_key), AppCompatDelegate.MODE_NIGHT_NO)
                    viewModel.changeTheme(AppCompatDelegate.MODE_NIGHT_NO)
                }
                binding.radioButtonThemeDark.id -> {
                    editor.putInt(getString(com.projectrestaurant.R.string.saved_theme_key), AppCompatDelegate.MODE_NIGHT_YES)
                    viewModel.changeTheme(AppCompatDelegate.MODE_NIGHT_YES)
                }
                binding.radioButtonThemeSystem.id -> {
                    editor.putInt(getString(com.projectrestaurant.R.string.saved_theme_key), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    viewModel.changeTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
            editor.apply()
        }
    }
}