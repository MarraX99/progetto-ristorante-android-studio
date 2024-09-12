package com.projectrestaurant.viewmodel

import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.projectrestaurant.database.Address
import com.projectrestaurant.database.RestaurantDB
import kotlinx.coroutines.tasks.await

class AccountViewModel(application: Application): AndroidViewModel(application) {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestoreDB by lazy { FirebaseFirestore.getInstance() }
    private val firestoreUser by lazy { firestoreDB.document("users/${auth.currentUser!!.uid}") }
    private val restaurantDB = RestaurantDB.getInstance(application)
    private val _name = MutableLiveData("")
    val name : LiveData<String> get() = _name
    private val _surname = MutableLiveData("")
    val surname : LiveData<String> get() = _surname
    private val _email = MutableLiveData("")
    val email : LiveData<String> get() = _email
    private val _deliveryAddress = MutableLiveData<String?>(null)
    val deliveryAddress : LiveData<String?> get() = _deliveryAddress

    fun validateEmail(email: String): HashMap<String,Boolean> {
        val checks = HashMap<String,Boolean>(2)
        checks["isBlank"] = email.isBlank()
        checks["isValid"] = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        return checks
    }

    fun logout() { auth.signOut() }

    suspend fun getUserData() {
        val user = restaurantDB.userDao().getUserById(auth.currentUser!!.uid)
        _name.postValue(user?.name ?: "")
        _surname.postValue(user?.surname ?: "")
        _email.postValue(user?.email ?: "")
        getDefaultDeliveryAddress(auth.currentUser!!.uid)
    }

    suspend fun changeUserNameAndSurname(newName: String, newSurname: String): Boolean {
        val user = firestoreUser.get().await()
        return try {
            if(!((user.data!!["name"] as String).equals(newName, false))) {
                Log.i("FirebaseFirestore", "Updating name for user ${auth.currentUser!!.uid}...")
                firestoreUser.set(hashMapOf("name" to newName), SetOptions.merge())
                restaurantDB.userDao().changeName(newName, auth.currentUser!!.uid)
                Log.i("FirebaseFirestore", "Update completed!")
            }
            Log.i("FirebaseFirestore", "Updating surname for user ${auth.currentUser!!.uid}...")
            if(!((user.data!!["surname"] as String).equals(newSurname, false))) {
                firestoreUser.set(hashMapOf("surname" to newSurname), SetOptions.merge())
                restaurantDB.userDao().changeSurname(newSurname, auth.currentUser!!.uid)
                Log.i("FirebaseFirestore", "Update completed!")
            }
            true
        } catch(exception: Exception) {
            Log.e("FirebaseFirestore", "Error while updating user ${auth.currentUser!!.uid} data", exception)
            false
        }
    }

    suspend fun changeUserEmail(newEmail: String, password: String): Boolean {
        val user = firestoreUser.get().await()
        var result: Boolean
        try {
            if(!((user.data!!["email"] as String).equals(newEmail, false))) {
                Log.i("FirebaseFirestore", "Updating e-mail for user ${auth.currentUser!!.uid}...")
                val credentials = EmailAuthProvider.getCredential(user.data!!["email"] as String, password)
                Log.i("FirebaseAuth", "Re-authenticating before changing e-mail...")
                auth.currentUser!!.reauthenticate(credentials).await()
                auth.currentUser!!.verifyBeforeUpdateEmail(newEmail).await()
                restaurantDB.userDao().changeEmail(newEmail, auth.currentUser!!.uid)
                Log.i("FirebaseFirestore", "Update completed!")
            }
            result = true
        } catch(exception: Exception) {
            Log.e("FirebaseFirestore", "Error while updating user ${auth.currentUser!!.uid} e-mail", exception)
            result = false
        }
        return result
    }

    suspend fun getDeliveryAddresses(): List<Address> {
        if(!(restaurantDB.addressDao().exists(auth.currentUser!!.uid))) getDeliveryAddressesFromRemoteDatabase()
        val list: List<Address> = try {
            Log.i("RoomDatabase", "Retrieving address data for user ${auth.currentUser!!.uid}")
            restaurantDB.addressDao().getAddressesByUserId(auth.currentUser!!.uid)
        } catch(exception: Exception) {
            Log.e("RoomDatabase", "Error while retrieving address data for user ${auth.currentUser!!.uid}", exception)
            emptyList()
        }
        return list
    }

    suspend fun getDefaultDeliveryAddress(userId: String) {
        val address = restaurantDB.addressDao().getDefaultDeliveryAddress(userId)
        _deliveryAddress.postValue(if(address != null) "${address.address} - ${address.cap} - ${address.city} - ${address.province}" else null)
    }

    suspend fun addDeliveryAddress(address: String, cap: String, city: String, province: String, isDefault: Boolean): Address? {
        val addressList = firestoreDB.collection("users/${auth.currentUser!!.uid}/delivery_addresses").get().await()
        val filteredList = addressList.documents.filter {(it.data!!["address"] as String) == address
                && (it.data!!["cap"] as String) == cap && (it.data!!["city"] as String) == city
                && (it.data!!["province"] as String) == province}
        lateinit var new: DocumentSnapshot
        val result = try {
            if(filteredList.isEmpty()) {
                Log.i("FirebaseFirestore", "Creating new delivery address for user ${auth.currentUser!!.uid}...")
                new = firestoreDB.collection("users/${auth.currentUser!!.uid}/delivery_addresses").document().get().await()
                val user = restaurantDB.userDao().getUserById(auth.currentUser!!.uid)
                firestoreDB.document("users/${auth.currentUser!!.uid}/delivery_addresses/${new.id}")
                    .set(hashMapOf("address" to address, "cap" to cap, "city" to city, "province" to province,
                        "name" to "${user!!.name} ${user.surname}"), SetOptions.merge()).await()
                if(isDefault) firestoreDB.document("users/${auth.currentUser!!.uid}")
                    .set(hashMapOf("default_delivery_address" to new.reference), SetOptions.merge()).await()
                Log.i("FirebaseFirestore", "New delivery address successfully created!")
                true
            } else false
        } catch(exception: Exception) {
            Log.e("FirebaseFirestore", "Error while creating new delivery address for user ${auth.currentUser!!.uid}", exception)
            false
        }
        if(result) {
            val newAddress = Address(new.id, auth.currentUser!!.uid, address, cap, city, province)
            Log.i("RoomDatabase", "Creating new delivery address for user ${auth.currentUser!!.uid}...")
            restaurantDB.addressDao().insert(newAddress)
            Log.i("RoomDatabase", "New delivery address successfully created!")
            if(isDefault) {
                restaurantDB.addressDao().setAsDefaultDeliveryAddress(auth.currentUser!!.uid, new.id)
                Log.i("RoomDatabase", "Address ${new.id} is now the default address!")
            }
            return newAddress
        } else return null
    }

    suspend fun setDefaultDeliveryAddress(addressId: String) {
        val addressReference = firestoreDB.document("users/${auth.currentUser!!.uid}/delivery_addresses/$addressId")
        val result = try {
            Log.i("FirebaseFirestore", "Updating default delivery address for user ${auth.currentUser!!.uid}...")
            firestoreDB.document("users/${auth.currentUser!!.uid}")
                .set(hashMapOf("default_delivery_address" to addressReference), SetOptions.merge()).await()
            true
        } catch(exception: Exception) {
            Log.e("FirebaseFirestore", "Error while updating default delivery address for user ${auth.currentUser!!.uid}", exception)
            false
        }
        if(result) {
            restaurantDB.addressDao().setAsDefaultDeliveryAddress(auth.currentUser!!.uid, addressId)
            Log.i("RoomDatabase", "Address $addressId is now the default address!")
        }
        getDefaultDeliveryAddress(auth.currentUser!!.uid)
    }

    /* PRIVATE FUNCTIONS HERE */

    private suspend fun getDeliveryAddressesFromRemoteDatabase() {
        try {
            Log.i("FirebaseFirestore", "Retrieving address data for user ${auth.currentUser!!.uid}...")
            val addresses = firestoreDB.collection("users/${auth.currentUser!!.uid}/delivery_addresses").get().await()
            val user = firestoreDB.document("users/${auth.currentUser!!.uid}").get().await()
            for(address in addresses) {
                if(address.reference == (user.data!!["default_delivery_address"] as DocumentReference)) {
                    restaurantDB.addressDao().insert(
                        Address(address.id, auth.currentUser!!.uid, address.data["address"] as String,
                        address.data["cap"] as String, address.data["city"] as String, address.data["province"] as String, true)
                    )
                } else {
                    restaurantDB.addressDao().insert(
                        Address(address.id, auth.currentUser!!.uid, address.data["address"] as String,
                        address.data["cap"] as String, address.data["city"] as String, address.data["province"] as String)
                    )
                }
            }
            Log.i("FirebaseFirestore", "Address data for user ${auth.currentUser!!.uid} successfully retrieved!")
        } catch(exception: Exception) { Log.e("FirebaseFirestore", "Error while retrieving address data for user ${auth.currentUser!!.uid}", exception) }
    }
}