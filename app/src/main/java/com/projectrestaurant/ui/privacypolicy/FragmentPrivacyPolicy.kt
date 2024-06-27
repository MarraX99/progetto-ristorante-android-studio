package com.projectrestaurant.ui.privacypolicy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.projectrestaurant.databinding.FragmentPrivacyPolicyBinding

class FragmentPrivacyPolicy : Fragment() {
    private lateinit var binding: FragmentPrivacyPolicyBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)
        return binding.root
    }
}