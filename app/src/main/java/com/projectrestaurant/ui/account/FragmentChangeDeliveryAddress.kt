package com.projectrestaurant.ui.account

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.radiobutton.MaterialRadioButton
import com.projectrestaurant.database.Address
import com.projectrestaurant.databinding.FragmentChangeDeliveryAddressBinding
import com.projectrestaurant.viewmodel.AccountViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentChangeDeliveryAddress: Fragment() {
    private lateinit var binding: FragmentChangeDeliveryAddressBinding
    private lateinit var navController: NavController
    private val viewModel: AccountViewModel by activityViewModels<AccountViewModel>()
    private lateinit var list: MutableList<Address>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentChangeDeliveryAddressBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        navController = findNavController()
        setFragmentResultListener("addedNewAddress") { _, bundle ->
            val new = bundle.getParcelable<Address>("newAddress")
            if(new != null) addToRadioGroup(new, binding.radioGroupAddresses, requireContext(),true)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            list = viewModel.getDeliveryAddresses().toMutableList()
            withContext(Dispatchers.Main) { for(element in list) addToRadioGroup(element, binding.radioGroupAddresses, requireContext()) }
        }
        binding.buttonChangeAddress.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                if(binding.radioGroupAddresses.checkedRadioButtonId != View.NO_ID) {
                    viewModel.setDefaultDeliveryAddress(list[binding.radioGroupAddresses.checkedRadioButtonId].addressId)
                    withContext(Dispatchers.Main){ navController.navigateUp() }
                }
            }
        }
        binding.buttonAddAddress.setOnClickListener {
            navController.navigate(com.projectrestaurant.R.id.action_fragment_change_delivery_address_to_fragment_new_address)
        }
    }

    private fun addToRadioGroup(element: Address, radioGroup: RadioGroup, context: Context, addToList: Boolean = false) {
        if(addToList) list.add(element)
        val radioButton = MaterialRadioButton(context)
        val stringBuilder = StringBuilder("${element.address} - ${element.cap} - ${element.city} - ${element.province}")
        val params = RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.MATCH_PARENT)
        with(params) {
            weight = 1.0f
            gravity = Gravity.CENTER
        }
        with(radioButton) {
            id = list.indexOf(element)
            text = stringBuilder.toString()
            isChecked = element.defaultAddress
            textSize = 16.0f
            layoutParams = params
            TextViewCompat.setAutoSizeTextTypeWithDefaults(this, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
        }
        radioGroup.addView(radioButton)
    }
}