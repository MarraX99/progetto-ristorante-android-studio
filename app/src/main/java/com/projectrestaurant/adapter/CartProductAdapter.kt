package com.projectrestaurant.adapter

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.projectrestaurant.CartProduct
import com.projectrestaurant.databinding.ItemRecyclerViewCartProductBinding
import com.projectrestaurant.ui.order.FragmentShoppingCartDirections
import com.projectrestaurant.viewmodel.FoodOrderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartProductAdapter(private val navController: NavController, private val context: Context, private val viewModel: FoodOrderViewModel):
    ListAdapter<CartProduct, CartProductAdapter.CartProductViewHolder>(ItemDiffCallback()) {

    private var stringBuilder: StringBuilder = StringBuilder()
    lateinit var productsList: MutableSet<CartProduct>
        private set

    fun setData(data: MutableSet<CartProduct>) {
        productsList = data
        submitList(productsList.toList())
    }

    //Single element of the list
    class CartProductViewHolder(val binding: ItemRecyclerViewCartProductBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductViewHolder {
        val binding = ItemRecyclerViewCartProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartProductViewHolder(binding)
    }

    override fun getItemCount(): Int = productsList.size

    override fun onBindViewHolder(holder: CartProductViewHolder, position: Int) {
        val cartElement = productsList.elementAt(position)
        with(holder.binding) {
            textViewName.text = cartElement.food.name
            textViewPrice.text = context.getString(com.projectrestaurant.R.string.shopping_cart_product_price, String.format("%.2f", cartElement.price))
            textViewQuantity.text = context.getString(com.projectrestaurant.R.string.shopping_cart_product_quantity, cartElement.quantity.toString())
            if(cartElement.extraIngredients.isEmpty()) textViewExtraIngredients.isVisible = false
            else {
                textViewExtraIngredients.isVisible = true
                for(ingredient in cartElement.extraIngredients) stringBuilder.append("${context
                    .getString(com.projectrestaurant.R.string.ingredient_extra_prefix)} ${ingredient.name}\n")
                textViewExtraIngredients.text = stringBuilder.deleteCharAt(stringBuilder.lastIndex)
            }
            stringBuilder = stringBuilder.clear()
            if(cartElement.removedIngredients.isEmpty()) textViewRemovedIngredients.isVisible = false
            else {
                textViewRemovedIngredients.isVisible = true
                for(ingredient in cartElement.removedIngredients) stringBuilder.append("${ingredient.name}\n")
                textViewRemovedIngredients.text = stringBuilder.deleteCharAt(stringBuilder.lastIndex)
                textViewRemovedIngredients.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            }
            stringBuilder = stringBuilder.clear()
            buttonDelete.setOnClickListener {
                AlertDialog.Builder(context).setTitle(com.projectrestaurant.R.string.shopping_cart_remove_item_title)
                    .setMessage(com.projectrestaurant.R.string.shopping_cart_remove_item_message)
                    .setPositiveButton(com.projectrestaurant.R.string.yes){ _, _ ->
                        GlobalScope.launch(Dispatchers.Main) {
                            withContext(Dispatchers.IO) { viewModel.deleteProductFromCart(cartElement.cartProductId) }
                            productsList.remove(cartElement)
                            notifyItemRemoved(position)
                            if(itemCount == 0) navController.navigateUp()
                        }
                    }.setNegativeButton(com.projectrestaurant.R.string.no){ _, _ -> return@setNegativeButton }.show()
            }
            buttonEdit.setOnClickListener {
                val bundle = Bundle()
                with(bundle) {
                    putString("cartProductId", cartElement.cartProductId)
                    putParcelable("food", cartElement.food)
                    putParcelableArrayList("extraIngredients", cartElement.extraIngredients)
                    putParcelableArrayList("removedIngredients", cartElement.removedIngredients)
                    putInt("quantity", cartElement.quantity)
                    putDouble("price", cartElement.price)
                }
                navController.navigate(FragmentShoppingCartDirections.actionFragmentShoppingCartToFragmentEditCartProduct(bundle))
            }
        }
    }

    fun updateCartProduct(newVersion: CartProduct) {
        val position: Int = productsList.indexOf(productsList.find { it.cartProductId == newVersion.cartProductId })
        if(position != -1) {
            with(productsList.elementAt(position)) {
                extraIngredients = newVersion.extraIngredients
                removedIngredients = newVersion.removedIngredients
                price = newVersion.price
                quantity = newVersion.quantity
            }
            notifyItemChanged(position)
        }
    }

    companion object {
        class ItemDiffCallback : DiffUtil.ItemCallback<CartProduct>() {
            override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean { return oldItem === newItem }

            override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean { return oldItem.cartProductId == newItem.cartProductId }
        }
    }
}