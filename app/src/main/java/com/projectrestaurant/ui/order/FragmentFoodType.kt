package com.projectrestaurant.ui.order

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.projectrestaurant.adapter.FoodTypeAdapter
import com.projectrestaurant.databinding.FragmentFoodTypeBinding
import com.projectrestaurant.ui.userorders.ActivityUserOrders
import com.projectrestaurant.viewmodel.FoodOrderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentFoodType: Fragment() {
    private lateinit var binding: FragmentFoodTypeBinding
    private val viewModel: FoodOrderViewModel by activityViewModels()
    private lateinit var navController: NavController
    private lateinit var adapter: FoodTypeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentFoodTypeBinding.inflate(inflater, container, false)
        binding.recyclerViewFoodType.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        navController = findNavController()
        adapter = FoodTypeAdapter(navController, requireActivity().application)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.isIndeterminate = true
        binding.constraintLayout.overlay.add(binding.progressBar)
        if(viewModel.isOnline(requireActivity().application)) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val foodTypeList = viewModel.getFoodTypes()
                withContext(Dispatchers.Main) {
                    if(viewModel.isLoggedIn()) {
                        binding.bottomNavbar.visibility = View.VISIBLE
                        binding.bottomNavbar.setOnItemSelectedListener { menuItem ->
                            when(menuItem.itemId) {
                                com.projectrestaurant.R.id.nav_orders -> {
                                    startActivity(Intent(requireActivity(), ActivityUserOrders::class.java))
                                    return@setOnItemSelectedListener true
                                }
                                com.projectrestaurant.R.id.nav_shopping_cart -> {
                                    navController.navigate(com.projectrestaurant.R.id.action_fragment_food_type_to_fragment_shopping_cart)
                                    return@setOnItemSelectedListener true
                                }
                                else -> return@setOnItemSelectedListener false
                            }
                        }
                        if(!(viewModel.isShoppingCartEmpty()))
                            binding.bottomNavbar.menu.findItem(com.projectrestaurant.R.id.nav_shopping_cart).isVisible = true
                    } else binding.bottomNavbar.visibility = View.GONE
                    adapter.setFoodTypeData(foodTypeList)
                }
            }
        } else { adapter.setFoodTypeData(listOf()) }
        binding.progressBar.visibility = View.GONE
        binding.progressBar.isIndeterminate = false
        binding.constraintLayout.overlay.remove(binding.progressBar)
        binding.recyclerViewFoodType.adapter = adapter
    }
}