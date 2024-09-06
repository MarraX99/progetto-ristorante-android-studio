package com.projectrestaurant.ui.order

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.projectrestaurant.databinding.FragmentShoppingCartNoteBinding
import com.projectrestaurant.viewmodel.FoodOrderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentNote: Fragment() {
    private lateinit var binding: FragmentShoppingCartNoteBinding
    private lateinit var navController: NavController
    private val viewModel: FoodOrderViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentShoppingCartNoteBinding.inflate(inflater, container, false)
        navController = findNavController()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            val note = withContext(Dispatchers.IO) { viewModel.getOrderNote() }
            binding.editText1.text = SpannableStringBuilder(note)
        }
        binding.buttonAddNote.setOnClickListener {
            it.isClickable = false
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                it.isClickable = false
                binding.progressBar.isIndeterminate = true
                binding.constraintLayout.overlay.add(binding.progressBar)
                binding.progressBar.visibility = View.VISIBLE
                val result = withContext(Dispatchers.IO) { viewModel.setOrderNote(binding.editText1.text.toString()) }
                if(result) navController.navigateUp()
                else {
                    AlertDialog.Builder(requireContext()).setTitle(com.projectrestaurant.R.string.shopping_cart_error_title)
                        .setMessage(com.projectrestaurant.R.string.order_note_error_message)
                        .setNeutralButton(com.projectrestaurant.R.string.ok) {
                            _, _ -> navController.navigateUp() }.show()
                }
            }
        }
    }
}