package com.projectrestaurant.ui.loginregister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.projectrestaurant.R.string
import com.projectrestaurant.databinding.FragmentNewPasswordBinding
import com.projectrestaurant.viewmodel.LoginRegisterViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentNewPassword : Fragment() {
    private lateinit var binding: FragmentNewPasswordBinding
    private val viewModel: LoginRegisterViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentNewPasswordBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.emailContainer.setOnClickListener {
            binding.emailContainer.error = null
            binding.emailContainer.isErrorEnabled = false
            binding.emailContainer.editText?.error = null
            return@setOnClickListener }

        binding.buttonNewPassword.setOnClickListener {
            binding.buttonNewPassword.isClickable = false
            val checks = viewModel.validateEmail(binding.editTextEmail.text.toString())
            if (checks.containsValue(false)) {
                binding.emailContainer.isErrorEnabled = true
                if (checks["isBlank"]!!) {
                    binding.emailContainer.error = getString(string.user_error_blank_email)
                    binding.buttonNewPassword.isClickable = true
                    return@setOnClickListener }
                if (!checks["isValid"]!!) {
                    binding.emailContainer.error = getString(string.user_error_email_invalid)
                    binding.buttonNewPassword.isClickable = true
                    return@setOnClickListener }
            }
            GlobalScope.launch(Dispatchers.Main) {
                binding.progressBarNewPassword.visibility = View.VISIBLE
                binding.progressBarNewPassword.animate()
                val result = withContext(Dispatchers.IO) { viewModel.resetPassword(binding.editTextEmail.text.toString()) }
                if (result) {
                    AlertDialog.Builder(this@FragmentNewPassword.requireContext())
                        .setTitle(string.reset_password_email_sent_title)
                        .setMessage(string.reset_password_email_sent_message)
                        .setPositiveButton(string.ok) { _, _ -> }.show()
                } else {
                    AlertDialog.Builder(this@FragmentNewPassword.requireContext())
                        .setTitle(string.reset_password_email_error_title)
                        .setMessage(string.reset_password_email_error_message)
                        .setPositiveButton(string.ok) { _, _ -> }.show() }
                binding.progressBarNewPassword.visibility = View.GONE
                binding.progressBarNewPassword.clearAnimation()
                binding.buttonNewPassword.isClickable = true
            }
        }
    }
}
