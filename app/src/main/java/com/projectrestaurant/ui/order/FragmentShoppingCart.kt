package com.projectrestaurant.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.projectrestaurant.CartProduct
import com.projectrestaurant.CartProductAdapter
import com.projectrestaurant.databinding.FragmentShoppingCartBinding
import com.projectrestaurant.viewmodel.FoodOrderViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentShoppingCart: Fragment() {
    private lateinit var binding: FragmentShoppingCartBinding
    private val viewModel: FoodOrderViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding = FragmentShoppingCartBinding.inflate(inflater, container, false)
        binding.recyclerViewShoppingCart.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        navController = findNavController()
        setFragmentResultListener("modifiedCartProduct") { requestKey, bundle ->
            if(requestKey == "modifiedCartProduct") {
                if(binding.recyclerViewShoppingCart.adapter != null) {
                    (binding.recyclerViewShoppingCart.adapter as CartProductAdapter).updateCartProduct(
                        CartProduct(bundle.getString("cartProductId")!!, bundle.getParcelable("food")!!,
                            bundle.getParcelableArrayList("extraIngredients")!!,
                            bundle.getParcelableArrayList("removedIngredients")!!,
                            bundle.getInt("quantity"), bundle.getString("price")!!))
                }
            }
        }
        return binding.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlobalScope.launch(Dispatchers.Main) {
            if(auth.currentUser != null) {
                val list = withContext(Dispatchers.IO) {
                    viewModel.getCartProducts(auth.currentUser!!.uid,
                        resources.getStringArray(com.projectrestaurant.R.array.food_names),
                        resources.getStringArray(com.projectrestaurant.R.array.food_descriptions),
                        resources.getStringArray(com.projectrestaurant.R.array.ingredient_names))
                }
                if(list.isNotEmpty()) binding.recyclerViewShoppingCart.adapter = CartProductAdapter(list, findNavController(), requireContext(), viewModel)
                binding.textViewNote.setOnClickListener {
                    navController.navigate(com.projectrestaurant.R.id.action_fragment_shopping_cart_to_fragment_note)
                }
            }
        }
        binding.buttonOrder.setOnClickListener {
            it.isClickable = false
            binding.progressBar.isIndeterminate = true
            binding.constraintLayout.overlay.add(binding.progressBar)
            binding.progressBar.visibility = View.VISIBLE
            if(viewModel.isOnline(requireContext().applicationContext)) {
                if(viewModel.isLoggedIn()) {
                    GlobalScope.launch(Dispatchers.Main) {
                        val result =
                            withContext(Dispatchers.IO) { viewModel.createOrder((binding.recyclerViewShoppingCart.adapter as CartProductAdapter).getCartProducts()) }
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