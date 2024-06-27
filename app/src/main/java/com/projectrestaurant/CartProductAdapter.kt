package com.projectrestaurant

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
import androidx.recyclerview.widget.RecyclerView
import com.projectrestaurant.ui.order.FragmentShoppingCartDirections
import com.projectrestaurant.viewmodel.FoodOrderViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartProductAdapter(private val data: MutableSet<CartProduct>, private val navController: NavController, private val context: Context, private val viewModel: FoodOrderViewModel)
    : RecyclerView.Adapter<CartProductAdapter.CartProductViewHolder>() {

    //Single element of the list
    class CartProductViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.text_view_name)
        val textViewQuantity: TextView = itemView.findViewById(R.id.text_view_quantity)
        val textViewPrice: TextView = itemView.findViewById(R.id.text_view_price)
        val textViewExtraIngredients: TextView = itemView.findViewById(R.id.text_view_extra_ingredients)
        val textViewRemovedIngredients: TextView = itemView.findViewById(R.id.text_view_removed_ingredients)
        val buttonEdit: ImageButton = itemView.findViewById(R.id.button_edit)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.button_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductViewHolder {
        val itemLayout = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_view_cart_product, parent, false)
        return CartProductViewHolder(itemLayout)
    }

    override fun getItemCount(): Int = data.size

    @OptIn(DelicateCoroutinesApi::class)
    override fun onBindViewHolder(holder: CartProductViewHolder, position: Int) {
        val cartElement = data.elementAt(position)
        holder.textViewName.text = cartElement.food.name
        holder.textViewPrice.text = context.resources.getString(R.string.shopping_cart_product_price, cartElement.price)
        holder.textViewQuantity.text = context.resources.getString(R.string.shopping_cart_product_quantity, cartElement.quantity.toString())
        var tmp: StringBuilder = StringBuilder()
        if(cartElement.extraIngredients.isEmpty()) holder.textViewExtraIngredients.visibility = View.GONE
        else {
            holder.textViewExtraIngredients.visibility = View.VISIBLE
            for(ingredient in cartElement.extraIngredients) tmp.append("${context.getString(R.string.ingredient_extra_prefix)} ${ingredient.name}\n")
            holder.textViewExtraIngredients.text = tmp.delete(tmp.lastIndex, tmp.lastIndex)
        }
        tmp = tmp.clear()
        if(cartElement.removedIngredients.isEmpty()) holder.textViewRemovedIngredients.visibility = View.GONE
        else {
            holder.textViewRemovedIngredients.visibility = View.VISIBLE
            for(ingredient in cartElement.removedIngredients) tmp.append("${ingredient.name}\n")
            holder.textViewRemovedIngredients.text = tmp.delete(tmp.lastIndex, tmp.lastIndex)
            holder.textViewRemovedIngredients.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }
        holder.buttonDelete.setOnClickListener {
            AlertDialog.Builder(context).setTitle(R.string.shopping_cart_remove_item_title).setMessage(R.string.shopping_cart_remove_item_message)
                .setPositiveButton(R.string.yes){ _, _ ->
                    GlobalScope.launch(Dispatchers.Main) {
                        withContext(Dispatchers.IO) { viewModel.deleteProductFromCart(cartElement.cartProductId) }
                        data.remove(cartElement)
                        notifyItemRemoved(position)
                        if(itemCount == 0) navController.navigateUp()
                    }
                }.setNegativeButton(R.string.no){ _, _ -> return@setNegativeButton }.show()
        }
        holder.buttonEdit.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("cartProductId", cartElement.cartProductId)
            bundle.putParcelable("food", cartElement.food)
            bundle.putParcelableArrayList("extraIngredients", cartElement.extraIngredients)
            bundle.putParcelableArrayList("removedIngredients", cartElement.removedIngredients)
            bundle.putInt("quantity", cartElement.quantity)
            bundle.putString("price", cartElement.price)
            navController.navigate(FragmentShoppingCartDirections.actionFragmentShoppingCartToFragmentEditCartProduct(bundle))
        }
    }

    fun updateCartProduct(newVersion: CartProduct) {
        val position: Int = data.indexOf(data.find { it.cartProductId == newVersion.cartProductId })
        if(position != -1) {
            data.elementAt(position).let{
                it.extraIngredients = newVersion.extraIngredients
                it.removedIngredients = newVersion.removedIngredients
                it.price = newVersion.price
                it.quantity = newVersion.quantity
            }
            notifyItemChanged(position)
        }
    }

    fun getCartProducts(): Set<CartProduct> { return data }
}