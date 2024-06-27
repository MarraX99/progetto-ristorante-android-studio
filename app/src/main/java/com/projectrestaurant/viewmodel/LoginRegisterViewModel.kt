package com.projectrestaurant.viewmodel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.projectrestaurant.database.UserDao
import com.projectrestaurant.database.User
import kotlinx.coroutines.tasks.await

class LoginRegisterViewModel(): ViewModel() {
    private val MINIMUM_PASSWORD_LENGTH = 8
    private val MINIMUM_LOWER_CASE_COUNT = 2
    private val MINIMUM_UPPER_CASE_COUNT = 2
    private val MINIMUM_DIGIT_COUNT = 2
    private val MINIMUM_SPECIAL_COUNT = 1
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestoreDB: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var userDao: UserDao

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
            Log.i("FirebaseAuth", "Logged in as ${auth.currentUser?.uid}")
            true
        } catch(exception: Exception) {
            Log.e("FirebaseAuth", "Login failed", exception)
            false
        }
        return result
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
        return result
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
                val user = hashMapOf("name" to name, "surname" to surname, "email" to email, "accepted_privacy_policy" to acceptedPrivacyPolicy.toString())
                firestoreDB.collection("users").document(auth.currentUser!!.uid).set(user).await()
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
            userDao.insert(User(auth.currentUser!!.uid, name, surname, email, acceptedPrivacyPolicy))
            Log.i("RoomDatabase", "User ${auth.currentUser!!.uid} successfully added to database")
        }
        return result //result = true => all ok
    }

    fun setUserDao(dao: UserDao) { userDao = dao }
}