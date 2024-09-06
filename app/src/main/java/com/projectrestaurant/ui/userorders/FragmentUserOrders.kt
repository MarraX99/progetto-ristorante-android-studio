package com.projectrestaurant.ui.userorders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.projectrestaurant.adapter.OrderProductAdapter
import com.projectrestaurant.database.Order
import com.projectrestaurant.database.OrderProduct
import com.projectrestaurant.database.OrderProductEdit
import com.projectrestaurant.databinding.FragmentUserOrdersBinding
import com.projectrestaurant.viewmodel.OrdersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentUserOrders: Fragment() {
    private lateinit var binding: FragmentUserOrdersBinding
    private val viewModel: OrdersViewModel by activityViewModels()
    private lateinit var adapter: OrderProductAdapter
    private val constraintSet = ConstraintSet()
    private lateinit var currentPageObserver: Observer<Int>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentUserOrdersBinding.inflate(inflater, container, false)
        binding.recyclerViewUserOrders.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        constraintSet.clone(binding.constraintLayout)
        adapter = OrderProductAdapter(requireActivity().application)
        val ordersList: MutableList<Order> = mutableListOf()
        val orderProductsList: MutableList<OrderProduct> = mutableListOf()
        val orderProductEditsList: MutableList<OrderProductEdit> = mutableListOf()
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            binding.progressBar.isIndeterminate = true
            binding.constraintLayout.overlay.add(binding.progressBar)
            binding.progressBar.visibility = View.VISIBLE
            withContext(Dispatchers.IO) {
                var currentProductIndex = 0
                ordersList.addAll(viewModel.getOrders())
                for(order in ordersList) {
                    val productsList = viewModel.getOrderProducts(order)
                    if(productsList.isNotEmpty()) {
                        orderProductsList.addAll(productsList)
                        for(product in orderProductsList.subList(currentProductIndex, orderProductsList.lastIndex))
                            orderProductEditsList.addAll(viewModel.getOrderProductEdits(product))
                        currentProductIndex = orderProductsList.lastIndex + 1
                    }
                }
            }
            binding.progressBar.isIndeterminate = false
            binding.constraintLayout.overlay.remove(binding.progressBar)
            binding.progressBar.visibility = View.GONE
            binding.recyclerViewUserOrders.adapter = adapter
            adapter.setData(ordersList, orderProductsList, orderProductEditsList)

            if(adapter.hasMultiplePages()) {
                currentPageObserver = Observer { newValue ->
                    binding.textViewPagesList.text = getString(com.projectrestaurant.R.string.pages_list, newValue.toString(), adapter.numberOfPages.toString())
                }
                constraintSet.constrainPercentHeight(binding.cardViewOrders.id, 0.9F)
                constraintSet.constrainPercentHeight(binding.buttonsOrders.id, 0.1F)
                binding.textViewPagesList.text = getString(com.projectrestaurant.R.string.pages_list, adapter.currentPage.value.toString(), adapter.numberOfPages.toString())
                binding.buttonNextPage.setOnClickListener { adapter.goToNextPage(adapter.currentPage.value!! + 1) }
                binding.buttonPreviousPage.setOnClickListener { adapter.goToPreviousPage(adapter.currentPage.value!! - 1) }
                adapter.currentPage.observe(viewLifecycleOwner, currentPageObserver)
                constraintSet.applyTo(binding.constraintLayout)
            }
        }
    }
}