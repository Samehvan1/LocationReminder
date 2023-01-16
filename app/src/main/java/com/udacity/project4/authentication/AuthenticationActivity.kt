package com.udacity.project4.authentication

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var viewModel: FirebaseViewModel
    val SignInCode = 111
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        binding.lifecycleOwner = this;
        binding.btnLogin.setOnClickListener {
            launchSignIn()
        }
        viewModel= FirebaseViewModel()
        viewModel.getAuthState().observe(this, Observer{auth-> if (auth) doLogin()})
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==SignInCode&&resultCode== Activity.RESULT_OK){
           doLogin()
        }
    }
private fun doLogin(){
    startActivity(Intent(this,RemindersActivity::class.java))
    finish()
}
    private fun launchSignIn() {
        val signInMethods = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(signInMethods)
                .build(), SignInCode
        )
    }
}
