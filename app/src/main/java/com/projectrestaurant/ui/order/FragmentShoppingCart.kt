package com.projectrestaurant.ui.order

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.projectrestaurant.CartProduct
import com.projectrestaurant.adapter.CartProductAdapter
import com.projectrestaurant.databinding.FragmentShoppingCartBinding
import com.projectrestaurant.viewmodel.AccountViewModel
import com.projectrestaurant.viewmodel.FoodOrderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentShoppingCart: Fragment() {
    private lateinit var binding: FragmentShoppingCartBinding
    private lateinit var navController: NavController
    private val foodOrderViewModel by activityViewModels<FoodOrderViewModel>()
    private val accountViewModel by activityViewModels<AccountViewModel>()
    private lateinit var adapter: CartProductAdapter
    private var hourList: List<Long> = emptyList()
    private var finalCalendar = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentShoppingCartBinding.inflate(inflater, container, false)
        binding.recyclerViewShoppingCart.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.accountViewModel = accountViewModel
        binding.lifecycleOwner = this
        setFragmentResultListener("modifiedCartProduct") { _, bundle ->
            adapter.updateCartProduct(CartProduct(bundle.getString("cartProductId")!!,
                bundle.getParcelable("food")!!, bundle.getParcelableArrayList("extraIngredients")!!,
                bundle.getParcelableArrayList("removedIngredients")!!,
                bundle.getInt("quantity"), bundle.getDouble("price")))
        }
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var dayList: List<Long> = emptyList()
        with(binding) {
            if(!(foodOrderViewModel.isLoggedIn)) navController.navigateUp()
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                var list: MutableSet<CartProduct>
                val foodImages = HashMap<Int,String?>()
                withContext(Dispatchers.IO) {
                    accountViewModel!!.getDefaultDeliveryAddress(foodOrderViewModel.userId)
                    list = foodOrderViewModel.getCartProducts(foodOrderViewModel.userId)
                    for(product in list) foodImages[product.food.foodId] = foodOrderViewModel.getFoodImage(product.food.foodId)
                }
                if(list.isEmpty()) navController.navigateUp()
                if(accountViewModel!!.deliveryAddress.value == null) {
                    radioButtonDeliveryAddress.visibility = View.GONE
                    textViewCurrentAddress.visibility = View.GONE
                    radioButtonVenue.isChecked = true
                } else radioButtonDeliveryAddress.isChecked = true
                adapter = CartProductAdapter(navController, requireContext(), foodOrderViewModel)
                recyclerViewShoppingCart.adapter = adapter
                adapter.setData(list, foodImages)
                dayList = foodOrderViewModel.getDeliveryDays()
                addDays(dayList, chipGroupDays)
            }
            textViewNote.setOnClickListener {
                navController.navigate(com.projectrestaurant.R.id.action_fragment_shopping_cart_to_fragment_note)
            }
            buttonChangeAddress.setOnClickListener {
                navController.navigate(com.projectrestaurant.R.id.action_fragment_shopping_cart_to_fragment_change_delivery_address)
            }
            chipGroupDays.setOnCheckedStateChangeListener { chipGroup, checkedIds ->
                chipGroup.isEnabled = false
                if(chipGroupHours.childCount != 0) { chipGroupHours.removeAllViews() }
                if(checkedIds.isEmpty()) { chipGroup.isEnabled = true }
                else {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = dayList[checkedIds[0]]
                    hourList = foodOrderViewModel.getHours(calendar)
                    addHours(hourList, chipGroupHours)
                    chipGroup.isEnabled = true
                }
            }
            chipGroupHours.setOnCheckedStateChangeListener { chipGroup, checkedIds ->
                chipGroup.isEnabled = false
                if(checkedIds.isNotEmpty()) { finalCalendar.timeInMillis = hourList[checkedIds[0]] }
                chipGroup.isEnabled = true
            }
            buttonOrder.setOnClickListener {
                it.isClickable = false
                progressBar.isIndeterminate = true
                constraintLayout.overlay.add(progressBar)
                progressBar.visibility = View.VISIBLE
                if(chipGroupDays.checkedChipId == View.NO_ID || chipGroupHours.checkedChipId == View.NO_ID) {
                    AlertDialog.Builder(requireContext()).setTitle(com.projectrestaurant.R.string.order_delivery_datetime_error_title)
                        .setMessage(com.projectrestaurant.R.string.order_delivery_datetime_error_message)
                        .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> }.show()
                    it.isClickable = true
                    return@setOnClickListener
                }
                when(foodOrderViewModel.isOnline(requireActivity().application)) {
                    true -> {
                        when(foodOrderViewModel.isLoggedIn) {
                            true -> {
                                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                                    val result =
                                        withContext(Dispatchers.IO) { foodOrderViewModel.createOrder(adapter.productsList, finalCalendar, radioButtonDeliveryAddress.isChecked) }
                                    if (result) {
                                        AlertDialog.Builder(requireContext())
                                            .setMessage(com.projectrestaurant.R.string.order_success_message)
                                            .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> navController.navigateUp() }.show()
                                    } else AlertDialog.Builder(requireContext()).setTitle(com.projectrestaurant.R.string.shopping_cart_error_title)
                                            .setMessage(com.projectrestaurant.R.string.order_error_message)
                                            .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> navController.navigateUp() }.show()
                                }
                            }
                            false -> AlertDialog.Builder(requireContext()).setTitle(com.projectrestaurant.R.string.order_login_error_title)
                                    .setMessage(com.projectrestaurant.R.string.order_login_error_message)
                                    .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> navController.navigateUp() }.show()
                        }
                    }
                    false -> AlertDialog.Builder(requireContext()).setTitle(com.projectrestaurant.R.string.order_connection_error_title)
                            .setMessage(com.projectrestaurant.R.string.order_connection_error_message)
                            .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> navController.navigateUp() }.show()
                }
            }
        }
    }

    private fun addDays(dayList: List<Long>,chipGroup: ChipGroup) {
        var calendar: Calendar
        for(day in dayList) {
            calendar = Calendar.getInstance().apply { timeInMillis = day }
            chipGroup.addView(Chip(requireContext()).apply {
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                id = dayList.indexOf(day)
                text = resources.getString(com.projectrestaurant.R.string.order_delivery_date_option,
                    calendar[Calendar.DAY_OF_MONTH].toString(), foodOrderViewModel.monthNames[calendar[Calendar.MONTH]]) // old => String.format("%02d", calendar[Calendar.DAY_OF_MONTH])
                isCheckable = true
            })
        }
        chipGroup.isEnabled = true
    }

    private fun addHours(hourList: List<Long>, chipGroupHours: ChipGroup) {
        var calendar: Calendar
        for(hour in hourList) {
            calendar = Calendar.getInstance().apply { timeInMillis = hour }
            chipGroupHours.addView(Chip(requireContext()).apply {
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                id = hourList.indexOf(hour)
                text = resources.getString(com.projectrestaurant.R.string.order_delivery_hour_option,
                    String.format("%02d", calendar[Calendar.HOUR_OF_DAY]), String.format("%02d", calendar[Calendar.MINUTE]))
                isCheckable = true
            })
        }
    }
}