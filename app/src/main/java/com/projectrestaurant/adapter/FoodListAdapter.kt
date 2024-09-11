package com.projectrestaurant.adapter

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.projectrestaurant.database.Food
import com.projectrestaurant.databinding.ItemRecyclerViewFoodListBinding
import com.projectrestaurant.ui.order.FragmentFoodListDirections

class FoodListAdapter(private val navController: NavController, private val application: Application):
    ListAdapter<Food, FoodListAdapter.FoodListViewHolder>(ItemDiffCallback()), Filterable {

    private lateinit var fullData: List<Food>
    private lateinit var currentData: List<Food>

    fun setFoodData(data: List<Food>) {
        this.fullData = data
        this.currentData = data
        submitList(currentData)
    }

    //Single element of the list
    inner class FoodListViewHolder(val binding: ItemRecyclerViewFoodListBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodListViewHolder {
        val binding = ItemRecyclerViewFoodListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodListViewHolder(binding)
    }

//    override fun getItemCount(): Int = fullData.size

    override fun onBindViewHolder(holder: FoodListViewHolder, position: Int) {
        val element = currentData.elementAt(position)
        with(holder) {
            Glide.with(application).load(element.imageUri)
                .placeholder(com.projectrestaurant.R.drawable.placeholder).error(com.projectrestaurant.R.drawable.placeholder).into(binding.imageViewFoodList)
            binding.imageViewFoodList.contentDescription = element.name
            binding.textViewFoodListTitle.text = element.name
            binding.textViewFoodListDescription.text = element.description
            binding.textViewFoodListPrice.text = application.getString(com.projectrestaurant.R.string.shopping_cart_product_price, String.format("%.2f", element.unitPrice))
            binding.cardViewFoodList.setOnClickListener{
                val action = FragmentFoodListDirections.actionFragmentFoodListToFragmentFoodIngredients(element)
                navController.navigate(action)
            }
        }
    }

    companion object {
        class ItemDiffCallback : DiffUtil.ItemCallback<Food>() {
            override fun areItemsTheSame(oldItem: Food, newItem: Food): Boolean { return oldItem === newItem }

            override fun areContentsTheSame(oldItem: Food, newItem: Food): Boolean { return oldItem.foodId == newItem.foodId }
        }
    }

    override fun getFilter(): Filter { return searchFilter }

    @Suppress("UNCHECKED_CAST")
    private val searchFilter: Filter = object: Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            currentData = if(constraint.isNullOrEmpty()) fullData
            else {
                val tmp = mutableSetOf<Food>()
                tmp.addAll(fullData.filter { it.name.startsWith(constraint.toString(), ignoreCase = true) ||
                        it.description.contains(constraint.toString(), ignoreCase = true) })
                tmp.toList()
            }
            return FilterResults().apply {
                count = currentData.size
                values = currentData
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            submitList(results!!.values as List<Food>)
        }
    }
}