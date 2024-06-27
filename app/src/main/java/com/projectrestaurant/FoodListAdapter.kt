package com.projectrestaurant

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.projectrestaurant.database.Food
import com.projectrestaurant.ui.order.FragmentFoodListDirections

class FoodListAdapter(private val data: List<Food>, private val navController: NavController, private val context: Context)
    : RecyclerView.Adapter<FoodListAdapter.FoodListViewHolder>() {

    //Single element of the list
    class FoodListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ShapeableImageView = itemView.findViewById(R.id.image_view_food_list)
        val textViewTitle: TextView = itemView.findViewById(R.id.text_view_food_list_title)
        val textViewDescription: TextView = itemView.findViewById(R.id.text_view_food_list_description)
        val textViewPrice: TextView = itemView.findViewById(R.id.text_view_food_list_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodListViewHolder {
        val itemLayout = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_view_food_list, parent, false)
        return FoodListViewHolder(itemLayout)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: FoodListViewHolder, position: Int) {
        Glide.with(context).load(data[position].imageUri)
            .placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(holder.imageView)
        holder.textViewTitle.text = data[position].name
        holder.textViewDescription.text = data[position].description
        holder.textViewPrice.text = context.getString(R.string.shopping_cart_product_price, data[position].unitPrice)
        holder.itemView.setOnClickListener{
            val action = FragmentFoodListDirections.actionFragmentFoodListToFragmentFoodIngredients(data[position])
            navController.navigate(action)
        }
    }
}