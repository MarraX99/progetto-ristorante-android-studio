package com.projectrestaurant.adapter

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.projectrestaurant.database.FoodType
import com.projectrestaurant.ui.order.FragmentFoodTypeDirections

class FoodTypeAdapter(private val navController: NavController, private val application: Application): ListAdapter<FoodType , FoodTypeAdapter.FoodTypeViewHolder>(
    ItemDiffCallback()
) {

    private var data: List<FoodType> = listOf()

    //Single element of the list
    inner class FoodTypeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ShapeableImageView = itemView.findViewById(com.projectrestaurant.R.id.image_view_food_type)
        val textView: TextView = itemView.findViewById(com.projectrestaurant.R.id.text_view_food_type)
    }

    fun setFoodTypeData(data: List<FoodType>) {
        this.data = data
        submitList(this.data)
    }

//    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodTypeViewHolder {
        val itemLayout = LayoutInflater.from(parent.context).inflate(com.projectrestaurant.R.layout.item_recycler_view_food_type, parent, false)
        return FoodTypeViewHolder(itemLayout)
    }

    override fun onBindViewHolder(holder: FoodTypeViewHolder, position: Int) {
        if(position < itemCount) {
            Glide.with(application).load(data[position].imageUri)
                .placeholder(com.projectrestaurant.R.drawable.placeholder).error(com.projectrestaurant.R.drawable.placeholder).into(holder.imageView)
            holder.imageView.contentDescription = data[position].name
            holder.textView.text = data[position].name
            holder.itemView.setOnClickListener{
                val action = FragmentFoodTypeDirections.actionFragmentFoodTypeToFragmentFoodList(position)
                navController.navigate(action)
            }
        } else {
            holder.imageView.setImageResource(com.projectrestaurant.R.drawable.placeholder)
            holder.textView.text = data[0].name
            holder.itemView.setOnClickListener{
                val action = FragmentFoodTypeDirections.actionFragmentFoodTypeToFragmentFoodList(0)
                navController.navigate(action)
            }
        }
    }

    companion object {
        class ItemDiffCallback: DiffUtil.ItemCallback<FoodType>() {
            override fun areItemsTheSame(oldItem: FoodType, newItem: FoodType): Boolean { return oldItem === newItem }

            override fun areContentsTheSame(oldItem: FoodType, newItem: FoodType): Boolean { return oldItem.typeId == newItem.typeId }
        }
    }
}