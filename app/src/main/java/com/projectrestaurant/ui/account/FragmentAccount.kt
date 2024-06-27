package com.projectrestaurant.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.projectrestaurant.databinding.FragmentAccountBinding
import com.projectrestaurant.viewmodel.AccountViewModel

class FragmentAccount: Fragment() {
    private lateinit var binding: FragmentAccountBinding
    private lateinit var navController: NavController
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val viewModel: AccountViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentAccountBinding.inflate(layoutInflater, container, false)
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(activity is ActivityAccount) (activity as ActivityAccount).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.buttonLogout.setOnClickListener { viewModel.logout() }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener { if(it.currentUser == null) activity?.finish() }
    }
}