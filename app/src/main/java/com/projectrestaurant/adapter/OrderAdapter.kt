package com.projectrestaurant.adapter

import android.app.Application
import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.projectrestaurant.database.Order
import com.projectrestaurant.database.OrderProduct
import com.projectrestaurant.databinding.ItemRecyclerViewOrderBinding
import com.projectrestaurant.ui.userorders.FragmentUserOrdersDirections
import com.projectrestaurant.viewmodel.OrdersViewModel

class OrderAdapter(private val application: Application, private val viewModel: OrdersViewModel, private val navController: NavController)
    : ListAdapter<Order, OrderAdapter.OrderViewHolder>(ItemDiffCallback()) {

    private var fullOrders = listOf<Order>()
    private var fullOrderProducts = listOf<OrderProduct>()
    private var currentOrders = listOf<Order>()
    private var stringBuilder = StringBuilder()
    private val PRODUCTS_PER_PAGE = 15
    var numberOfPages: Int = 1; private set
    private val _currentPage = MutableLiveData(1)
    val currentPage: LiveData<Int> get() = _currentPage

    fun setData(orders: List<Order>, orderProducts: List<OrderProduct>) {
        fullOrders = orders
        fullOrderProducts = orderProducts
        numberOfPages =
            if(fullOrders.size % PRODUCTS_PER_PAGE != 0) (fullOrders.size / PRODUCTS_PER_PAGE) + 1
            else fullOrders.size / PRODUCTS_PER_PAGE
        currentOrders = if(numberOfPages > 1) fullOrders.subList(0, PRODUCTS_PER_PAGE) else fullOrders
        submitList(currentOrders)
    }

    //Single element of the list
    inner class OrderViewHolder(val binding: ItemRecyclerViewOrderBinding)
        : RecyclerView.ViewHolder(binding.root) {
        lateinit var orderId: String
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemRecyclerViewOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val calendar: Calendar = Calendar.getInstance()
        holder.orderId = currentOrders[position].orderId
        with(holder.binding) {
            val tmp = fullOrderProducts.filter { it.orderId == currentOrders[position].orderId }.sortedBy { it.foodId }
            if(tmp.isNotEmpty()) {
                var total = 0 ; var i = 0 ; var j = 1; var count = tmp[i].quantity
                while(i < tmp.size) {
                    while(j < tmp.size && tmp[j].foodId == tmp[i].foodId) { count += tmp[j].quantity; j++ } // tmp[i]'s foodId is the current id to count
                    stringBuilder.append("$count - ${viewModel.foodNames[tmp[i].foodId]}\n")
                    total += count; i = j; j++; count = if(i < tmp.size) tmp[i].quantity else 0
                }
                textViewTotalQuantity.text = application.resources.getString(com.projectrestaurant.R.string.order_total_product_quantity, total.toString())
                textViewProductsList.text = stringBuilder.deleteCharAt(stringBuilder.lastIndex)
                stringBuilder = stringBuilder.clear()
            }
            textViewTotalPrice.text = application.resources.getString(com.projectrestaurant.R.string.shopping_cart_product_price, String.format("%.2f", currentOrders[position].totalPrice))
            calendar.timeInMillis = currentOrders[position].orderDate.time
            textViewOrderDate.text = application.resources.getString(com.projectrestaurant.R.string.order_date,
                calendar.get(Calendar.DAY_OF_MONTH).toString(), viewModel.monthNames[calendar.get(Calendar.MONTH)], calendar.get(Calendar.YEAR).toString())
            calendar.timeInMillis = currentOrders[position].deliveryDate.time
            textViewOrderDeliveryDate.text = application.resources.getString(com.projectrestaurant.R.string.order_delivery_date,
                calendar.get(Calendar.DAY_OF_MONTH).toString(), viewModel.monthNames[calendar.get(Calendar.MONTH)], calendar.get(Calendar.YEAR).toString())
            cardViewOrder.setOnClickListener {
                val action = FragmentUserOrdersDirections.actionFragmentUserOrdersToFragmentUserOrderedProducts(currentOrders[position])
                navController.navigate(action)
            }
        }
    }

    companion object {
        class ItemDiffCallback : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean { return oldItem === newItem }

            override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean { return oldItem.orderId == newItem.orderId }
        }
    }

    fun goToNextPage(nextPage: Int = _currentPage.value!! + 1) {
        if(_currentPage.value == numberOfPages) return
        else {
            currentOrders =
                if(nextPage * PRODUCTS_PER_PAGE > fullOrders.lastIndex)
                    fullOrders.subList(_currentPage.value!! * PRODUCTS_PER_PAGE, fullOrderProducts.lastIndex + 1)
                else fullOrders.subList(_currentPage.value!! * PRODUCTS_PER_PAGE, nextPage * PRODUCTS_PER_PAGE)
            _currentPage.value = _currentPage.value!! + 1
            submitList(currentOrders)
        }
    }

    fun goToPreviousPage(previousPage: Int = _currentPage.value!! - 1) {
        if(_currentPage.value!! == 1) return
        else {
            currentOrders = fullOrders.subList((previousPage - 1) * PRODUCTS_PER_PAGE, previousPage * PRODUCTS_PER_PAGE)
            _currentPage.value = _currentPage.value!! - 1
            submitList(currentOrders)
        }
    }

    fun hasMultiplePages(): Boolean { return numberOfPages > 1 }
}