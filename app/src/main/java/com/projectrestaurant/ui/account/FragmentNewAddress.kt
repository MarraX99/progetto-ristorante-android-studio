package com.projectrestaurant.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.projectrestaurant.databinding.FragmentNewAddressBinding
import com.projectrestaurant.viewmodel.AccountViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentNewAddress: Fragment() {
    private lateinit var binding: FragmentNewAddressBinding
    private lateinit var navController: NavController
    private val viewModel: AccountViewModel by activityViewModels<AccountViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentNewAddressBinding.inflate(inflater, container, false)
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            addressContainer.setOnClickListener(::onClickListener)
            postcodeContainer.setOnClickListener(::onClickListener)
            cityContainer.setOnClickListener(::onClickListener)
            provinceContainer.setOnClickListener(::onClickListener)
            buttonAddAddress.setOnClickListener {
                it.isClickable = false
                if(editTextAddress.text.isNullOrBlank()) {
                    addressContainer.isErrorEnabled = true
                    editTextAddress.error = getString(com.projectrestaurant.R.string.error_field_empty)
                    it.isClickable = true
                    return@setOnClickListener
                }
                if(editTextPostcode.text.isNullOrBlank()) {
                    postcodeContainer.isErrorEnabled = true
                    editTextPostcode.error = getString(com.projectrestaurant.R.string.error_field_empty)
                    it.isClickable = true
                    return@setOnClickListener
                }
                if(editTextCity.text.isNullOrBlank()) {
                    cityContainer.isErrorEnabled = true
                    editTextCity.error = getString(com.projectrestaurant.R.string.error_field_empty)
                    it.isClickable = true
                    return@setOnClickListener
                }
                if(editTextProvince.text.isNullOrBlank()) {
                    provinceContainer.isErrorEnabled = true
                    editTextProvince.error = getString(com.projectrestaurant.R.string.error_field_empty)
                    it.isClickable = true
                    return@setOnClickListener
                }
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    val address = withContext(Dispatchers.IO) {
                        viewModel.addDeliveryAddress(editTextAddress.text.toString(),
                            editTextPostcode.text.toString(), editTextCity.text.toString(),
                            editTextProvince.text.toString(), checkBoxDefaultAddress.isChecked)
                    }
                    if(address != null) {
                        val bundle = Bundle()
                        Toast.makeText(requireContext(), com.projectrestaurant.R.string.new_address_success, Toast.LENGTH_LONG).show()
                        bundle.putParcelable("newAddress", address)
                        setFragmentResult("addedNewAddress", bundle)
                        navController.navigateUp()
                    } else {
                        AlertDialog.Builder(requireContext()).setTitle(com.projectrestaurant.R.string.new_address_error_title)
                            .setMessage(com.projectrestaurant.R.string.new_address_error_message)
                            .setPositiveButton(com.projectrestaurant.R.string.ok) { _, _ -> }.show()
                    }
                }
                it.isClickable = true
            }
        }
    }

    private fun onClickListener(view: View) {
        if(view is TextInputLayout) {
            view.error = null
            view.isErrorEnabled = false
            view.editText?.error = null
        }
        return
    }

}