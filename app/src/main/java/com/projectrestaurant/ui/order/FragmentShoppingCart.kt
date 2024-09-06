package com.projectrestaurant.ui.order

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
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
    private val foodOrderViewModel: FoodOrderViewModel by activityViewModels()
    private val accountViewModel: AccountViewModel by activityViewModels()
    private lateinit var adapter: CartProductAdapter
    private lateinit var dayList: List<Long>
    private lateinit var hourList: List<Long>
    private val monthNames: Array<String> by lazy {
        resources.getStringArray(com.projectrestaurant.R.array.month_names)
    }
    private lateinit var calendar: Calendar

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
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            if(foodOrderViewModel.isLoggedIn()) {
                val list = withContext(Dispatchers.IO) {
                    accountViewModel.getDefaultDeliveryAddress(foodOrderViewModel.getUserId())
                    foodOrderViewModel.getCartProducts(foodOrderViewModel.getUserId())
                }
                if(list.isEmpty()) navController.navigateUp()
                adapter = CartProductAdapter(navController, requireContext(), foodOrderViewModel)
                adapter.setData(list)
                binding.recyclerViewShoppingCart.adapter = adapter
                binding.radioGroup.findViewById<RadioButton>(com.projectrestaurant.R.id.radio_button_delivery_address).isChecked = true
                dayList = foodOrderViewModel.getDeliveryDays()
                var chip: Chip
                for(day in dayList) {
                    calendar = Calendar.getInstance()
                    calendar.timeInMillis = day
                    chip = Chip(requireContext())
                    with(chip) {
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                        id = dayList.indexOf(day)
                        text = resources.getString(com.projectrestaurant.R.string.order_delivery_date_option,
                            String.format("%02d", calendar[Calendar.DAY_OF_MONTH]), monthNames[calendar[Calendar.MONTH]])
                        isCheckable = true
                    }
                    binding.chipGroupDays.addView(chip)
                }
            }
        }
        binding.textViewNote.setOnClickListener {
            navController.navigate(com.projectrestaurant.R.id.action_fragment_shopping_cart_to_fragment_note)
        }

        binding.buttonChangeAddress.setOnClickListener {
            navController.navigate(com.projectrestaurant.R.id.action_fragment_shopping_cart_to_fragment_change_delivery_address)
        }

        binding.chipGroupDays.setOnCheckedStateChangeListener { chipGroup, checkedIds ->
            chipGroup.isEnabled = false
            if(binding.chipGroupHours.childCount != 0) {
                binding.chipGroupHours.removeAllViews()
            }
            if(checkedIds.isEmpty()) {
                chipGroup.isEnabled = true
                return@setOnCheckedStateChangeListener
            }
            else {
                calendar = Calendar.getInstance()
                calendar.timeInMillis = dayList[checkedIds[0]]
                hourList = foodOrderViewModel.getHours(calendar)
                var chip: Chip
                for(hour in hourList) {
                    calendar = Calendar.getInstance()
                    calendar.timeInMillis = hour
                    chip = Chip(requireContext())
                    with(chip) {
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                        id = hourList.indexOf(hour)
                        text = resources.getString(com.projectrestaurant.R.string.order_delivery_hour_option,
                            String.format("%02d", calendar[Calendar.HOUR_OF_DAY]), String.format("%02d", calendar[Calendar.MINUTE]))
                        isCheckable = true
                    }
                    binding.chipGroupHours.addView(chip)
                }
            }
            chipGroup.isEnabled = true
        }

        binding.chipGroupHours.setOnCheckedStateChangeListener { _, checkedIds ->
            if(checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            else {
                calendar = Calendar.getInstance()
                calendar.timeInMillis = hourList[checkedIds[0]]
            }
        }

        binding.buttonOrder.setOnClickListener {
            it.isClickable = false
            binding.progressBar.isIndeterminate = true
            binding.constraintLayout.overlay.add(binding.progressBar)
            binding.progressBar.visibility = View.VISIBLE
            if(binding.chipGroupDays.checkedChipId == View.NO_ID && binding.chipGroupHours.checkedChipId == View.NO_ID) {
                AlertDialog.Builder(requireContext())
                    .setTitle(com.projectrestaurant.R.string.order_delivery_datetime_error_title)
                    .setMessage(com.projectrestaurant.R.string.order_delivery_datetime_error_message)
                    .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> }.show()
                it.isClickable = true
                return@setOnClickListener
            }
            if(foodOrderViewModel.isOnline(requireContext().applicationContext)) {
                if(foodOrderViewModel.isLoggedIn()) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        val result =
                            withContext(Dispatchers.IO) { foodOrderViewModel.createOrder(adapter.productsList, calendar) }
                        if (result) {
                            AlertDialog.Builder(requireContext())
                                .setMessage(com.projectrestaurant.R.string.order_success_message)
                                .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> navController.navigateUp() }.show()
                        } else {
                            AlertDialog.Builder(requireContext())
                                .setTitle(com.projectrestaurant.R.string.shopping_cart_error_title)
                                .setMessage(com.projectrestaurant.R.string.order_error_message)
                                .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> navController.navigateUp() }.show()
                        }
                    }
                } else {
                    AlertDialog.Builder(requireContext()).setTitle(com.projectrestaurant.R.string.order_login_error_title)
                        .setMessage(com.projectrestaurant.R.string.order_login_error_message)
                        .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> navController.navigateUp() }.show()
                }
            } else {
                AlertDialog.Builder(requireContext()).setTitle(com.projectrestaurant.R.string.order_connection_error_title)
                    .setMessage(com.projectrestaurant.R.string.order_connection_error_message)
                    .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> navController.navigateUp() }.show()
            }
        }
    }
}