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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.projectrestaurant.adapter.OrderAdapter
import com.projectrestaurant.database.OrderProduct
import com.projectrestaurant.databinding.FragmentUserOrdersBinding
import com.projectrestaurant.viewmodel.OrdersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentUserOrders: Fragment() {
    private lateinit var binding: FragmentUserOrdersBinding
    private val viewModel: OrdersViewModel by activityViewModels()
    private val constraintSet = ConstraintSet()
    private lateinit var adapter: OrderAdapter
    private lateinit var currentPageObserver: Observer<Int>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentUserOrdersBinding.inflate(inflater, container, false)
        adapter = OrderAdapter(requireActivity().application, viewModel, findNavController())
        binding.recyclerViewOrders.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            constraintSet.clone(constraintLayout)
            progressBar.isIndeterminate = true
            constraintLayout.overlay.add(progressBar)
            progressBar.visibility = View.VISIBLE
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val orderList = viewModel.getOrders()
                val productList = mutableListOf<OrderProduct>()
                for(order in orderList) productList.addAll(viewModel.getOrderProducts(order))
                withContext(Dispatchers.Main) {
                    progressBar.isIndeterminate = false
                    constraintLayout.overlay.remove(progressBar)
                    progressBar.visibility = View.GONE
                    recyclerViewOrders.adapter = adapter
                    adapter.setData(orderList, productList)
                    if(adapter.hasMultiplePages()) {
                        currentPageObserver = Observer { newValue ->
                            textViewPagesList.text = getString(com.projectrestaurant.R.string.pages_list, newValue.toString(), adapter.numberOfPages.toString())
                        }
                        adapter.currentPage.observe(viewLifecycleOwner, currentPageObserver)
                        buttonNextPage.setOnClickListener { adapter.goToNextPage() }
                        buttonPreviousPage.setOnClickListener { adapter.goToPreviousPage() }
                        constraintSet.constrainPercentHeight(cardViewOrders.id, 0.9F)
                        constraintSet.constrainPercentHeight(layoutButtons.id, 0.1F)
                        constraintSet.applyTo(constraintLayout)
                    }
                }
            }
        }
    }
}