package com.projectrestaurant.ui.loginregister

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.projectrestaurant.MainActivity
import com.projectrestaurant.databinding.FragmentRegisterBinding
import com.projectrestaurant.R.string
import com.projectrestaurant.viewmodel.LoginRegisterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentRegister : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var navController: NavController
    private val viewModel: LoginRegisterViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = view.findNavController()
        binding.cardViewLoginTitle.setOnClickListener { navController.navigateUp() }
        binding.nameContainer.setOnClickListener(::onClickListener)
        binding.surnameContainer.setOnClickListener(::onClickListener)
        binding.emailContainer.setOnClickListener(::onClickListener)
        binding.passwordContainer.setOnClickListener(::onClickListener)
        binding.passwordConfirmContainer.setOnClickListener(::onClickListener)

        binding.buttonRegister.setOnClickListener {
            it.isClickable = false
            //name value validation
            if (binding.editTextName.text.toString().isBlank()) {
                binding.nameContainer.isErrorEnabled = true
                binding.nameContainer.error = getString(string.user_error_blank_name)
                it.isClickable = true
                return@setOnClickListener }
            //surname value validation
            if (binding.editTextSurname.text.toString().isBlank()) {
                binding.surnameContainer.isErrorEnabled = true
                binding.surnameContainer.error = getString(string.user_error_blank_surname)
                it.isClickable = true
                return@setOnClickListener }
            //email value validation
            var checks = viewModel.validateEmail(binding.editTextEmail.text.toString())
            if (checks.containsValue(false)) {
                binding.emailContainer.isErrorEnabled = true
                if (checks["isBlank"]!!) {
                    binding.emailContainer.error = getString(string.user_error_blank_email)
                    it.isClickable = true
                    return@setOnClickListener }
                if (!checks["isValid"]!!) {
                    binding.emailContainer.error = getString(string.user_error_email_invalid)
                    it.isClickable = true
                    return@setOnClickListener }
            }
            //password validation
            checks = viewModel.validatePassword(binding.editTextPassword.text.toString(), binding.editTextPasswordConfirm.text.toString())
            if (checks.containsValue(false)) {
                if (checks["isBlank"]!!) {
                    binding.passwordContainer.isErrorEnabled = true
                    binding.passwordContainer.error = getString(string.user_error_blank_password)
                    it.isClickable = true
                    return@setOnClickListener }
                if (!checks["hasMinimumLength"]!!) {
                    binding.passwordContainer.isErrorEnabled = true
                    binding.passwordContainer.error = getString(string.user_error_password_short)
                    it.isClickable = true
                    return@setOnClickListener }
                if (!checks["hasMinimumUpperCases"]!!) {
                    binding.passwordContainer.isErrorEnabled = true
                    binding.passwordContainer.error = getString(string.user_error_password_upper_case)
                    it.isClickable = true
                    return@setOnClickListener }
                if (!checks["hasMinimumLowerCases"]!!) {
                    binding.passwordContainer.isErrorEnabled = true
                    binding.passwordContainer.error = getString(string.user_error_password_lower_case)
                    it.isClickable = true
                    return@setOnClickListener }
                if (!checks["hasMinimumDigits"]!!) {
                    binding.passwordContainer.isErrorEnabled = true
                    binding.passwordContainer.error = getString(string.user_error_password_digit)
                    it.isClickable = true
                    return@setOnClickListener }
                if (!checks["hasMinimumSpecialCharacters"]!!) {
                    binding.passwordContainer.isErrorEnabled = true
                    binding.passwordContainer.error = getString(string.user_error_password_special_char)
                    it.isClickable = true
                    return@setOnClickListener }
                if (!checks["equalsConfirm"]!!) {
                    binding.passwordConfirmContainer.isErrorEnabled = true
                    binding.passwordConfirmContainer.error = getString(string.user_error_password_mismatch)
                    it.isClickable = true
                    return@setOnClickListener }
            }
            //checks if the check box is checked
            if (!binding.checkBoxPrivacyPolicy.isChecked) {
                AlertDialog.Builder(requireActivity())
                    .setTitle(string.user_register_error_title)
                    .setMessage(string.user_privacy_policy_check_missing)
                    .setPositiveButton(string.ok) { _, _ -> }.show()
                binding.buttonRegister.isClickable = true
                return@setOnClickListener }
            //if all fields' values are valid register new user
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                binding.progressBarRegister.visibility = View.VISIBLE
                binding.progressBarRegister.animate()
                val result = withContext(Dispatchers.IO) { viewModel.register(binding.editTextName.text.toString(),
                    binding.editTextSurname.text.toString(), binding.editTextEmail.text.toString(),
                    binding.editTextPassword.text.toString(), binding.checkBoxPrivacyPolicy.isChecked) }
                if(result) {
                    Toast.makeText(requireContext(), string.user_register_success, Toast.LENGTH_LONG).show()
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    activity?.finish()
                }
                else {
                    binding.progressBarRegister.clearAnimation()
                    binding.progressBarRegister.visibility = View.GONE
                    AlertDialog.Builder(requireContext()).setTitle(string.user_register_error_title)
                        .setMessage(string.user_register_error_message).setPositiveButton(string.ok) { _, _ -> }.show()
                    it.isClickable = true }
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