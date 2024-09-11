package com.projectrestaurant.ui.order

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.projectrestaurant.adapter.FoodListAdapter
import com.projectrestaurant.databinding.FragmentFoodListBinding
import com.projectrestaurant.viewmodel.FoodOrderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentFoodList: Fragment(), MenuProvider {
    private lateinit var binding: FragmentFoodListBinding
    private lateinit var navController: NavController
    private val viewModel: FoodOrderViewModel by activityViewModels()
    private val args: FragmentFoodListArgs by navArgs<FragmentFoodListArgs>()
    private lateinit var adapter: FoodListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentFoodListBinding.inflate(inflater, container, false)
        binding.recyclerViewFoodList.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FoodListAdapter(navController, requireActivity().application)
        (requireActivity() as MenuHost).addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.STARTED)
        with(binding) {
            progressBar.isIndeterminate = true
            constraintLayout.overlay.add(progressBar)
            progressBar.visibility = View.VISIBLE
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val foodList = viewModel.getFoodList(args.foodType)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    progressBar.isIndeterminate = false
                    constraintLayout.overlay.remove(progressBar)
                    if(viewModel.isLoggedIn() && !(viewModel.isShoppingCartEmpty())) buttonShoppingCart.visibility = View.VISIBLE
                    adapter.setFoodData(foodList)
                    recyclerViewFoodList.adapter = adapter
                    editTextSearch.addTextChangedListener { adapter.filter.filter(it) }
                    buttonShoppingCart.setOnClickListener {
                        viewModel.resetPrice()
                        navController.navigate(com.projectrestaurant.R.id.action_fragment_food_list_to_fragment_shopping_cart)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetPrice()
        viewModel.setFoodQuantity(1)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(com.projectrestaurant.R.menu.food_order_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            com.projectrestaurant.R.id.nav_search -> {
                if(binding.searchContainer.visibility == View.VISIBLE) binding.searchContainer.visibility = View.GONE
                else binding.searchContainer.visibility = View.VISIBLE
                closeKeyboard(requireActivity())
                return true
            }
            else -> return false
        }
    }

    private fun closeKeyboard(activity: Activity) {
        val manager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view: View = if(activity.currentFocus != null) activity.currentFocus!! else View(activity)
        manager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}