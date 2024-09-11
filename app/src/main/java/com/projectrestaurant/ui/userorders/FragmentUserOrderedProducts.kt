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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.projectrestaurant.adapter.OrderProductAdapter
import com.projectrestaurant.database.OrderProductEdit
import com.projectrestaurant.databinding.FragmentUserOrderedProductsBinding
import com.projectrestaurant.viewmodel.OrdersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentUserOrderedProducts: Fragment() {
    private lateinit var binding: FragmentUserOrderedProductsBinding
    private val viewModel: OrdersViewModel by activityViewModels()
    private lateinit var adapter: OrderProductAdapter
    private val constraintSet = ConstraintSet()
    private lateinit var currentPageObserver: Observer<Int>
    private val args: FragmentUserOrderedProductsArgs by navArgs<FragmentUserOrderedProductsArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentUserOrderedProductsBinding.inflate(inflater, container, false)
        binding.recyclerViewOrderedProducts.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.lifecycleOwner = this
        adapter = OrderProductAdapter(requireActivity().application, viewModel)
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
                val productList = viewModel.getOrderProducts(args.order)
                val productEditsList = mutableListOf<OrderProductEdit>()
                val productImages = hashMapOf<Int, String?>()
                for(product in productList) {
                    productEditsList.addAll(viewModel.getOrderProductEdits(product))
                    productImages[product.foodId] = viewModel.getFoodImage(product.foodId)
                }
                withContext(Dispatchers.Main) {
                    progressBar.isIndeterminate = false
                    constraintLayout.overlay.remove(progressBar)
                    progressBar.visibility = View.GONE
                    recyclerViewOrderedProducts.adapter = adapter
                    adapter.setData(productList, productEditsList, productImages)
                    if(adapter.hasMultiplePages()) {
                        currentPageObserver = Observer { newValue ->
                            textViewPagesList.text = getString(com.projectrestaurant.R.string.pages_list, newValue.toString(), adapter.numberOfPages.toString())
                        }
                        adapter.currentPage.observe(viewLifecycleOwner, currentPageObserver)
                        buttonNextPage.setOnClickListener { adapter.goToNextPage() }
                        buttonPreviousPage.setOnClickListener { adapter.goToPreviousPage() }
                        constraintSet.constrainPercentHeight(cardViewOrderedProducts.id, 0.9F)
                        constraintSet.constrainPercentHeight(layoutButtons.id, 0.1F)
                        constraintSet.applyTo(constraintLayout)
                    }
                }
            }
        }
    }
}