package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class FirebaseViewModel:ViewModel() {
    private val auth= FirebaseAuth.getInstance()
    fun getAuthState(): LiveData<Boolean> {
        return FirebaseLiveData(auth)
    }
}