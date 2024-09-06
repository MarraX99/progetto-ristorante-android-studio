package com.projectrestaurant.ui.order

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
import com.projectrestaurant.database.FoodType
import com.projectrestaurant.databinding.FragmentFoodTypeBinding
import com.projectrestaurant.viewmodel.FoodOrderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentFoodType: Fragment() {
    private lateinit var binding: FragmentFoodTypeBinding
    private val viewModel: FoodOrderViewModel by activityViewModels()
    private lateinit var navController: NavController
    private lateinit var foodTypeList: List<FoodType>
    private lateinit var adapter: FoodTypeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentFoodTypeBinding.inflate(inflater, container, false)
        binding.recyclerViewFoodType.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity is ActivityOrder) (activity as ActivityOrder).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if(viewModel.isOnline(requireContext().applicationContext)) {
            adapter = FoodTypeAdapter(navController, requireActivity().application)
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                var isShoppingCartEmpty: Boolean?
                withContext(Dispatchers.IO) {
                    foodTypeList = viewModel.getFoodTypes()
                    isShoppingCartEmpty = if(viewModel.isLoggedIn()) viewModel.isShoppingCartEmpty() else null
                }
                if(isShoppingCartEmpty != null && !isShoppingCartEmpty!!)
                    binding.buttonShoppingCart.visibility = View.VISIBLE
               adapter.setFoodTypeData(foodTypeList)
            }
        } else adapter.setFoodTypeData(listOf())
        binding.recyclerViewFoodType.adapter = adapter

        binding.buttonShoppingCart.setOnClickListener {
            it.isClickable = false
            binding.progressBar.isIndeterminate = true
            binding.constraintLayout.overlay.add(binding.progressBar)
            binding.progressBar.visibility = View.VISIBLE
            navController.navigate(com.projectrestaurant.R.id.action_fragment_food_type_to_fragment_shopping_cart)
            binding.progressBar.visibility = View.GONE
            binding.progressBar.isIndeterminate = false
            binding.constraintLayout.overlay.remove(binding.progressBar)
            it.isClickable = true
        }
    }
}