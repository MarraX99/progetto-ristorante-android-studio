package com.projectrestaurant.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.projectrestaurant.CartProduct
import com.projectrestaurant.adapter.IngredientAdapter
import com.projectrestaurant.database.Food
import com.projectrestaurant.database.Ingredient
import com.projectrestaurant.databinding.FragmentShoppingCartEditProductBinding
import com.projectrestaurant.viewmodel.FoodOrderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentEditCartProduct: Fragment() {
    private lateinit var binding: FragmentShoppingCartEditProductBinding
    private lateinit var navController: NavController
    private val viewModel: FoodOrderViewModel by activityViewModels()
    private val args: FragmentEditCartProductArgs by navArgs<FragmentEditCartProductArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentShoppingCartEditProductBinding.inflate(inflater, container, false)
        binding.recyclerViewFoodIngredients.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewFoodIngredients.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setFoodQuantity(args.bundle.getInt("quantity"))
        viewModel.addToPrice(args.bundle.getDouble("price"))
        val food = args.bundle.getParcelable<Food>("food")
        if (food != null) {
            binding.textViewFoodTitle.text = food.name
            binding.textViewFoodDescription.text = food.description
            Glide.with(this).load(food.imageUri)
                .placeholder(com.projectrestaurant.R.drawable.placeholder)
                .error(com.projectrestaurant.R.drawable.placeholder).into(binding.imageViewFood)
        }
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            val ingredientList = withContext(Dispatchers.IO) { viewModel.getIngredientsFromFood(food!!) }
            val adapter = IngredientAdapter(ingredientList, requireActivity().application, viewModel)
            if(ingredientList.isNotEmpty()) {
                var tmp = args.bundle.getParcelableArrayList<Ingredient>("extraIngredients")
                if (tmp != null) for(i in tmp) adapter.addToExtraIngredients(i)
                tmp = args.bundle.getParcelableArrayList("removedIngredients")
                if (tmp != null) for(i in tmp) adapter.addToRemovedIngredients(i)
                binding.textView3.visibility = View.VISIBLE
                binding.materialDivider2.visibility = View.VISIBLE
                binding.recyclerViewFoodIngredients.adapter = adapter
            }
        }
        binding.buttonIncrement.setOnClickListener {
            viewModel.incrementFoodQuantity()
            viewModel.addToPrice(food!!.unitPrice)
        }
        binding.buttonDecrement.setOnClickListener {
            if(viewModel.foodQuantity.value!! > 1) {
                viewModel.decrementFoodQuantity()
                viewModel.removeToPrice(food!!.unitPrice)
            }
        }
        binding.cardViewShoppingCart.setOnClickListener{
            it.isClickable = false
            binding.progressBar.isIndeterminate = true
            binding.constraintLayout.overlay.add(binding.progressBar)
            binding.progressBar.visibility = View.VISIBLE
            args.bundle.putParcelableArrayList("extraIngredients",
                (binding.recyclerViewFoodIngredients.adapter as IngredientAdapter).getExtraIngredients())
            args.bundle.putParcelableArrayList("removedIngredients",
                (binding.recyclerViewFoodIngredients.adapter as IngredientAdapter).getRemovedIngredients())
            args.bundle.putInt("quantity", viewModel.foodQuantity.value!!)
            args.bundle.putDouble("price", viewModel.totalPrice.value!!)
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                val result = withContext(Dispatchers.IO){
                    viewModel.updateCartProduct(CartProduct(
                        args.bundle.getString("cartProductId")!!,
                        args.bundle.getParcelable("food")!!,
                        args.bundle.getParcelableArrayList("extraIngredients")!!,
                        args.bundle.getParcelableArrayList("removedIngredients")!!,
                        args.bundle.getInt("quantity"), args.bundle.getDouble("price")))
                }
                if(result) {
                    setFragmentResult("modifiedCartProduct", args.bundle)
                    navController.navigateUp()
                }
                else {
                    AlertDialog.Builder(requireContext())
                        .setTitle(com.projectrestaurant.R.string.shopping_cart_error_title)
                        .setMessage(com.projectrestaurant.R.string.shopping_cart_edit_product_error_message)
                        .setNeutralButton(com.projectrestaurant.R.string.ok) {
                                _, _ -> navController.navigateUp() }.show()
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