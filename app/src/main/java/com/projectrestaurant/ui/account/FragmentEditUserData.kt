package com.projectrestaurant.ui.account

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.projectrestaurant.databinding.FragmentEditUserDataBinding
import com.projectrestaurant.viewmodel.AccountViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentEditUserData: Fragment() {
    private lateinit var binding: FragmentEditUserDataBinding
    private lateinit var navController: NavController
    private val viewModel: AccountViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentEditUserDataBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nameContainer.setOnClickListener(::onClickListener)
        binding.surnameContainer.setOnClickListener(::onClickListener)
        binding.emailContainer.setOnClickListener(::onClickListener)
        binding.passwordContainer.setOnClickListener(::onClickListener)
        binding.buttonConfirm.setOnClickListener {
            it.isClickable = false
            if(binding.editTextName.text.isNullOrBlank()) {
                binding.nameContainer.isErrorEnabled = true
                binding.nameContainer.error = getString(com.projectrestaurant.R.string.user_error_blank_name)
                it.isClickable = true
                return@setOnClickListener
            }
            if(binding.editTextSurname.text.isNullOrBlank()) {
                binding.nameContainer.isErrorEnabled = true
                binding.nameContainer.error = getString(com.projectrestaurant.R.string.user_error_blank_surname)
                it.isClickable = true
                return@setOnClickListener
            }
            val checks = viewModel.validateEmail(binding.editTextEmail.text.toString())
            if (checks.containsValue(false)) {
                binding.emailContainer.isErrorEnabled = true
                if (checks["isBlank"]!!) {
                    binding.emailContainer.error = getString(com.projectrestaurant.R.string.user_error_blank_email)
                    it.isClickable = true
                    return@setOnClickListener }
                if (!checks["isValid"]!!) {
                    binding.emailContainer.error = getString(com.projectrestaurant.R.string.user_error_email_invalid)
                    it.isClickable = true
                    return@setOnClickListener }
            }
            if (binding.editTextPassword.text.isNullOrBlank()) {
                binding.passwordContainer.isErrorEnabled = true
                binding.passwordContainer.error = getString(com.projectrestaurant.R.string.user_error_blank_password)
                it.isClickable = true
                return@setOnClickListener }
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                val result1 = withContext(Dispatchers.IO) {
                    viewModel.changeUserNameAndSurname(binding.editTextName.text.toString(),
                        binding.editTextSurname.text.toString())
                }
                val result2 = withContext(Dispatchers.IO) {
                    viewModel.changeUserEmail(binding.editTextEmail.text.toString(), binding.editTextPassword.text.toString())
                }
                if(result1 && result2) {
                    AlertDialog.Builder(requireContext()).setMessage(com.projectrestaurant.R.string.user_edit_data_completed)
                        .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> navController.navigateUp() }.show()
                }
                if(!result1) {
                    AlertDialog.Builder(requireContext()).setMessage(com.projectrestaurant.R.string.user_edit_name_surname_error_message)
                        .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> navController.navigateUp() }.show()
                }
                if(!result2) {
                    AlertDialog.Builder(requireContext()).setTitle(com.projectrestaurant.R.string.user_edit_email_error_title)
                        .setMessage(com.projectrestaurant.R.string.user_edit_email_error_message)
                        .setNeutralButton(com.projectrestaurant.R.string.ok) { _, _ -> navController.navigateUp() }.show()
                }

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
