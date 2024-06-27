package com.projectrestaurant.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.projectrestaurant.CartProduct
import com.projectrestaurant.database.Food
import com.projectrestaurant.database.FoodIngredient
import com.projectrestaurant.database.FoodType
import com.projectrestaurant.database.Ingredient
import com.projectrestaurant.database.RestaurantDB
import kotlinx.coroutines.tasks.await
import java.util.Locale

class FoodOrderViewModel: ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestoreDB = FirebaseFirestore.getInstance()
    private lateinit var restaurantDB: RestaurantDB

    private val _foodQuantity = MutableLiveData(1)
    val foodQuantity: LiveData<Int>
        get() = _foodQuantity

    private var totalPrice = 0.0

    private val _totalPriceString = MutableLiveData("0.00")
    val totalPriceString: LiveData<String>
        get() = _totalPriceString

    suspend fun getFoodTypes(types: Array<String>): List<FoodType>? {
        val list = try {
            Log.i("RoomDatabase", "Retrieving food type data...")
            restaurantDB.foodTypeDao().getAllFoodTypes()
        } catch(exception: Exception) {
            Log.e("RoomDatabase", "Error while retrieving food type data", exception)
            null
        }
        if(list != null) for(i in list.indices) list[i].name = types[i]
        return list
    }

    suspend fun getFoodTypesFromRemoteDatabase() {
        try {
            Log.i("Firestore", "Retrieving food type data")
            val resultDB = firestoreDB.collection("food_types").get().await()
            for(document in resultDB)
                restaurantDB.foodTypeDao().insert(FoodType(document.id.toInt(), "", document.data["image_uri"].toString()))
        } catch(exception: Exception) { Log.e("Firestore", "Error while retrieving food type data", exception) }
    }

    suspend fun foodTypeTableExists(): Boolean = restaurantDB.foodTypeDao().exists()

    fun setRestaurantDB(db: RestaurantDB) { restaurantDB = db }

    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true }
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true }
        }
        return false
    }

    fun isLoggedIn(): Boolean { return auth.currentUser != null }

    suspend fun foodTableExists(): Boolean = restaurantDB.foodDao().exists()

    suspend fun getFoodList(type: Int, titles: Array<String>, descriptions: Array<String>): List<Food>? {
        val list = try {
            Log.i("RoomDatabase", "Retrieving food data")
            restaurantDB.foodDao().getFoodsByType(type)
        } catch(exception: Exception) {
            Log.e("RoomDatabase", "Error while retrieving food data", exception)
            null
        }
        if(list != null) {
            for(i in list.indices) {
                list[i].name = titles[list[i].foodId]
                list[i].description = descriptions[list[i].foodId]
            }
        }
        return list
    }

    suspend fun getFoodsFromRemoteDatabase() {
        try {
            Log.i("Firestore", "Retrieving food data...")
            val resultDB = firestoreDB.collection("foods").get().await()
            for(document in resultDB)
                restaurantDB.foodDao().insert(Food(document.id.toInt(), "", "", document.data["type"].toString().toInt(), document.data["unit_price"].toString(), document.data["image_uri"].toString()))
        } catch(exception: Exception) { Log.e("Firestore", "Error while retrieving food type data", exception) }
    }

    fun incrementFoodQuantity() { _foodQuantity.value = _foodQuantity.value!! + 1 }

    fun decrementFoodQuantity() { _foodQuantity.value = _foodQuantity.value!! - 1 }

    fun setFoodQuantity(value: Int) { if(value > 0) _foodQuantity.value = value }

    suspend fun ingredientTableExists(): Boolean = restaurantDB.ingredientDao().exists()

    suspend fun getIngredientsFromFood(food: Food, names: Array<String>): List<Ingredient>? {
        val list = try {
            Log.i("RoomDatabase", "Retrieving ingredients data...")
            restaurantDB.foodIngredientDao().getIngredientsByFoodId(food.foodId)
        } catch(exception: Exception) {
            Log.e("RoomDatabase", "Error while retrieving ingredients data", exception)
            null
        }
        if(list != null) for(i in list.indices) list[i].name = names[list[i].ingredientId]
        return list
    }

    suspend fun getIngredientsFromRemoteDatabase() {
        try {
            Log.i("Firestore", "Retrieving ingredients data...")
            val resultDB = firestoreDB.collection("ingredients").get().await()
            for(document in resultDB)
                restaurantDB.ingredientDao().insert(Ingredient(document.id.toInt(),"" ,document.data["unit_price"].toString(), document.data["image_uri"].toString()))

            Log.i("Firestore", "Retrieving food ingredients data...")
            val resultDB2 = firestoreDB.collection("food_ingredients").get().await()
            var ingredientIds: List<String>
            for(document in resultDB2) {
                ingredientIds = document.get("ingredient_ids") as List<String>
                if(ingredientIds.isNotEmpty())
                    for(ingrId in ingredientIds) restaurantDB.foodIngredientDao().insert(FoodIngredient(document.data["food_id"].toString().toInt(), ingrId.toInt()))
            }
        } catch(exception: Exception) { Log.e("Firestore", "Error while retrieving ingredients data", exception) }
    }

    @SuppressLint("DefaultLocale")
    fun addToPrice(value: Double) {
        totalPrice += value
        _totalPriceString.value = String.format(Locale.US ,"%.2f", totalPrice)
    }

    @SuppressLint("DefaultLocale")
    fun removeToPrice(value: Double) {
        totalPrice -= value
        _totalPriceString.value = String.format(Locale.US,"%.2f", totalPrice)
    }

    @SuppressLint("DefaultLocale")
    fun resetPrice() {
        totalPrice = 0.0
        _totalPriceString.value = String.format(Locale.US, "%.2f", totalPrice)
    }

    suspend fun getCartProducts(userId: String, foodNames: Array<String>, foodDescriptions: Array<String>, ingredientNames: Array<String>): MutableSet<CartProduct> {
        val list = mutableSetOf<CartProduct>()
        try {
            val result = firestoreDB.collection("users/$userId/shopping_cart").get().await()
            var food: Food?
            var cartProduct: CartProduct
            var ingredientList : ArrayList<DocumentReference>
            var ingredient: Ingredient?
            for(document in result) {
                food = restaurantDB.foodDao().getFoodById((document.data["food"] as DocumentReference).id.toInt())
                food!!.name = foodNames[food.foodId]
                food.description = foodDescriptions[food.foodId]
                cartProduct = CartProduct(document.id, food, arrayListOf(), arrayListOf(), document.data["quantity"].toString().toInt(), document.data["price"].toString())
                ingredientList = document.data["extra_ingredients"] as ArrayList<DocumentReference>
                if(ingredientList.isNotEmpty()) {
                    for(i in ingredientList) {
                        ingredient = restaurantDB.ingredientDao().getIngredientById(i.id.toInt())
                        ingredient!!.name = ingredientNames[ingredient.ingredientId]
                        cartProduct.extraIngredients.add(ingredient)
                    }
                }
                ingredientList = document.data["removed_ingredients"] as ArrayList<DocumentReference>
                if(ingredientList.isNotEmpty()) {
                    for(i in ingredientList) {
                        ingredient = restaurantDB.ingredientDao().getIngredientById(i.id.toInt())
                        ingredient!!.name = ingredientNames[ingredient.ingredientId]
                        cartProduct.removedIngredients.add(ingredient)
                    }
                }
                list.add(cartProduct)
            }
        } catch(exception: Exception) { Log.e("FirebaseFirestore", "User: $userId - Failed to get shopping cart data", exception) }
        return list
    }

    suspend fun createOrder(cartProducts: Set<CartProduct>): Boolean {
        val order: DocumentReference = firestoreDB.collection("orders").document()
        return try {
            order.set(hashMapOf(
                "note" to getOrderNote(),
                "user" to firestoreDB.collection("users").document(auth.currentUser!!.uid),
                "order_date_time" to FieldValue.serverTimestamp())).await()
            setOrderNote("")
            val products = order.collection("products")
            for(product in cartProducts) {
                val extra = arrayListOf<DocumentReference>()
                for(ingredient in product.extraIngredients) extra.add(firestoreDB.document("ingredients/${ingredient.ingredientId}"))
                val removed = arrayListOf<DocumentReference>()
                for(ingredient in product.removedIngredients) removed.add(firestoreDB.document("ingredients/${ingredient.ingredientId}"))
                products.document().set(hashMapOf(
                    "food" to firestoreDB.collection("foods").document(product.food.foodId.toString()),
                    "quantity" to product.quantity,
                    "price" to product.price,
                    "extra_ingredients" to extra,
                    "removed_ingredients" to removed)).await()
            }
            val products2 = firestoreDB.collection("users/${auth.currentUser!!.uid}/shopping_cart").get().await()
            for(p in products2.documents) p.reference.delete().await()
            true
        } catch(exception: Exception) {
            Log.e("FirebaseFirestore", "Error while creating new order", exception)
            firestoreDB.document("orders/${order.id}").delete().await()
            false
        }
    }


    fun createCartProductOnDatabase(): String {
        return firestoreDB.collection("users/${auth.currentUser!!.uid}/shopping_cart").document().id
    }

    suspend fun addProductToShoppingCart(product: CartProduct): Boolean {
        return try {
            val foodRef = firestoreDB.document("foods/${product.food.foodId}")
            val extra = arrayListOf<DocumentReference>()
            for (ingredient in product.extraIngredients) {
                extra.add(firestoreDB.document("ingredients/${ingredient.ingredientId}"))
            }
            val removed = arrayListOf<DocumentReference>()
            for (ingredient in product.removedIngredients) {
                removed.add(firestoreDB.document("ingredients/${ingredient.ingredientId}"))
            }
            val tmp = firestoreDB.collection("users/${auth.currentUser!!.uid}/shopping_cart")
                .where(Filter.and(
                    Filter.equalTo("price", product.price),
                    Filter.equalTo("food", foodRef),
                    Filter.equalTo("extra_ingredients", extra),
                    Filter.equalTo("removed_ingredients", removed))).limit(1).get().await()
            if (tmp.isEmpty) {
                firestoreDB.collection("users/${auth.currentUser!!.uid}/shopping_cart")
                    .document(product.cartProductId).set(hashMapOf(
                        "food" to foodRef,
                        "extra_ingredients" to extra,
                        "removed_ingredients" to removed,
                        "quantity" to product.quantity,
                        "price" to product.price)).await()
                Log.i("FirebaseFirestore", "Cart product successfully added to shopping cart with ID: ${product.cartProductId}")
            } else {
                Log.i("FirebaseFirestore", "Cart product already exists with ID: ${tmp.documents[0].id}")
                firestoreDB.document("users/${auth.currentUser!!.uid}/shopping_cart/${tmp.documents[0].id}")
                    .set(hashMapOf("quantity" to tmp.documents[0].data!!["quantity"].toString().toInt() + product.quantity), SetOptions.merge())
                Log.i("FirebaseFirestore", "Increased cart product's quantity")
            }
            true
        } catch(exception: Exception) {
            Log.e("FirebaseFirestore", "Error while creating a new shopping cart product", exception)
            false
        }
    }

    suspend fun isShoppingCartEmpty(): Boolean {
        return firestoreDB.collection("users/${auth.currentUser!!.uid}/shopping_cart").limit(1).get().await().isEmpty
    }

    suspend fun deleteProductFromCart(productId: String) {
        firestoreDB.document("users/${auth.currentUser!!.uid}/shopping_cart/$productId").delete().await()
    }

    suspend fun setOrderNote(note: String): Boolean {
        return try {
            firestoreDB.document("users/${auth.currentUser!!.uid}")
                .set(hashMapOf("order_note" to note), SetOptions.merge()).await()
            Log.i("FirebaseFirestore", "Order's note successfully updated!")
            true
        } catch(exception: Exception) {
            Log.e("FirebaseFirestore", "Error while updating order's note", exception)
            false
        }
    }

    suspend fun getOrderNote(): String {
        return firestoreDB.document("users/${auth.currentUser!!.uid}").get().await().data?.get("order_note").toString() ?: ""
    }

    suspend fun updateCartProduct(cartProduct: CartProduct): Boolean {
        return try{
            val extra = arrayListOf<DocumentReference>()
            for(ingredient in cartProduct.extraIngredients)
                extra.add(firestoreDB.document("ingredients/${ingredient.ingredientId}"))
            val removed = arrayListOf<DocumentReference>()
            for(ingredient in cartProduct.removedIngredients)
                removed.add(firestoreDB.document("ingredients/${ingredient.ingredientId}"))
            firestoreDB.document("users/${auth.currentUser!!.uid}/shopping_cart/${cartProduct.cartProductId}")
                .set(hashMapOf("extra_ingredients" to extra, "removed_ingredients" to removed,
                    "quantity" to cartProduct.quantity, "price" to cartProduct.price,), SetOptions.merge()).await()
            Log.i("FirebaseFirestore", "Cart product's data successfully updated! - Cart product's ID: ${cartProduct.cartProductId}")
            true
        } catch(exception: Exception) {
            Log.e("FirebaseFirestore", "Error while updating cart product's data - Cart product's ID: ${cartProduct.cartProductId}", exception)
            false
        }
    }
}