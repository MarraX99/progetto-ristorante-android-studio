package com.projectrestaurant.adapter

import android.app.Application
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.projectrestaurant.IngredientQuantity
import com.projectrestaurant.database.OrderProduct
import com.projectrestaurant.database.OrderProductEdit
import com.projectrestaurant.databinding.ItemRecyclerViewOrderProductBinding
import com.projectrestaurant.viewmodel.OrdersViewModel

class OrderProductAdapter(private val application: Application, private val viewModel: OrdersViewModel)
    : ListAdapter<OrderProduct, OrderProductAdapter.OrderProductViewHolder>(ItemDiffCallback()) {

    private lateinit var fullOrderProducts: List<OrderProduct>
    private lateinit var currentOrderProducts: List<OrderProduct>
    private lateinit var fullOrderProductEdits: List<OrderProductEdit>
    private lateinit var productImages: HashMap<Int,String?>
    private var stringBuilder = StringBuilder()
    private val PRODUCTS_PER_PAGE = 15
    var numberOfPages: Int = 1
        private set
    private val _currentPage = MutableLiveData(1)
    val currentPage: LiveData<Int>
        get() = _currentPage

    fun setData(orderProducts: List<OrderProduct>, orderProductEdits: List<OrderProductEdit>, images: HashMap<Int,String?>) {
        fullOrderProducts = orderProducts
        fullOrderProductEdits = orderProductEdits
        productImages = images
        numberOfPages =
            if(fullOrderProducts.size % PRODUCTS_PER_PAGE != 0) (fullOrderProducts.size / PRODUCTS_PER_PAGE) + 1
            else fullOrderProducts.size / PRODUCTS_PER_PAGE
        currentOrderProducts = if(numberOfPages > 1) fullOrderProducts.subList(0, PRODUCTS_PER_PAGE) else fullOrderProducts
        submitList(currentOrderProducts)
    }

    //Single element of the list
    class OrderProductViewHolder(val binding: ItemRecyclerViewOrderProductBinding): RecyclerView.ViewHolder(binding.root) {
        lateinit var productId: String
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderProductViewHolder {
        val binding = ItemRecyclerViewOrderProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderProductViewHolder, position: Int) {
        holder.productId = currentOrderProducts[position].orderProductId
        with(holder.binding) {
            textViewName.text = viewModel.foodNames[currentOrderProducts[position].foodId]
            textViewQuantity.text = application.resources.getString(com.projectrestaurant.R.string.shopping_cart_product_quantity, currentOrderProducts[position].quantity.toString())
            textViewPrice.text = application.resources.getString(com.projectrestaurant.R.string.shopping_cart_product_price, String.format("%.2f", currentOrderProducts[position].price))
            imageViewFood.contentDescription = viewModel.foodNames[currentOrderProducts[position].foodId]
            Glide.with(application).load(productImages[currentOrderProducts[position].foodId])
                .placeholder(com.projectrestaurant.R.drawable.placeholder).error(com.projectrestaurant.R.drawable.placeholder).into(imageViewFood)
            val editList = fullOrderProductEdits.filter{ it.orderProductId == currentOrderProducts[position].orderProductId }
            if(editList.isNotEmpty()) {
                val tmpList = mutableListOf<OrderProductEdit>()
                tmpList.addAll(editList.filter { it.ingredientQuantity == IngredientQuantity.INGREDIENT_EXTRA })
                if(tmpList.isEmpty()) textViewExtraIngredients.visibility = View.GONE
                else {
                    for(edit in tmpList)
                        stringBuilder.append("${application.getString(com.projectrestaurant.R.string.ingredient_extra_prefix)} ${viewModel.ingredientNames[edit.ingredientId]}\n")
                    textViewExtraIngredients.text = stringBuilder.deleteCharAt(stringBuilder.lastIndex)
                }
                tmpList.clear()
                stringBuilder = stringBuilder.clear()
                tmpList.addAll(editList.filter { it.ingredientQuantity == IngredientQuantity.INGREDIENT_REMOVED })
                if(tmpList.isEmpty()) textViewRemovedIngredients.visibility = View.GONE
                else {
                    for(edit in tmpList) stringBuilder.append("${viewModel.ingredientNames[edit.ingredientId]}\n")
                    textViewRemovedIngredients.text = stringBuilder.delete(stringBuilder.lastIndex, stringBuilder.lastIndex)
                    textViewRemovedIngredients.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                }
                stringBuilder = stringBuilder.clear()
            } else {
                textViewExtraIngredients.visibility = View.GONE
                textViewRemovedIngredients.visibility = View.GONE
            }
        }
    }

    fun goToNextPage(nextPage: Int = _currentPage.value!! + 1) {
        if(_currentPage.value == numberOfPages) return
        else {
            currentOrderProducts =
                if(nextPage * PRODUCTS_PER_PAGE > fullOrderProducts.lastIndex)
                fullOrderProducts.subList(_currentPage.value!! * PRODUCTS_PER_PAGE, fullOrderProducts.lastIndex + 1).toList()
            else fullOrderProducts.subList(_currentPage.value!! * PRODUCTS_PER_PAGE, nextPage * PRODUCTS_PER_PAGE).toList()
            _currentPage.value = _currentPage.value!! + 1
            submitList(currentOrderProducts)
        }
    }

    fun goToPreviousPage(previousPage: Int = _currentPage.value!! - 1) {
        if(_currentPage.value!! == 1) return
        else {
            currentOrderProducts = fullOrderProducts.subList((previousPage - 1) * PRODUCTS_PER_PAGE, previousPage * PRODUCTS_PER_PAGE).toList()
            _currentPage.value = _currentPage.value!! - 1
            submitList(currentOrderProducts)
        }
    }

    fun hasMultiplePages(): Boolean { return numberOfPages > 1 }

    companion object {
        class ItemDiffCallback : DiffUtil.ItemCallback<OrderProduct>() {
            override fun areItemsTheSame(oldItem: OrderProduct, newItem: OrderProduct): Boolean { return oldItem === newItem }

            override fun areContentsTheSame(oldItem: OrderProduct, newItem: OrderProduct): Boolean { return oldItem.orderProductId == newItem.orderProductId }
        }
    }
}