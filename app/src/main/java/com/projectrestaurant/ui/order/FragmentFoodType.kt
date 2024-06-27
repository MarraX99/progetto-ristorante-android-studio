package com.projectrestaurant.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.projectrestaurant.FoodTypeAdapter
import com.projectrestaurant.database.FoodType
import com.projectrestaurant.databinding.FragmentFoodTypeBinding
import com.projectrestaurant.viewmodel.FoodOrderViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentFoodType: Fragment() {
    private lateinit var binding: FragmentFoodTypeBinding
    private val viewModel: FoodOrderViewModel by activityViewModels()
    private lateinit var navController: NavController
    private val constraintSet: ConstraintSet = ConstraintSet()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentFoodTypeBinding.inflate(inflater, container, false)
        binding.recyclerViewFoodType.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        navController = findNavController()
        return binding.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity is ActivityOrder) (activity as ActivityOrder).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if(viewModel.isOnline(requireContext().applicationContext)) {
            constraintSet.clone(binding.constraintLayout)
            GlobalScope.launch(Dispatchers.Main) {
                lateinit var foodTypeList: List<FoodType>
                var isShoppingCartEmpty: Boolean?
                withContext(Dispatchers.IO) {
                    foodTypeList = if(viewModel.foodTypeTableExists()) viewModel.getFoodTypes(resources.getStringArray(com.projectrestaurant.R.array.food_types))!!
                    else {
                        viewModel.getFoodTypesFromRemoteDatabase()
                        viewModel.getFoodTypes(resources.getStringArray(com.projectrestaurant.R.array.food_types))!!
                    }
                    isShoppingCartEmpty = if(viewModel.isLoggedIn()) viewModel.isShoppingCartEmpty() else null
                }
                if(isShoppingCartEmpty != null && !isShoppingCartEmpty!!) {
                    constraintSet.constrainPercentHeight(binding.cardViewFoodType.id, 0.9F)
                    constraintSet.constrainPercentHeight(binding.buttonShoppingCart.id, 0.08F)
                    constraintSet.applyTo(binding.constraintLayout)
                }
                binding.recyclerViewFoodType.adapter = FoodTypeAdapter(foodTypeList, view.findNavController(), requireContext())
            }
        } else binding.recyclerViewFoodType.adapter = FoodTypeAdapter(listOf(), view.findNavController(), requireContext())

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