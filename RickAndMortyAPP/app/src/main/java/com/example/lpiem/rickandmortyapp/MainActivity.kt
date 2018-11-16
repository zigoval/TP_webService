package com.example.lpiem.rickandmortyapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

import org.json.JSONException

import java.util.Arrays

const val TAG = "TAG_Magic"
const val RC_SIGN_IN = 1


class MainActivity : AppCompatActivity() {

    private var callbackManager: CallbackManager? = null
    private lateinit var loginButton: LoginButton
    private var token: String? = null
    private lateinit var gso: GoogleSignInOptions
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private lateinit var signInButton: SignInButton
    private lateinit var userNameTV: TextView
    private lateinit var disconnectGoogleBtn: Button
    private var userNameGG: String? = null
    private var userNameFB: String? = null
    private var account: GoogleSignInAccount? = null
    private var displayIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        displayIntent = Intent(this, DisplayActivity::class.java)


        userNameTV = findViewById(R.id.userNameTV)
        userNameTV.visibility = View.INVISIBLE
        disconnectGoogleBtn = findViewById(R.id.disconnectGoogle)
        disconnectGoogleBtn.visibility = View.INVISIBLE
        disconnectGoogleBtn.setOnClickListener { disconnectGoogleAccount() }


        callbackManager = CallbackManager.Factory.create()

        Log.d(TAG, "onCreate: callBackManager = $callbackManager")

        loginButton = findViewById(R.id.login_button)
        loginButton.setReadPermissions("email")
        loginButton.setReadPermissions("public_profile")

        signInButton = findViewById(R.id.sign_in_button)
        signInButton.setSize(SignInButton.SIZE_STANDARD)


        // FACEBOOK
        loginButton.setOnClickListener {
            // Callback registration
            loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    token = loginResult.accessToken.token
                    Log.d(TAG, "onSuccess: token = $token")

                    val accessToken = AccessToken.getCurrentAccessToken()
                    val isLoggedIn = accessToken != null && !accessToken.isExpired

                    if (isLoggedIn) {
                        val request = GraphRequest.newMeRequest(
                                accessToken
                        ) { `object`, response ->
                            val result = response.jsonObject
                            try {
                                userNameFB = result.getString("name")
                                userNameTV.text = userNameFB
                                userNameTV.visibility = View.VISIBLE
                                Log.d(TAG, "onCompleted: name = $userNameFB")
                                Toast.makeText(applicationContext, "Bienvenue $userNameFB", Toast.LENGTH_SHORT).show()
                                startActivity(displayIntent)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                        val parameters = Bundle()
                        parameters.putString("fields", "id,name,link")
                        request.parameters = parameters
                        request.executeAsync()
                    }

                }

                override fun onCancel() {
                    Log.d(TAG, "onCancel: ")
                }

                override fun onError(exception: FacebookException) {
                    Toast.makeText(applicationContext, exception.toString(), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onError: " + exception.toString())
                }
            })
        }


        // GOOGLE
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"))

        gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        signInButton.setOnClickListener { signIn() }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
            signInButton.visibility = View.INVISIBLE
            disconnectGoogleBtn.visibility = View.VISIBLE
            val account = GoogleSignIn.getLastSignedInAccount(this)
            if (account != null) {
                userNameGG = account.displayName
                userNameTV.text = userNameGG
                userNameTV.visibility = View.VISIBLE
            } else {
                userNameTV.visibility = View.INVISIBLE
            }
            startActivity(displayIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.

        if (account != null) {
            userNameGG = account!!.displayName
            userNameTV.text = userNameGG
            userNameTV.visibility = View.VISIBLE
        } else {
            userNameTV.visibility = View.INVISIBLE
        }


    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)

    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "handleSignInResult: connected")
            // Signed in successfully, show authenticated UI.

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)

        }

    }

    private fun disconnectGoogleAccount() {
        mGoogleSignInClient!!.signOut().addOnCompleteListener { task ->
            task.result
            Log.d(TAG, "onComplete: disconnectGoogle")
            //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
            if (account != null) {
                userNameGG = account!!.displayName
                userNameTV.text = userNameGG
                userNameTV.visibility = View.VISIBLE
                userNameTV.setText(R.string.empty)
            } else {
                userNameTV.visibility = View.INVISIBLE

            }
        }
        signInButton.visibility = View.VISIBLE
        disconnectGoogleBtn.visibility = View.INVISIBLE
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }


}

