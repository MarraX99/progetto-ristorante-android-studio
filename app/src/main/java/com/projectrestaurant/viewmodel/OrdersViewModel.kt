package com.projectrestaurant.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.projectrestaurant.IngredientQuantity
import com.projectrestaurant.database.OrderProduct
import com.projectrestaurant.database.Order
import com.projectrestaurant.database.OrderProductEdit
import com.projectrestaurant.database.RestaurantDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class OrdersViewModel(private val application: Application): AndroidViewModel(application) {
    private val firestoreDB = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val NUMBER_OF_MONTHS: Byte = 1
    private val MILLISECONDS_PER_MONTH = 2592000000  //Milliseconds in 30 days
    private val restaurantDB: RestaurantDB = RestaurantDB.getInstance(application)
    val foodNames: Array<String> by lazy {
        application.resources.getStringArray(com.projectrestaurant.R.array.food_names)
    }
    val ingredientNames: Array<String> by lazy {
        application.resources.getStringArray(com.projectrestaurant.R.array.ingredient_names)
    }
    val monthNames: Array<String> by lazy {
        application.resources.getStringArray(com.projectrestaurant.R.array.month_names)
    }

    init {
        val NUMBER_OF_MONTHS: Byte = 1
        val MILLISECONDS_PER_MONTH = 2592000000  //Milliseconds in 30 days
        val currentTimestamp = Timestamp.now().toDate().time
        val greaterDate = Date(currentTimestamp - NUMBER_OF_MONTHS * MILLISECONDS_PER_MONTH)
        val userRef = firestoreDB.document("users/${auth.currentUser!!.uid}")
        val orderRef = firestoreDB.collection("orders").whereEqualTo("user", userRef)
            .whereGreaterThanOrEqualTo("order_date_time", greaterDate)
        orderRef.addSnapshotListener { _, _ ->
            GlobalScope.launch(Dispatchers.IO) {
                restaurantDB.orderDao().deleteOlderOrders(auth.currentUser!!.uid)
                if(restaurantDB.orderDao().exists(auth.currentUser!!.uid)) getMostRecentOrderFromRemoteDatabase()
                else getOrdersFromRemoteDatabase() }
        }
    }

    suspend fun getOrders(): List<Order> {
        if(!(restaurantDB.orderDao().exists(auth.currentUser!!.uid))) getOrdersFromRemoteDatabase()
        return try {
            Log.i("Room", "Retrieving orders' data")
            restaurantDB.orderDao().getAllOrders(auth.currentUser!!.uid)
        } catch(exception: Exception) {
            Log.e("Room", "Error while retrieving orders' data", exception)
            emptyList()
        }
    }

    suspend fun getOrderProducts(order: Order): List<OrderProduct> {
        return try {
            Log.i("Room", "Retrieving products for order ID: ${order.orderId}")
            restaurantDB.orderProductDao().getOrderProductsByOrderId(order.orderId)
        } catch(exception: Exception) {
            Log.e("Room", "Error while retrieving products for order ID: ${order.orderId}", exception)
            emptyList()
        }
    }

    suspend fun getOrderProductEdits(orderProduct: OrderProduct): List<OrderProductEdit> {
        return try {
            Log.i("Room", "Retrieving ingredient edits for order product ID: ${orderProduct.orderProductId}")
            restaurantDB.orderProductEditDao().getOrderProductEditsByProductId(orderProduct.orderProductId)
        } catch(exception: Exception) {
            Log.e("Room", "Error while retrieving ingredient edits for order product ID: ${orderProduct.orderProductId}", exception)
            emptyList()
        }
    }

    suspend fun getFoodImage(foodId: Int): String? = restaurantDB.foodDao().getFoodById(foodId)?.imageUri

    /* PRIVATE FUNCTIONS HERE */

    @Suppress("UNCHECKED_CAST")
    private suspend fun getOrdersFromRemoteDatabase() {
        var orderProducts: QuerySnapshot
        val currentTimestamp = Timestamp.now().toDate().time
        val greaterDate = Date(currentTimestamp - (NUMBER_OF_MONTHS * MILLISECONDS_PER_MONTH))
        val userRef = firestoreDB.document("users/${auth.currentUser!!.uid}")
        val orders = firestoreDB.collection("orders").whereEqualTo("user", userRef)
            .whereGreaterThanOrEqualTo("order_date_time", greaterDate).get().await()
        for(order in orders) {
            orderProducts = firestoreDB.collection("orders/${order.id}/products").get().await()
            restaurantDB.orderDao().insert(Order(order.id, auth.currentUser!!.uid, (order.data["order_date_time"] as Timestamp).toDate(),
                (order.data["delivery_date_time"] as Timestamp).toDate(), order.data["total_price"].toString().toDouble()))
            for(product in orderProducts) {
                val foodRef = (product.data["food"] as DocumentReference).get().await()
                restaurantDB.orderProductDao().insert(OrderProduct(product.id, order.id, foodRef.id.toInt(),
                    product.data["quantity"].toString().toInt(), product.data["price"].toString().toDouble()))
                for(edit in (product.data["extra_ingredients"] as ArrayList<DocumentReference>)) {
                    restaurantDB.orderProductEditDao().insert(OrderProductEdit(product.id, edit.id.toInt(), IngredientQuantity.INGREDIENT_EXTRA))
                }
                for(edit in (product.data["removed_ingredients"] as ArrayList<DocumentReference>)) {
                    restaurantDB.orderProductEditDao().insert(OrderProductEdit(product.id, edit.id.toInt(), IngredientQuantity.INGREDIENT_REMOVED))
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun getMostRecentOrderFromRemoteDatabase(): Boolean {
        val userRef = firestoreDB.document("users/${auth.currentUser!!.uid}")
        val orderRef = firestoreDB.collection("orders").whereEqualTo("user", userRef).orderBy("order_date_time", Query.Direction.DESCENDING).limit(1).get().await()
        val orderProducts = firestoreDB.collection("orders/${orderRef.documents[0].id}/products").get().await()
        var foodRef: DocumentSnapshot
        return try {
            Log.i("Room", "Creating new order in database")
            for(order in orderRef) {
                restaurantDB.orderDao().insert(Order(order.id, auth.currentUser!!.uid,
                    (order.data["order_date_time"] as Timestamp).toDate(),
                    (order.data["delivery_date_time"] as Timestamp).toDate(), order.data["total_price"].toString().toDouble()))
                for(product in orderProducts) {
                    foodRef = (product.data["food"] as DocumentReference).get().await()
                    restaurantDB.orderProductDao().insert(OrderProduct(product.id, order.id, foodRef.id.toInt(),
                        product.data["quantity"].toString().toInt(), product.data["price"].toString().toDouble()))
                    for(edit in (product.data["extra_ingredients"] as ArrayList<DocumentReference>)) {
                        restaurantDB.orderProductEditDao().insert(OrderProductEdit(product.id, edit.id.toInt(), IngredientQuantity.INGREDIENT_EXTRA))
                    }
                    for(edit in (product.data["removed_ingredients"] as ArrayList<DocumentReference>)) {
                        restaurantDB.orderProductEditDao().insert(OrderProductEdit(product.id, edit.id.toInt(), IngredientQuantity.INGREDIENT_REMOVED))
                    }
                }
            }
            Log.i("Room", "New order created successfully")
            true
        } catch(exception: Exception) {
            Log.e("Room", "Error while creating new order in database", exception)
            false
        }
    }
}