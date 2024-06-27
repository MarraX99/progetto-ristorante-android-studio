package com.projectrestaurant.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AccountViewModel: ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun logout() { auth.signOut() }
}