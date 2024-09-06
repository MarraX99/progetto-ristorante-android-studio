package com.projectrestaurant.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.projectrestaurant.databinding.FragmentAccountBinding
import com.projectrestaurant.viewmodel.AccountViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentAccount: Fragment(), MenuProvider {
    private lateinit var binding: FragmentAccountBinding
    private lateinit var navController: NavController
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val viewModel: AccountViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentAccountBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) { viewModel.getUserData() }
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
        if (activity is ActivityAccount) (activity as ActivityAccount).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.buttonLogout.setOnClickListener { viewModel.logout() }
        binding.buttonChangeAddress.setOnClickListener {
            navController.navigate(com.projectrestaurant.R.id.action_fragment_account_to_fragment_change_delivery_address)
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener { if(it.currentUser == null) activity?.finish() }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(com.projectrestaurant.R.menu.account_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            com.projectrestaurant.R.id.menu_edit -> {
                navController.navigate(com.projectrestaurant.R.id.action_fragment_account_to_fragment_edit_user_data)
                return true
            }
            else -> return false
        }
    }
}