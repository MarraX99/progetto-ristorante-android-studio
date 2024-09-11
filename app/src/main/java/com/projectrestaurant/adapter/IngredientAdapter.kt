package com.projectrestaurant.adapter

import android.app.Application
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.projectrestaurant.IngredientQuantity
import com.projectrestaurant.database.Ingredient
import com.projectrestaurant.databinding.ItemRecyclerViewIngredientListBinding
import com.projectrestaurant.viewmodel.FoodOrderViewModel

class IngredientAdapter(private val data: List<Ingredient>, private val application: Application, private val viewModel: FoodOrderViewModel)
    : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    private val extraIngredients = arrayListOf<Ingredient>()
    private val removedIngredients = arrayListOf<Ingredient>()

    //Single element of the list
    inner class IngredientViewHolder(val binding: ItemRecyclerViewIngredientListBinding)
        : RecyclerView.ViewHolder(binding.root) {
        var quantity: IngredientQuantity = IngredientQuantity.INGREDIENT_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemRecyclerViewIngredientListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IngredientViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        with(holder) {
            with(data[position]) {
                Glide.with(application).load(imageUri)
                    .placeholder(com.projectrestaurant.R.drawable.placeholder).error(com.projectrestaurant.R.drawable.placeholder).into(binding.imageViewIngredient)
                binding.imageViewIngredient.contentDescription = name
                binding.textViewIngredientName.text = name
                binding.textViewIngredientPrice.text = application.getString(com.projectrestaurant.R.string.shopping_cart_product_price_plus, String.format("%.2f", unitPrice))
            }
            if(extraIngredients.contains(data[position])) {
                binding.textViewIngredientPrice.isVisible = true
                quantity = IngredientQuantity.INGREDIENT_EXTRA
            }
            if(removedIngredients.contains(data[position])) {
                binding.textViewIngredientName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                quantity = IngredientQuantity.INGREDIENT_REMOVED
            }
            binding.buttonDecrement.setOnClickListener {
                when(quantity) {
                    IngredientQuantity.INGREDIENT_REMOVED -> return@setOnClickListener
                    IngredientQuantity.INGREDIENT_NORMAL -> {
                        binding.textViewIngredientName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                        quantity = IngredientQuantity.INGREDIENT_REMOVED
                        removedIngredients.add(data[position])
                    }
                    IngredientQuantity.INGREDIENT_EXTRA -> {
                        binding.textViewIngredientPrice.isVisible = false
                        quantity = IngredientQuantity.INGREDIENT_NORMAL
                        viewModel.removeToPrice(data[position].unitPrice)
                        extraIngredients.remove(data[position])
                    }
                }
            }
            binding.buttonIncrement.setOnClickListener {
                when(quantity) {
                    IngredientQuantity.INGREDIENT_EXTRA -> return@setOnClickListener
                    IngredientQuantity.INGREDIENT_NORMAL -> {
                        binding.textViewIngredientPrice.isVisible = true
                        quantity = IngredientQuantity.INGREDIENT_EXTRA
                        viewModel.addToPrice(data[position].unitPrice)
                        extraIngredients.add(data[position])
                    }
                    IngredientQuantity.INGREDIENT_REMOVED -> {
                        binding.textViewIngredientName.paintFlags = Paint.ANTI_ALIAS_FLAG
                        quantity = IngredientQuantity.INGREDIENT_NORMAL
                        removedIngredients.remove(data[position])
                    }
                }
            }
        }
    }

    fun addToExtraIngredients(vararg ingredients: Ingredient) { for(i in ingredients) extraIngredients.add(i) }

    fun addToRemovedIngredients(vararg ingredients: Ingredient) { for(i in ingredients) removedIngredients.add(i) }

    fun getExtraIngredients(): ArrayList<Ingredient> { return extraIngredients }

    fun getRemovedIngredients(): ArrayList<Ingredient> { return removedIngredients }
}