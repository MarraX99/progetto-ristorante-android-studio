package com.projectrestaurant.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.app.Application
import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

@Suppress("UNCHECKED_CAST")
class FoodOrderViewModel(private val application: Application): AndroidViewModel(application) {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestoreDB by lazy { FirebaseFirestore.getInstance() }
    private val restaurantDB: RestaurantDB = RestaurantDB.getInstance(application)
    private val foodTypes: Array<String> by lazy {
        application.resources.getStringArray(com.projectrestaurant.R.array.food_types)
    }
    private val foodNames: Array<String> by lazy {
        application.resources.getStringArray(com.projectrestaurant.R.array.food_names)
    }
    private val ingredientNames: Array<String> by lazy {
        application.resources.getStringArray(com.projectrestaurant.R.array.ingredient_names)
    }
    private val foodDescriptions: Array<String> by lazy {
        application.resources.getStringArray(com.projectrestaurant.R.array.food_descriptions)
    }
    val monthNames: Array<String> by lazy {
        application.resources.getStringArray(com.projectrestaurant.R.array.month_names)
    }
    private val _foodQuantity = MutableLiveData(1)
    val foodQuantity: LiveData<Int> get() = _foodQuantity
    private val _totalPrice = MutableLiveData(0.0)
    val totalPrice: LiveData<Double> get() = _totalPrice
    private val _totalPriceString = MutableLiveData("0.00")
    val totalPriceString: LiveData<String> get() = _totalPriceString
    val userId by lazy { auth.currentUser!!.uid }
    val isLoggedIn: Boolean get() = auth.currentUser != null

    init {
        val foodTypeRef = firestoreDB.collection("food_types")
        val foodRef = firestoreDB.collection("foods")
        val ingredientRef = firestoreDB.collection("ingredients")
        foodTypeRef.addSnapshotListener { _, _ ->
            GlobalScope.launch(Dispatchers.IO) { getFoodTypesFromRemoteDatabase() }
        }
        foodRef.addSnapshotListener { _, _ ->
            GlobalScope.launch(Dispatchers.IO) { getFoodsFromRemoteDatabase() }
        }
        ingredientRef.addSnapshotListener { _, _ ->
            GlobalScope.launch(Dispatchers.IO) { getIngredientsFromRemoteDatabase() }
        }
    }

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

    suspend fun getFoodTypes(): List<FoodType> {
        if(!(restaurantDB.foodTypeDao().exists())) getFoodTypesFromRemoteDatabase()
        val list = try {
            Log.i("RoomDatabase", "Retrieving food type data...")
            restaurantDB.foodTypeDao().getAllFoodTypes()
        } catch(exception: Exception) {
            Log.e("RoomDatabase", "Error while retrieving food type data", exception)
            emptyList()
        }
        if(list.isNotEmpty()) for(i in list.indices) list[i].name = foodTypes[i]
        return list
    }

    suspend fun getFoodList(type: Int): List<Food> {
        if(!(restaurantDB.foodDao().exists())) getFoodsFromRemoteDatabase()
        val list = try {
            Log.i("RoomDatabase", "Retrieving food data")
            restaurantDB.foodDao().getFoodsByType(type)
        } catch(exception: Exception) {
            Log.e("RoomDatabase", "Error while retrieving food data", exception)
            emptyList()
        }
        if(list.isNotEmpty()) {
            for(i in list.indices) {
                list[i].name = foodNames[list[i].foodId]
                list[i].description = foodDescriptions[list[i].foodId]
            }
        }
        return list
    }

    fun incrementFoodQuantity() { _foodQuantity.value = _foodQuantity.value!! + 1 }

    fun decrementFoodQuantity() { _foodQuantity.value = _foodQuantity.value!! - 1 }

    fun setFoodQuantity(value: Int) { if(value > 0) _foodQuantity.value = value }

    suspend fun getIngredientsFromFood(food: Food): List<Ingredient> {
        if(!(restaurantDB.ingredientDao().exists())) getIngredientsFromRemoteDatabase()
        val list = try {
            Log.i("RoomDatabase", "Retrieving ingredients data...")
            restaurantDB.foodIngredientDao().getIngredientsByFoodId(food.foodId)
        } catch(exception: Exception) {
            Log.e("RoomDatabase", "Error while retrieving ingredients data", exception)
            emptyList()
        }
        if(list.isNotEmpty()) for(i in list.indices) list[i].name = ingredientNames[list[i].ingredientId]
        return list
    }

    fun addToPrice(value: Double) {
        _totalPrice.value = _totalPrice.value!! + value
        _totalPriceString.value = String.format("%.2f", _totalPrice.value!!)
    }

    fun removeToPrice(value: Double) {
        _totalPrice.value = _totalPrice.value!! - value
        _totalPriceString.value = String.format("%.2f", _totalPrice.value!!)
    }

    fun resetPrice() {
        _totalPrice.value = 0.0
        _totalPriceString.value = String.format("%.2f", _totalPrice.value!!)
    }

    suspend fun getCartProducts(userId: String): MutableSet<CartProduct> {
        val list = mutableSetOf<CartProduct>()
        try {
            val result = firestoreDB.collection("users/${auth.currentUser!!.uid}/shopping_cart").get().await()
            var food: Food?
            var cartProduct: CartProduct
            var ingredientList : ArrayList<DocumentReference>
            var ingredient: Ingredient?
            for(document in result) {
                food = restaurantDB.foodDao().getFoodById((document.data["food"] as DocumentReference).id.toInt())
                food!!.name = foodNames[food.foodId]
                food.description = foodDescriptions[food.foodId]
                cartProduct = CartProduct(document.id, food, arrayListOf(), arrayListOf(), document.data["quantity"].toString().toInt(), document.data["price"].toString().toDouble())
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

    suspend fun createOrder(cartProducts: Set<CartProduct>, deliveryDate: Calendar, useDeliveryAddress: Boolean): Boolean {
        val order: DocumentReference = firestoreDB.collection("orders").document()
        val user = firestoreDB.document("users/${auth.currentUser!!.uid}").get().await()
        var totalPrice = 0.0
        val result = try {
            order.set(hashMapOf(
                "note" to getOrderNote(),
                "user" to user.reference,
                "order_date_time" to FieldValue.serverTimestamp(), "delivery_date_time" to Timestamp(deliveryDate.time),
                "delivery_address" to (if(useDeliveryAddress) user.data!!["default_delivery_address"] as DocumentReference else null))).await()
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
                totalPrice += product.price
            }
            order.set(hashMapOf("total_price" to totalPrice), SetOptions.merge())
            true
        } catch(exception: Exception) {
            Log.e("FirebaseFirestore", "Error while creating new order", exception)
            firestoreDB.document("orders/${order.id}").delete().await()
            false
        }
        return if(result) deleteShoppingCart() else false
    }

    suspend fun addProductToShoppingCart(food: Food, extraIngredients: List<Ingredient>, removedIngredients: List<Ingredient>): Boolean {
        return try {
            val foodRef = firestoreDB.document("foods/${food.foodId}")
            val extra = arrayListOf<DocumentReference>()
            for (ingredient in extraIngredients) {
                extra.add(firestoreDB.document("ingredients/${ingredient.ingredientId}"))
            }
            val removed = arrayListOf<DocumentReference>()
            for (ingredient in removedIngredients) {
                removed.add(firestoreDB.document("ingredients/${ingredient.ingredientId}"))
            }
            val tmp = firestoreDB.collection("users/${auth.currentUser!!.uid}/shopping_cart")
                .where(Filter.and(
                    Filter.equalTo("price", _totalPrice.value!!),
                    Filter.equalTo("food", foodRef),
                    Filter.equalTo("extra_ingredients", extra),
                    Filter.equalTo("removed_ingredients", removed))).limit(1).get().await()
            if (tmp.isEmpty) {
                val new = firestoreDB.collection("users/${auth.currentUser!!.uid}/shopping_cart").document()
                new.set(hashMapOf(
                        "food" to foodRef, "extra_ingredients" to extra, "removed_ingredients" to removed,
                        "quantity" to _foodQuantity.value!!, "price" to _totalPrice.value!!), SetOptions.merge()).await()
                Log.i("FirebaseFirestore", "Cart product successfully added to shopping cart with ID: ${new.id}")
            } else {
                Log.i("FirebaseFirestore", "Cart product already exists with ID: ${tmp.documents[0].id}")
                firestoreDB.document("users/${auth.currentUser!!.uid}/shopping_cart/${tmp.documents[0].id}")
                    .set(hashMapOf("quantity" to tmp.documents[0].data!!["quantity"].toString().toInt() + _foodQuantity.value!!), SetOptions.merge())
                Log.i("FirebaseFirestore", "Increased cart product's quantity")
            }
            true
        } catch(exception: Exception) {
            Log.e("FirebaseFirestore", "Error while creating a new shopping cart product", exception)
            false
        }
    }

    suspend fun getFoodImage(foodId: Int): String? = restaurantDB.foodDao().getFoodById(foodId)?.imageUri

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
        return firestoreDB.document("users/${auth.currentUser!!.uid}").get().await().data?.get("order_note").toString()
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

    fun getDeliveryDays(): List<Long> {
        val days = mutableListOf<Long>()
        val day: Calendar = Calendar.getInstance()
        day.time = Date()
        if(day[Calendar.HOUR_OF_DAY] in 0..22 || (day[Calendar.HOUR_OF_DAY] == 23 && day[Calendar.MINUTE] <= 15)) days.add(day.timeInMillis)
        for(i: Int in 1..4) {
            day.timeInMillis += 86400000    // 1 day = 86400000 milliseconds
            days.add(day.timeInMillis)
        }
        return days
    }

    fun getHours(day: Calendar): List<Long> {
        val hours = mutableListOf<Long>()
        val startHour: Calendar = Calendar.getInstance()
        val currentDay: Calendar =  Calendar.getInstance()
        currentDay.time = Date()
        if(day[Calendar.DAY_OF_MONTH] == currentDay[Calendar.DAY_OF_MONTH]) {
            startHour.time = currentDay.time
            when(currentDay[Calendar.HOUR_OF_DAY]) {
                in 0..17 -> with(startHour) {
                    set(Calendar.HOUR_OF_DAY, 18); set(Calendar.MINUTE, 30)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                in 18..22 -> {
                    when(currentDay[Calendar.MINUTE]) {
                        in 0..15 -> with(startHour) {
                            set(Calendar.HOUR_OF_DAY, currentDay[Calendar.HOUR_OF_DAY])
                            set(Calendar.MINUTE, 30); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                        }
                        in 16..30 -> with(startHour) {
                            set(Calendar.HOUR_OF_DAY, currentDay[Calendar.HOUR_OF_DAY])
                            set(Calendar.MINUTE, 45); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                        }
                        in 31..45 -> with(startHour) {
                            set(Calendar.HOUR_OF_DAY, ++(currentDay[Calendar.HOUR_OF_DAY]))
                            set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                        }
                        else -> with(startHour) { // else branch => between xx:46 and (++xx):00 where xx is the current hour
                            set(Calendar.HOUR_OF_DAY, ++(currentDay[Calendar.HOUR_OF_DAY]))
                            set(Calendar.MINUTE, 15); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                        }
                    }
                }
                23 -> with(startHour) {
                    set(Calendar.HOUR_OF_DAY, currentDay[Calendar.HOUR_OF_DAY])
                    set(Calendar.MINUTE, 30); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
            }
            hours.add(startHour.timeInMillis)
            while(startHour.get(Calendar.HOUR_OF_DAY) <= 22) { // from startHour until 23:00 if hour != 23
                startHour.timeInMillis += 900000  // 15 minutes = 900000 milliseconds
                hours.add(startHour.timeInMillis)
            }
            startHour.timeInMillis += 900000  // 15 minutes = 900000 milliseconds
            hours.add(startHour.timeInMillis)   // adding 23:15
        } else {
            with(startHour) {
                time = day.time
                set(Calendar.HOUR_OF_DAY, 18); set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            hours.add(startHour.timeInMillis)
            while(startHour.get(Calendar.HOUR_OF_DAY) <= 22) { // from 18:45 until 23:00
                startHour.timeInMillis += 900000  // 15 minutes = 900000 milliseconds
                hours.add(startHour.timeInMillis)
            }
            startHour.timeInMillis += 900000  // 15 minutes = 900000 milliseconds
            hours.add(startHour.timeInMillis)   // adding 23:15
        }
        return hours
    }

    /* PRIVATE FUNCTIONS HERE */

    private suspend fun getFoodTypesFromRemoteDatabase() {
        try {
            Log.i("Firestore", "Retrieving food type data")
            val resultDB = firestoreDB.collection("food_types").get().await()
            for(document in resultDB)
                restaurantDB.foodTypeDao().insert(FoodType(document.id.toInt(), "", document.data["image_uri"].toString()))
        } catch(exception: Exception) { Log.e("Firestore", "Error while retrieving food type data", exception) }
    }

    private suspend fun getFoodsFromRemoteDatabase() {
        try {
            Log.i("Firestore", "Retrieving food data...")
            val resultDB = firestoreDB.collection("foods").get().await()
            var types: DocumentSnapshot
            for(document in resultDB) {
                types = (document.data["type"] as DocumentReference).get().await()
                restaurantDB.foodDao().insert(Food(document.id.toInt(), "", "", types.id.toInt(), document.data["unit_price"].toString().toDouble(), document.data["image_uri"].toString()))
            }
        } catch(exception: Exception) { Log.e("Firestore", "Error while retrieving food data", exception) }
    }

    private suspend fun getIngredientsFromRemoteDatabase() {
        try {
            Log.i("Firestore", "Retrieving ingredients data...")
            val resultDB = firestoreDB.collection("ingredients").get().await()
            for(document in resultDB)
                restaurantDB.ingredientDao().insert(Ingredient(document.id.toInt(),"" ,document.data["unit_price"].toString().toDouble(), document.data["image_uri"] as String))

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

    private suspend fun deleteShoppingCart(): Boolean {
        return try {
            val productsToDelete = firestoreDB.collection("users/${auth.currentUser!!.uid}/shopping_cart").get().await()
            for(product in productsToDelete.documents) product.reference.delete().await()
            Log.i("FirebaseFirestore", "User's shopping cart successfully deleted")
            true
        } catch(exception: Exception) {
            Log.e("FirebaseFirestore", "Error while deleting user's shopping cart", exception)
            false
        }
    }
}