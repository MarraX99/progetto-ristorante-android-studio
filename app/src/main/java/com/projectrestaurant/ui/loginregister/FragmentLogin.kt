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
import com.projectrestaurant.databinding.FragmentLoginBinding
import com.projectrestaurant.R.string
import com.projectrestaurant.ui.order.ActivityOrder
import com.projectrestaurant.viewmodel.LoginRegisterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentLogin : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var navController: NavController
    private val viewModel: LoginRegisterViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = view.findNavController()
        if (activity is ActivityLoginRegister) (activity as ActivityLoginRegister).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.cardViewRegisterTitle.setOnClickListener { navController.navigate(com.projectrestaurant.R.id.action_fragment_login_to_fragment_register) }
        binding.textViewForgottenPassword.setOnClickListener { navController.navigate(com.projectrestaurant.R.id.action_fragment_login_to_fragment_new_password) }
        binding.emailContainer.setOnClickListener(::onClickListener)
        binding.passwordContainer.setOnClickListener(::onClickListener)

        binding.buttonLogin.setOnClickListener {
            binding.buttonLogin.isClickable = false
            //email validation
            var checks = viewModel.validateEmail(binding.editTextEmail.text.toString())
            if (checks.containsValue(false)) {
                binding.emailContainer.isErrorEnabled = true
                if (checks["isBlank"]!!) {
                    binding.emailContainer.error = getString(string.user_error_blank_email)
                    binding.buttonLogin.isClickable = true
                    return@setOnClickListener }
                if (!checks["isValid"]!!) {
                    binding.emailContainer.error = getString(string.user_error_email_invalid)
                    binding.buttonLogin.isClickable = true
                    return@setOnClickListener }
            }
            //password validation
            checks = viewModel.validatePassword(binding.editTextPassword.text.toString())
            if (checks.containsValue(false)) {
                binding.passwordContainer.isErrorEnabled = true
                if (checks["isBlank"]!!) {
                    binding.passwordContainer.error = getString(string.user_error_blank_password)
                    binding.buttonLogin.isClickable = true
                    return@setOnClickListener }
                if (!checks["hasMinimumLength"]!!) {
                    binding.passwordContainer.error = getString(string.user_error_password_short)
                    binding.buttonLogin.isClickable = true
                    return@setOnClickListener }
                if (!checks["hasMinimumUpperCases"]!!) {
                    binding.passwordContainer.error = getString(string.user_error_password_upper_case)
                    binding.buttonLogin.isClickable = true
                    return@setOnClickListener }
                if (!checks["hasMinimumLowerCases"]!!) {
                    binding.passwordContainer.error = getString(string.user_error_password_lower_case)
                    binding.buttonLogin.isClickable = true
                    return@setOnClickListener }
                if (!checks["hasMinimumDigits"]!!) {
                    binding.passwordContainer.error = getString(string.user_error_password_digit)
                    binding.buttonLogin.isClickable = true
                    return@setOnClickListener }
                if (!checks["hasMinimumSpecialCharacters"]!!) {
                    binding.passwordContainer.error = getString(string.user_error_password_special_char)
                    binding.buttonLogin.isClickable = true
                    return@setOnClickListener }
            }
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                binding.progressBarLogin.visibility = View.VISIBLE
                binding.progressBarLogin.animate()
                val result = withContext(Dispatchers.IO) {
                    viewModel.logIn(binding.editTextEmail.text.toString(), binding.editTextPassword.text.toString()) }
                if (result) {
                    Toast.makeText(requireActivity(), string.user_login_success, Toast.LENGTH_LONG).show()
                    startActivity(Intent(requireActivity(), ActivityOrder::class.java))
                    activity?.finish()
                }
                else {
                    binding.progressBarLogin.clearAnimation()
                    binding.progressBarLogin.visibility = View.GONE
                    AlertDialog.Builder(requireContext()).setTitle(string.user_login_error_title)
                        .setMessage(string.user_login_error_message)
                        .setPositiveButton(string.ok) { _, _ -> }.show()
                    binding.buttonLogin.isClickable = true }
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

