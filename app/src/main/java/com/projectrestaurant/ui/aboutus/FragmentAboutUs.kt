package com.projectrestaurant.ui.aboutus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.projectrestaurant.databinding.FragmentAboutUsBinding


class FragmentAboutUs : Fragment() {

    private lateinit var binding: FragmentAboutUsBinding
    private lateinit var intent: Intent

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentAboutUsBinding.inflate(inflater, container, false)
        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val onClickListener = View.OnClickListener {
//            when(it.id) {
//                binding.phoneNumberTextView.id -> {
//                    intent = Intent(Intent.ACTION_DIAL)
//                    intent.putExtra(Intent.EXTRA_PHONE_NUMBER, com.projectrestaurant.R.string.phone_number.toString())
//                    startActivity(intent)
//                }
//
//                binding.emailTextView.id -> {
//                    intent = Intent(Intent.ACTION_SEND)
//                    intent.putExtra(Intent.EXTRA_EMAIL, com.projectrestaurant.R.string.email.toString())
//                    startActivity(intent)
//                }
//
//                binding.websiteTextView.id -> {
//                    intent = Intent(Intent.ACTION_VIEW, Uri.parse(com.projectrestaurant.R.string.website.toString()))
//                    startActivity(intent)
//                }
//            }
//        }
//        binding.websiteTextView.setOnClickListener(onClickListener)
//        binding.phoneNumberTextView.setOnClickListener(onClickListener)
//        binding.emailTextView.setOnClickListener(onClickListener)
//    }
}