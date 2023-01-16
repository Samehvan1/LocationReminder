package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth

class FirebaseLiveData(private val auth:FirebaseAuth):LiveData<Boolean>(),FirebaseAuth.AuthStateListener {
    override fun onAuthStateChanged(p0: FirebaseAuth) {
        value=p0.currentUser!=null
    }

    override fun onActive() {
        super.onActive()
        auth.addAuthStateListener(this)
    }

    override fun onInactive() {
        super.onInactive()
        auth.removeAuthStateListener(this)
    }

}