package com.projectrestaurant

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.projectrestaurant.database.Ingredient
import com.projectrestaurant.viewmodel.FoodOrderViewModel

class IngredientAdapter(private val data: List<Ingredient>, private val context: Context, private val viewModel: FoodOrderViewModel)
    : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    private val extraIngredients = arrayListOf<Ingredient>()
    private val removedIngredients = arrayListOf<Ingredient>()

    //Single element of the list
    class IngredientViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ShapeableImageView = itemView.findViewById(R.id.image_view_ingredient)
        val textViewName: TextView = itemView.findViewById(R.id.text_view_ingredient_name)
        val textViewPrice: TextView = itemView.findViewById(R.id.text_view_ingredient_price)
        val buttonIncrement: ImageButton = itemView.findViewById(R.id.button_increment)
        val buttonDecrement: ImageButton = itemView.findViewById(R.id.button_decrement)
        var quantity: Int = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val itemLayout = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_view_ingredient_list, parent, false)
        return IngredientViewHolder(itemLayout)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        Glide.with(context).load(data[position].imageUri)
            .placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(holder.imageView)
//        holder.imageView.setImageResource(R.drawable.placeholder)
        holder.textViewName.text = data[position].name
        holder.textViewPrice.text = context.getString(R.string.shopping_cart_product_price_plus, data[position].unitPrice)
        if(extraIngredients.contains(data[position])) {
            holder.textViewPrice.visibility = View.VISIBLE
            holder.quantity = 2
        }
        if(removedIngredients.contains(data[position])) {
            holder.textViewName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            holder.quantity = 0
        }
        holder.buttonDecrement.setOnClickListener {
            when(holder.quantity) {
                0 -> return@setOnClickListener
                1 -> {
                    holder.textViewName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    holder.quantity--
                    removedIngredients.add(data[position]) }
                2 -> {
                    holder.textViewPrice.visibility = View.GONE
                    holder.quantity--
                    viewModel.removeToPrice(data[position].unitPrice.toDouble())
                    extraIngredients.remove(data[position]) }
                else -> return@setOnClickListener
            }
        }
        holder.buttonIncrement.setOnClickListener {
            when(holder.quantity) {
                0 -> {
                    holder.textViewName.paintFlags = Paint.ANTI_ALIAS_FLAG
                    holder.quantity++
                    removedIngredients.remove(data[position]) }
                1 -> {
                    holder.textViewPrice.visibility = View.VISIBLE
                    holder.quantity++
                    viewModel.addToPrice(data[position].unitPrice.toDouble())
                    extraIngredients.add(data[position]) }
                2 -> return@setOnClickListener
                else -> return@setOnClickListener
            }
        }
    }

    fun addToExtraIngredients(vararg ingredients: Ingredient) { for(i in ingredients) extraIngredients.add(i) }

    fun removeToExtraIngredients(vararg ingredients: Ingredient) { for(i in ingredients) extraIngredients.remove(i) }

    fun addToRemovedIngredients(vararg ingredients: Ingredient) { for(i in ingredients) removedIngredients.add(i) }

    fun removeToRemovedIngredients(vararg ingredients: Ingredient) { for(i in ingredients) removedIngredients.remove(i) }

    fun getExtraIngredients(): ArrayList<Ingredient> { return extraIngredients }

    fun getRemovedIngredients(): ArrayList<Ingredient> { return removedIngredients }
}