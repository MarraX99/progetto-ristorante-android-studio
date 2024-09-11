package com.projectrestaurant.adapter

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.projectrestaurant.database.FoodType
import com.projectrestaurant.databinding.ItemRecyclerViewFoodTypeBinding
import com.projectrestaurant.ui.order.FragmentFoodTypeDirections

class FoodTypeAdapter(private val navController: NavController, private val application: Application): ListAdapter<FoodType , FoodTypeAdapter.FoodTypeViewHolder>(
    ItemDiffCallback()
) {

    private var data: List<FoodType> = listOf()

    //Single element of the list
    inner class FoodTypeViewHolder(val binding: ItemRecyclerViewFoodTypeBinding)
        : RecyclerView.ViewHolder(binding.root)

    fun setFoodTypeData(data: List<FoodType>) {
        this.data = data
        submitList(this.data)
    }

//    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodTypeViewHolder {
        val binding = ItemRecyclerViewFoodTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodTypeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodTypeViewHolder, position: Int) {
        with(holder) {
            with(data[position]) {
                if(position < itemCount) {
                    Glide.with(application).load(imageUri)
                        .placeholder(com.projectrestaurant.R.drawable.placeholder).error(com.projectrestaurant.R.drawable.placeholder).into(binding.imageViewFoodType)
                    binding.imageViewFoodType.contentDescription = name
                    binding.textViewFoodType.text = name
                    holder.itemView.setOnClickListener{
                        val action = FragmentFoodTypeDirections.actionFragmentFoodTypeToFragmentFoodList(position)
                        navController.navigate(action)
                    }
                } else {
                    binding.imageViewFoodType.setImageResource(com.projectrestaurant.R.drawable.placeholder)
                    binding.textViewFoodType.text = data[0].name
                    binding.cardViewFoodType.setOnClickListener{
                        val action = FragmentFoodTypeDirections.actionFragmentFoodTypeToFragmentFoodList(0)
                        navController.navigate(action)
                    }
                }
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