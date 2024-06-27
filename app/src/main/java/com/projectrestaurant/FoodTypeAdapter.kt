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
import com.projectrestaurant.database.FoodType
import com.projectrestaurant.ui.order.FragmentFoodTypeDirections

class FoodTypeAdapter(private val data: List<FoodType>, private val navController: NavController, private val context: Context): RecyclerView.Adapter<FoodTypeAdapter.FoodTypeViewHolder>() {

    //Single element of the list
    class FoodTypeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ShapeableImageView = itemView.findViewById(R.id.image_view_food_type)
        val textView: TextView = itemView.findViewById(R.id.text_view_food_type)
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodTypeViewHolder {
        val itemLayout = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_view_food_type, parent, false)
        return FoodTypeViewHolder(itemLayout)
    }

    override fun onBindViewHolder(holder: FoodTypeViewHolder, position: Int) {
        if(position < itemCount) {
            Glide.with(context).load(data[position].imageUri)
                .placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(holder.imageView)
            holder.textView.text = data[position].name
            holder.itemView.setOnClickListener{
                val action = FragmentFoodTypeDirections.actionFragmentFoodTypeToFragmentFoodList(position)
                navController.navigate(action)
            }
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder)
            holder.textView.text = data[0].name
            holder.itemView.setOnClickListener{
                val action = FragmentFoodTypeDirections.actionFragmentFoodTypeToFragmentFoodList(0)
                navController.navigate(action)
            }
        }
    }
}