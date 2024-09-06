package com.projectrestaurant.adapter

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.projectrestaurant.CartProduct
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
    class CartProductViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(com.projectrestaurant.R.id.text_view_name)
        val textViewQuantity: TextView = itemView.findViewById(com.projectrestaurant.R.id.text_view_quantity)
        val textViewPrice: TextView = itemView.findViewById(com.projectrestaurant.R.id.text_view_price)
        val textViewExtraIngredients: TextView = itemView.findViewById(com.projectrestaurant.R.id.text_view_extra_ingredients)
        val textViewRemovedIngredients: TextView = itemView.findViewById(com.projectrestaurant.R.id.text_view_removed_ingredients)
        val buttonEdit: ImageButton = itemView.findViewById(com.projectrestaurant.R.id.button_edit)
        val buttonDelete: ImageButton = itemView.findViewById(com.projectrestaurant.R.id.button_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductViewHolder {
        val itemLayout = LayoutInflater.from(parent.context).inflate(com.projectrestaurant.R.layout.item_recycler_view_cart_product, parent, false)
        return CartProductViewHolder(itemLayout)
    }

    override fun getItemCount(): Int = productsList.size

    override fun onBindViewHolder(holder: CartProductViewHolder, position: Int) {
        val cartElement = productsList.elementAt(position)
        holder.textViewName.text = cartElement.food.name
        holder.textViewPrice.text = context.getString(com.projectrestaurant.R.string.shopping_cart_product_price, String.format("%.2f", cartElement.price))
        holder.textViewQuantity.text = context.getString(com.projectrestaurant.R.string.shopping_cart_product_quantity, cartElement.quantity.toString())
        if(cartElement.extraIngredients.isEmpty()) holder.textViewExtraIngredients.visibility = View.GONE
        else {
            holder.textViewExtraIngredients.visibility = View.VISIBLE
            for(ingredient in cartElement.extraIngredients) stringBuilder.append("${context
                .getString(com.projectrestaurant.R.string.ingredient_extra_prefix)} ${ingredient.name}\n")
            holder.textViewExtraIngredients.text = stringBuilder.delete(stringBuilder.lastIndex, stringBuilder.lastIndex)
        }
        stringBuilder = stringBuilder.clear()
        if(cartElement.removedIngredients.isEmpty()) holder.textViewRemovedIngredients.visibility = View.GONE
        else {
            holder.textViewRemovedIngredients.visibility = View.VISIBLE
            for(ingredient in cartElement.removedIngredients) stringBuilder.append("${ingredient.name}\n")
            holder.textViewRemovedIngredients.text = stringBuilder.delete(stringBuilder.lastIndex, stringBuilder.lastIndex)
            holder.textViewRemovedIngredients.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }
        stringBuilder = stringBuilder.clear()
        holder.buttonDelete.setOnClickListener {
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
        holder.buttonEdit.setOnClickListener {
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