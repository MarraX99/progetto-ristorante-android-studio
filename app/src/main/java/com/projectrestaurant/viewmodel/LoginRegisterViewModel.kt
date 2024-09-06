package com.projectrestaurant.viewmodel

import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.projectrestaurant.database.Address
import com.projectrestaurant.database.RestaurantDB
import com.projectrestaurant.database.User
import kotlinx.coroutines.tasks.await

class LoginRegisterViewModel(private val application: Application): AndroidViewModel(application) {
    private val MINIMUM_PASSWORD_LENGTH = 8
    private val MINIMUM_LOWER_CASE_COUNT = 2
    private val MINIMUM_UPPER_CASE_COUNT = 2
    private val MINIMUM_DIGIT_COUNT = 2
    private val MINIMUM_SPECIAL_COUNT = 1
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestoreDB: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val restaurantDB: RestaurantDB = RestaurantDB.getInstance(application)

    fun validateEmail(email: String): HashMap<String,Boolean> {
        val checks = HashMap<String,Boolean>(2)
        checks["isBlank"] = email.isBlank()
        checks["isValid"] = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        return checks
    }

    fun validatePassword(password: String, passwordConfirm: String? = null): HashMap<String,Boolean> {
        val checks = HashMap<String,Boolean>(7)
        checks["isBlank"] = password.isBlank()
        checks["hasMinimumLength"] = password.length >= MINIMUM_PASSWORD_LENGTH
        checks["hasMinimumUpperCases"] = password.count(Char::isUpperCase) >= MINIMUM_UPPER_CASE_COUNT
        checks["hasMinimumLowerCases"] = password.count(Char::isLowerCase) >= MINIMUM_LOWER_CASE_COUNT
        checks["hasMinimumDigits"] = password.count(Char::isDigit) >= MINIMUM_DIGIT_COUNT
        checks["hasMinimumSpecialCharacters"] = password.count{it in "@#\$%?!€£*§~^&+=_.<>°-"} >= MINIMUM_SPECIAL_COUNT
        if(passwordConfirm != null) checks["equalsConfirm"] = password == passwordConfirm
        return checks
    }

    suspend fun logIn(email: String, password: String): Boolean {
        val result = try {
            auth.signInWithEmailAndPassword(email, password).await()
            Log.i("FirebaseAuth", "Authentication state changed to ${auth.currentUser!!.uid}")
            true
        } catch(exception: Exception) {
            Log.e("FirebaseAuth", "Login failed", exception)
            false
        }
        if(result) {
            if(!(restaurantDB.userDao().exists(auth.currentUser!!.uid))) {
                val user = firestoreDB.document("users/${auth.currentUser!!.uid}").get().await()
                addUserToDatabase(user)
                getDeliveryAddressesFromRemoteDatabase(user)
            }
        }
        return result //result = true => all ok
    }

    suspend fun resetPassword(email: String): Boolean {
        val result = try {
            auth.sendPasswordResetEmail(email).await()
            Log.i("FirebaseAuth", "Sent password reset email at $email")
            true
        } catch(exception: Exception) {
            Log.e("FirebaseAuth", "Failed to send password reset email to $email")
            false
        }
        return result //result = true => all ok
    }

    suspend fun register(name: String, surname: String, email: String, password: String, acceptedPrivacyPolicy: Boolean): Boolean {
        //creates a new firebase user
        var result = try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Log.i("FirebaseAuth", "User ${auth.currentUser?.uid} successfully created")
            true
        } catch(exception: Exception) {
            Log.e("FirebaseAuth", "Error while creating new user", exception)
            false
        }
        //if firebase user creation is successful creates a new user document in firestore
        if(result) {
            result = try {
                firestoreDB.document("users/${auth.currentUser!!.uid}")
                    .set(hashMapOf("name" to name, "surname" to surname, "email" to email,
                        "accepted_privacy_policy" to acceptedPrivacyPolicy,
                        "default_delivery_address" to null)).await()
                Log.i("FirebaseFirestore", "DocumentSnapshot successfully written")
                true
            } catch(exception: Exception) {
                Log.e("FirebaseFirestore", "Error while writing document", exception)
                auth.currentUser?.delete()?.await() //if document creation fails deletes the user from firebase
                false
            }
        }
        //if firestore document creation is successful adds the user to room database
        if(result) {
            restaurantDB.userDao().insert(User(auth.currentUser!!.uid, name, surname, email, acceptedPrivacyPolicy))
            Log.i("RoomDatabase", "User ${auth.currentUser!!.uid} successfully added to database")
        }
        return result //result = true => all ok
    }

    /* PRIVATE FUNCTIONS HERE */

    private suspend fun addUserToDatabase(user: DocumentSnapshot) {
        Log.i("RoomDatabase", "Adding data for user ${auth.currentUser!!.uid}...")
        restaurantDB.userDao().insert(User(auth.currentUser!!.uid, user.data!!["name"] as String, user.data!!["surname"] as String,
            user.data!!["email"] as String, user.data!!["accepted_privacy_policy"] as Boolean))
        Log.i("RoomDatabase", "User ${auth.currentUser!!.uid} added to database")
    }

    private suspend fun getDeliveryAddressesFromRemoteDatabase(user: DocumentSnapshot) {
        try {
            Log.i("FirebaseFirestore", "Retrieving address data for user ${auth.currentUser!!.uid}...")
            val addresses = firestoreDB.collection("users/${auth.currentUser!!.uid}/delivery_addresses").get().await()
            for(address in addresses) {
                restaurantDB.addressDao().insert(Address(address.id, auth.currentUser!!.uid, address.data["address"] as String,
                    address.data["cap"] as String, address.data["city"] as String, address.data["province"] as String))
                if(address.reference == (user.data!!["default_delivery_address"] as DocumentReference))
                    restaurantDB.addressDao().addDefaultAddress(auth.currentUser!!.uid, address.id)
            }
        } catch(exception: Exception) { Log.e("FirebaseFirestore", "Error while retrieving address data for user ${auth.currentUser!!.uid}", exception) }
    }
}