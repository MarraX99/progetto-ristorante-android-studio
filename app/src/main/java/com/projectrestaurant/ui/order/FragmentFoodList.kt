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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.projectrestaurant.FoodListAdapter
import com.projectrestaurant.database.Food
import com.projectrestaurant.databinding.FragmentFoodListBinding
import com.projectrestaurant.viewmodel.FoodOrderViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentFoodList: Fragment() {
    private lateinit var binding: FragmentFoodListBinding
    private val viewModel: FoodOrderViewModel by activityViewModels()
    private val args: FragmentFoodListArgs by navArgs<FragmentFoodListArgs>()
    private lateinit var navController: NavController
    private val constraintSet: ConstraintSet = ConstraintSet()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentFoodListBinding.inflate(inflater, container, false)
        binding.recyclerViewFoodList.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        navController = findNavController()
        return binding.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        constraintSet.clone(binding.constraintLayout)
        GlobalScope.launch(Dispatchers.Main) {
            lateinit var foodList: List<Food>
            var isShoppingCartEmpty: Boolean?
            withContext(Dispatchers.IO) {
                foodList = if(viewModel.foodTableExists()) {
                    viewModel.getFoodList(args.foodType, resources.getStringArray(com.projectrestaurant.R.array.food_names),
                        resources.getStringArray(com.projectrestaurant.R.array.food_descriptions))!!
                } else {
                    viewModel.getFoodsFromRemoteDatabase()
                    viewModel.getFoodList(args.foodType, resources.getStringArray(com.projectrestaurant.R.array.food_names),
                        resources.getStringArray(com.projectrestaurant.R.array.food_descriptions))!!
                }
                isShoppingCartEmpty = if(viewModel.isLoggedIn()) viewModel.isShoppingCartEmpty() else null
            }
            if(isShoppingCartEmpty != null && !isShoppingCartEmpty!!) {
                constraintSet.constrainPercentHeight(binding.cardViewFoodList.id, 0.9F)
                constraintSet.constrainPercentHeight(binding.buttonShoppingCart.id, 0.08F)
                constraintSet.applyTo(binding.constraintLayout)
            }
            binding.recyclerViewFoodList.adapter = FoodListAdapter(foodList, view.findNavController(), requireContext())
        }
        binding.buttonShoppingCart.setOnClickListener {
            it.isClickable = false
            binding.progressBar.isIndeterminate = true
            binding.constraintLayout.overlay.add(binding.progressBar)
            binding.progressBar.visibility = View.VISIBLE
            viewModel.resetPrice()
            navController.navigate(com.projectrestaurant.R.id.action_fragment_food_list_to_fragment_shopping_cart)
            binding.progressBar.visibility = View.GONE
            binding.progressBar.isIndeterminate = false
            binding.constraintLayout.overlay.remove(binding.progressBar)
            it.isClickable = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetPrice()
        viewModel.setFoodQuantity(1)
    }
}