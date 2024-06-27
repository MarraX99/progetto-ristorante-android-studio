package com.projectrestaurant.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.projectrestaurant.CartProduct
import com.projectrestaurant.IngredientAdapter
import com.projectrestaurant.databinding.FragmentFoodIngredientsBinding
import com.projectrestaurant.viewmodel.FoodOrderViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentFoodIngredients: Fragment() {
    private lateinit var binding: FragmentFoodIngredientsBinding
    private val viewModel: FoodOrderViewModel by activityViewModels()
    private val args: FragmentFoodIngredientsArgs by navArgs<FragmentFoodIngredientsArgs>()
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentFoodIngredientsBinding.inflate(inflater, container, false)
        binding.recyclerViewFoodIngredients.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        navController = findNavController()
        return binding.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonIncrement.setOnClickListener {
            viewModel.incrementFoodQuantity()
            viewModel.addToPrice(args.food.unitPrice.toDouble())
        }
        binding.buttonDecrement.setOnClickListener {
            if(viewModel.foodQuantity.value!! > 1) {
                viewModel.decrementFoodQuantity()
                viewModel.removeToPrice(args.food.unitPrice.toDouble())
            }
        }
        viewModel.addToPrice(args.food.unitPrice.toDouble())
        binding.textViewFoodTitle.text = args.food.name
        binding.textViewFoodDescription.text = args.food.description
        Glide.with(this).load(args.food.imageUri)
            .placeholder(com.projectrestaurant.R.drawable.placeholder)
            .error(com.projectrestaurant.R.drawable.placeholder).into(binding.imageViewFood)
        GlobalScope.launch(Dispatchers.Main) {
            val ingredientList = withContext(Dispatchers.IO) {
                if(viewModel.ingredientTableExists()) { viewModel.getIngredientsFromFood(args.food, resources.getStringArray(com.projectrestaurant.R.array.ingredient_names)) }
                else {
                    viewModel.getIngredientsFromRemoteDatabase()
                    viewModel.getIngredientsFromFood(args.food, resources.getStringArray(com.projectrestaurant.R.array.ingredient_names))
                }
            }
            if (ingredientList != null) {
                if(ingredientList.isEmpty()) {
                    binding.recyclerViewFoodIngredients.adapter = IngredientAdapter(listOf(), requireContext(), viewModel)
                } else {
                    binding.textView3.visibility = View.VISIBLE
                    binding.materialDivider2.visibility = View.VISIBLE
                    binding.recyclerViewFoodIngredients.adapter = IngredientAdapter(ingredientList, requireContext(), viewModel)
                }
            }
            binding.textViewShoppingCartTitle.text = resources.getString(com.projectrestaurant.R.string.shopping_cart_add_title, viewModel.foodQuantity.value.toString())
            binding.cardViewShoppingCart.setOnClickListener {
                it.isClickable = false
                binding.progressBar.isIndeterminate = true
                binding.constraintLayout.overlay.add(binding.progressBar)
                binding.progressBar.visibility = View.VISIBLE
                if(viewModel.isLoggedIn()) {
                    GlobalScope.launch(Dispatchers.Main) {
                        val result = withContext(Dispatchers.IO) { viewModel.addProductToShoppingCart(CartProduct(
                            viewModel.createCartProductOnDatabase(), args.food,
                            (binding.recyclerViewFoodIngredients.adapter as IngredientAdapter).getExtraIngredients(),
                            (binding.recyclerViewFoodIngredients.adapter as IngredientAdapter).getRemovedIngredients(),
                            viewModel.foodQuantity.value!!, viewModel.totalPriceString.value!!))
                        }
                        if(result) navController.navigateUp()
                        else {
                            AlertDialog.Builder(requireContext()).setTitle(com.projectrestaurant.R.string.shopping_cart_error_title)
                                .setMessage(com.projectrestaurant.R.string.shopping_cart_add_product_error_message)
                                .setNeutralButton(com.projectrestaurant.R.string.ok) {
                                        _, _ -> navController.navigateUp() }.show()
                        }
                    }
                } else {
                    AlertDialog.Builder(requireContext()).setTitle(com.projectrestaurant.R.string.shopping_cart_error_title)
                        .setMessage(com.projectrestaurant.R.string.shopping_cart_add_product_error_message_no_auth)
                        .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> navController.navigateUp() }.show()
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setFoodQuantity(1)
        viewModel.resetPrice()
    }
}