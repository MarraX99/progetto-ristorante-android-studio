package com.projectrestaurant.adapter

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Paint
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.projectrestaurant.IngredientQuantity
import com.projectrestaurant.database.Order
import com.projectrestaurant.database.OrderProduct
import com.projectrestaurant.database.OrderProductEdit

class OrderProductAdapter(private val application: Application)
    : ListAdapter<OrderProduct, OrderProductAdapter.OrderProductViewHolder>(ItemDiffCallback()) {

    private var fullOrders = listOf<Order>()
    private var fullOrderProducts = listOf<OrderProduct>()
    private var currentOrderProducts = listOf<OrderProduct>()
    private var fullOrderProductEdits = listOf<OrderProductEdit>()
    private val dateFormat = SimpleDateFormat.getDateInstance()
    private val foodNames: Array<String> = application.resources.getStringArray(com.projectrestaurant.R.array.food_names)
    private val ingredientNames: Array<String> = application.resources.getStringArray(com.projectrestaurant.R.array.ingredient_names)
    private var stringBuilder = StringBuilder()
    private val PRODUCTS_PER_PAGE = 15
    var numberOfPages: Int = 1
        private set

    private val _currentPage = MutableLiveData(1)
    val currentPage: LiveData<Int>
        get() = _currentPage



    fun setData(orders: List<Order>, orderProducts: List<OrderProduct>, orderProductEdits: List<OrderProductEdit>) {
        fullOrders = orders
        fullOrderProducts = orderProducts
        fullOrderProductEdits = orderProductEdits
        numberOfPages =
            if(fullOrderProducts.size % PRODUCTS_PER_PAGE != 0) (fullOrderProducts.size / PRODUCTS_PER_PAGE) + 1
            else fullOrderProducts.size / PRODUCTS_PER_PAGE
        println("Number of pages: $numberOfPages")
        currentOrderProducts =
            if(numberOfPages > 1) fullOrderProducts.subList(0, PRODUCTS_PER_PAGE).toList()
            else fullOrderProducts.toList()
        submitList(currentOrderProducts)
    }

    //Single element of the list
    class OrderProductViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(com.projectrestaurant.R.id.text_view_name)
        val textViewExtraIngredients: TextView = itemView.findViewById(com.projectrestaurant.R.id.text_view_extra_ingredients)
        val textViewRemovedIngredients: TextView = itemView.findViewById(com.projectrestaurant.R.id.text_view_removed_ingredients)
        val textViewPrice: TextView = itemView.findViewById(com.projectrestaurant.R.id.text_view_price)
        val textViewQuantity: TextView = itemView.findViewById(com.projectrestaurant.R.id.text_view_quantity)
        val textViewOrderDate: TextView = itemView.findViewById(com.projectrestaurant.R.id.text_view_order_date)
        val textViewDeliveryDate: TextView = itemView.findViewById(com.projectrestaurant.R.id.text_view_delivery_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderProductViewHolder {
        val itemLayout = LayoutInflater.from(parent.context).inflate(com.projectrestaurant.R.layout.item_recycler_view_order_product, parent, false)
        return OrderProductViewHolder(itemLayout)
    }

    override fun onBindViewHolder(holder: OrderProductViewHolder, position: Int) {
        with(holder) {
            textViewName.text = foodNames[currentOrderProducts[position].foodId!!]
            textViewQuantity.text = application.resources.getString(com.projectrestaurant.R.string.shopping_cart_product_quantity, currentOrderProducts[position].quantity.toString())
            textViewPrice.text = application.resources.getString(com.projectrestaurant.R.string.shopping_cart_product_price, String.format("%.2f", currentOrderProducts[position].price))
            textViewOrderDate.text = application.resources.getString(com.projectrestaurant.R.string.order_date, dateFormat.format(fullOrders.find{ it.orderId == currentOrderProducts[position].orderId }?.orderDate))
            textViewDeliveryDate.text = application.resources.getString(com.projectrestaurant.R.string.order_delivery_date, dateFormat.format(fullOrders.find{ it.orderId == currentOrderProducts[position].orderId }?.deliveryDate))
            val editList = fullOrderProductEdits.filter{ it.orderProductId == currentOrderProducts[position].orderProductId }
            if(editList.isNotEmpty()) {
                val tmpList = mutableListOf<OrderProductEdit>()
                tmpList.addAll(editList.filter { it.ingredientQuantity == IngredientQuantity.INGREDIENT_EXTRA })
                if(tmpList.isEmpty()) textViewExtraIngredients.visibility = View.GONE
                else {
                    for(edit in tmpList)
                        stringBuilder.append("${application.getString(com.projectrestaurant.R.string.ingredient_extra_prefix)} ${ingredientNames[edit.ingredientId]}\n")
                    textViewExtraIngredients.text = stringBuilder.delete(stringBuilder.lastIndex, stringBuilder.lastIndex)
                }
                tmpList.clear()
                stringBuilder = stringBuilder.clear()
                tmpList.addAll(editList.filter { it.ingredientQuantity == IngredientQuantity.INGREDIENT_REMOVED })
                if(tmpList.isEmpty()) textViewRemovedIngredients.visibility = View.GONE
                else {
                    for(edit in tmpList) stringBuilder.append("${ingredientNames[edit.ingredientId]}\n")
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

    fun goToNextPage(nextPage: Int) {
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

    fun goToPreviousPage(previousPage: Int) {
        if(_currentPage.value!! == 1) return
        else {
            currentOrderProducts = fullOrderProducts.subList((previousPage - 1) * PRODUCTS_PER_PAGE, previousPage * PRODUCTS_PER_PAGE).toList()
            _currentPage.value = _currentPage.value!! - 1
            submitList(currentOrderProducts)
        }
    }

    fun hasMultiplePages(): Boolean { return numberOfPages > 1 }

//    override fun getItemCount(): Int = orderProducts.size

    companion object {
        class ItemDiffCallback : DiffUtil.ItemCallback<OrderProduct>() {
            override fun areItemsTheSame(oldItem: OrderProduct, newItem: OrderProduct): Boolean { return oldItem === newItem }

            override fun areContentsTheSame(oldItem: OrderProduct, newItem: OrderProduct): Boolean { return oldItem.orderProductId == newItem.orderProductId }
        }
    }

}