package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel>()

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()) {}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)


        login_btn.setOnClickListener {
            launchSignInFlow()
        }

        observeAuthenticationState()

    }

    private fun observeAuthenticationState() {
        viewModel.authenticationState.observe(this, Observer { authenticationState ->
            if (authenticationState == LoginViewModel.AuthenticationState.AUTHENTICATED) {
                //  Go to Reminder screen
                val intent = Intent(this, RemindersActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Log.d(TAG, "No User!")
            }
        })
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val signInIntent = AuthUI
            .getInstance()
            .createSignInIntentBuilder()
            .setIsSmartLockEnabled(false)
            .setAvailableProviders(providers)
            .build()

        signInLauncher.launch(signInIntent)
    }

    companion object {
        private var TAG = AuthenticationActivity::class.java.simpleName
    }

}
