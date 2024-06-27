package com.projectrestaurant

import com.projectrestaurant.database.Food
import com.projectrestaurant.database.Ingredient

data class CartProduct(
    val cartProductId: String,
    val food: Food,
    var extraIngredients: ArrayList<Ingredient>,
    var removedIngredients: ArrayList<Ingredient>,
    var quantity: Int,
    var price: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CartProduct
        if (cartProductId != other.cartProductId) return false
        if (food != other.food) return false
        if (extraIngredients != other.extraIngredients) return false
        if (removedIngredients != other.removedIngredients) return false
        if (quantity != other.quantity) return false
        if (price != other.price) return false
        return true
    }

    override fun hashCode(): Int {
        var result = cartProductId.hashCode()
        result = 31 * result + food.hashCode()
        result = 31 * result + extraIngredients.hashCode()
        result = 31 * result + removedIngredients.hashCode()
        result = 31 * result + quantity
        result = 31 * result + price.hashCode()
        return result
    }
}
