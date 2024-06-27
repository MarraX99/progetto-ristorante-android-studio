package com.projectrestaurant.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel

class SettingsViewModel: ViewModel() {

    fun changeTheme(mode: Int) = AppCompatDelegate.setDefaultNightMode(mode)
}