package com.projectrestaurant.adapter

import android.app.Application
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.projectrestaurant.IngredientQuantity
import com.projectrestaurant.database.Ingredient
import com.projectrestaurant.viewmodel.FoodOrderViewModel

class IngredientAdapter(private val data: List<Ingredient>, private val application: Application, private val viewModel: FoodOrderViewModel)
    : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    private val extraIngredients = arrayListOf<Ingredient>()
    private val removedIngredients = arrayListOf<Ingredient>()

    //Single element of the list
    inner class IngredientViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ShapeableImageView = itemView.findViewById(com.projectrestaurant.R.id.image_view_ingredient)
        val textViewName: TextView = itemView.findViewById(com.projectrestaurant.R.id.text_view_ingredient_name)
        val textViewPrice: TextView = itemView.findViewById(com.projectrestaurant.R.id.text_view_ingredient_price)
        val buttonIncrement: ImageButton = itemView.findViewById(com.projectrestaurant.R.id.button_increment)
        val buttonDecrement: ImageButton = itemView.findViewById(com.projectrestaurant.R.id.button_decrement)
        var quantity: IngredientQuantity = IngredientQuantity.INGREDIENT_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val itemLayout = LayoutInflater.from(parent.context).inflate(com.projectrestaurant.R.layout.item_recycler_view_ingredient_list, parent, false)
        return IngredientViewHolder(itemLayout)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        Glide.with(application).load(data[position].imageUri)
            .placeholder(com.projectrestaurant.R.drawable.placeholder).error(com.projectrestaurant.R.drawable.placeholder).into(holder.imageView)
        holder.imageView.contentDescription = data[position].name
        holder.textViewName.text = data[position].name
        holder.textViewPrice.text = application.getString(com.projectrestaurant.R.string.shopping_cart_product_price_plus, String.format("%.2f", data[position].unitPrice))
        if(extraIngredients.contains(data[position])) {
            holder.textViewPrice.visibility = View.VISIBLE
            holder.quantity = IngredientQuantity.INGREDIENT_EXTRA
        }
        if(removedIngredients.contains(data[position])) {
            holder.textViewName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            holder.quantity = IngredientQuantity.INGREDIENT_REMOVED
        }
        holder.buttonDecrement.setOnClickListener {
            when(holder.quantity) {
                IngredientQuantity.INGREDIENT_REMOVED -> return@setOnClickListener
                IngredientQuantity.INGREDIENT_NORMAL -> {
                    holder.textViewName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    holder.quantity = IngredientQuantity.INGREDIENT_REMOVED
                    removedIngredients.add(data[position])
                }
                IngredientQuantity.INGREDIENT_EXTRA -> {
                    holder.textViewPrice.visibility = View.GONE
                    holder.quantity = IngredientQuantity.INGREDIENT_NORMAL
                    viewModel.removeToPrice(data[position].unitPrice)
                    extraIngredients.remove(data[position])
                }
            }
        }
        holder.buttonIncrement.setOnClickListener {
            when(holder.quantity) {
                IngredientQuantity.INGREDIENT_REMOVED -> {
                    holder.textViewName.paintFlags = Paint.ANTI_ALIAS_FLAG
                    holder.quantity = IngredientQuantity.INGREDIENT_NORMAL
                    removedIngredients.remove(data[position])
                }
                IngredientQuantity.INGREDIENT_NORMAL -> {
                    holder.textViewPrice.visibility = View.VISIBLE
                    holder.quantity = IngredientQuantity.INGREDIENT_EXTRA
                    viewModel.addToPrice(data[position].unitPrice)
                    extraIngredients.add(data[position])
                }
                IngredientQuantity.INGREDIENT_EXTRA -> return@setOnClickListener
            }
        }
    }

    fun addToExtraIngredients(vararg ingredients: Ingredient) { for(i in ingredients) extraIngredients.add(i) }

//    fun removeToExtraIngredients(vararg ingredients: Ingredient) { for(i in ingredients) extraIngredients.remove(i) }

    fun addToRemovedIngredients(vararg ingredients: Ingredient) { for(i in ingredients) removedIngredients.add(i) }

//    fun removeToRemovedIngredients(vararg ingredients: Ingredient) { for(i in ingredients) removedIngredients.remove(i) }

    fun getExtraIngredients(): ArrayList<Ingredient> { return extraIngredients }

    fun getRemovedIngredients(): ArrayList<Ingredient> { return removedIngredients }
}