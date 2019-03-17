package com.example.lpiem.rickandmortyapp.Manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.lpiem.rickandmortyapp.Data.*
import com.example.lpiem.rickandmortyapp.Data.JsonProperty.*
import com.example.lpiem.rickandmortyapp.Model.User
import com.example.lpiem.rickandmortyapp.Model.UserResponse
import com.example.lpiem.rickandmortyapp.R
import com.example.lpiem.rickandmortyapp.Util.SingletonHolder
import com.example.lpiem.rickandmortyapp.Util.observeOnce
import com.example.lpiem.rickandmortyapp.View.*
import com.facebook.AccessToken
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.gson.JsonObject

class LoginAppManager private constructor(private var context: Context) {

    private lateinit var preferencesHelper: PreferencesHelper
    private val rickAndMortyAPI = RickAndMortyRetrofitSingleton.getInstance(context)
    private var connectedToGoogle = false
    lateinit var gso: GoogleSignInOptions
    var mGoogleSignInClient: GoogleSignInClient? = null
    private var account: GoogleSignInAccount? = null
    var connectedUser: User? = null
    var gameInProgress = true
    var memoryInProgress = true
    internal var loginLiveData = MutableLiveData<UserResponse>()
    var loaderDisplay = MutableLiveData<Int>()
    var googleBtnSwitch = MutableLiveData<Boolean>()
    var resolveIntent = MutableLiveData<Intent>()
    var facebookInit = MutableLiveData<Unit>()
    var alreadyConnectedToFacebook = MutableLiveData<Boolean>()
    var finishActivityLiveData = MutableLiveData<Boolean>()

    companion object : SingletonHolder<LoginAppManager, Context>(::LoginAppManager)

    //REGULAR CONNECTION AND SIGN IN

    fun regularConnection(mail: String, pass: String) {
        if (mail == "" || pass == "") {
            Toast.makeText(context, context.getString(R.string.thanks_to_fill_all_fields), Toast.LENGTH_SHORT).show()
        } else {
            loaderDisplay.postValue(View.VISIBLE)
            val jsonBody = JsonObject()
            jsonBody.addProperty(JsonProperty.UserEmail.string, mail)
            jsonBody.addProperty(JsonProperty.UserPassword.string, pass)
            loginLiveData = rickAndMortyAPI.login(jsonBody)
            loginLiveData.observeOnce(Observer {
                loginTreatment(it, LoginFrom.FROM_LOGIN)
            })
        }
    }

    fun connectionWithToken(token: String, observer: Observer<UserResponse>) {
        Log.d(TAG, " log 2")
        val jsonBody = JsonObject()
        jsonBody.addProperty("session_token", token)
        loginLiveData = rickAndMortyAPI.loginWithToken(jsonBody)
        loginLiveData.observeOnce(observer)
    }

    fun regularSignIn() {
        val signInIntent = Intent(context, SignInActivity::class.java)
        resolveIntent.postValue(signInIntent)
    }

    fun openActivityLostPassword() {
        val lostPassIntent = Intent(context, LostPasswordActivity::class.java)
        resolveIntent.postValue(lostPassIntent)
    }

    // GOOGLE CONNECTION

    fun googleSetup() {
        googleBtnSwitch.postValue(true)
        gso = instanciateGSO()
    }

    fun instanciateGSO(): GoogleSignInOptions {
        gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .build()
        return gso
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            account = completedTask.getResult(ApiException::class.java)
            connectedToGoogle = true
            Log.d(TAG, "handleGoogleSignInResult: connected")

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            loaderDisplay.postValue(View.GONE)
            Toast.makeText(context, "Une erreur est arrivée lors de la connection, merci de réessayer plus tard.", Toast.LENGTH_SHORT).show()
        }

    }

    fun onGoogleConnectionResult(data: Intent?) {
        // The Task returned from this call is always completed, no need to attach
        // a listener.
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        handleGoogleSignInResult(task)
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            val userName = account.displayName
            val userEmail = account.email
            val userId = account.id
            val userImage = account.photoUrl
            val jsonBody = JsonObject()
            jsonBody.addProperty(UserEmail.string, userEmail)
            jsonBody.addProperty(UserName.string, userName)
            jsonBody.addProperty(UserPassword.string, userId)
            jsonBody.addProperty(UserImage.string, userImage.toString())
            jsonBody.addProperty(ExternalID.string, userId)

            loginLiveData = rickAndMortyAPI.login(jsonBody)
            loginLiveData.observeOnce(Observer {
                loginTreatment(it, LoginFrom.FROM_LOGIN)
            })

        } else {
            Toast.makeText(context, "erreur : ${data.toString()}", Toast.LENGTH_SHORT).show()
            loaderDisplay.postValue(View.GONE)
        }
    }

    fun disconnectGoogleAccount(verbose: Boolean) {
        mGoogleSignInClient?.signOut()?.addOnCompleteListener { task ->
            task.result
            if (verbose) Toast.makeText(context, context.getString(R.string.google_disconnected), Toast.LENGTH_SHORT).show()
            googleBtnSwitch.postValue(true)
            googleBtnSwitch.postValue(true)
            connectedToGoogle = false
        }
    }

    // FACEBOOK CONNECTION

    fun facebookSetup() {
        facebookInit.postValue(Unit)

        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        Log.d(TAG, "isLoggedIn = $isLoggedIn")
        alreadyConnectedToFacebook.postValue(isLoggedIn)

    }

    fun facebookSuccess(loginResult: LoginResult) {
        loaderDisplay.postValue(View.VISIBLE)
        val accessToken = loginResult.accessToken
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        Log.d(TAG, "isLoggedIn on success = $isLoggedIn")

        if (isLoggedIn) {
            val request = GraphRequest.newMeRequest(
                    accessToken
            ) { obj, response ->
                val result = response.jsonObject
                Log.d(TAG, "request Body: $result")
                try {
                    val userNameFB = result.getString("name")
                    val userEmail = result.getString("email")
                    val userId = result.getString("id")
                    val image = "https://graph.facebook.com/$userId/picture?type=large"
                    val jsonBody = JsonObject()

                    jsonBody.addProperty(UserEmail.string, userEmail)
                    jsonBody.addProperty(UserName.string, userNameFB)
                    jsonBody.addProperty(UserPassword.string, userId)
                    jsonBody.addProperty(UserImage.string, image)
                    jsonBody.addProperty(ExternalID.string, userId)

                    loginLiveData = rickAndMortyAPI.login(jsonBody)
                    loginLiveData.observeOnce(Observer {
                        loginTreatment(it, LoginFrom.FROM_LOGIN)
                    })

                } catch (e: Throwable) {
                    Log.d(TAG, "error : $e")
                    e.printStackTrace()
                    Toast.makeText(context, context.getString(R.string.no_access_to_DB), Toast.LENGTH_SHORT).show()
                    LoginManager.getInstance().logOut()
                    loaderDisplay.postValue(View.GONE)
                }
            }
            val parameters = Bundle()
            parameters.putString("fields", "id,name,email,picture")
            request.parameters = parameters
            request.executeAsync()
        }
    }

    fun facebookCancel() {
        Log.d(TAG, "onCancel: ")
        //TODO : remove this line when app is finished
        LoginManager.getInstance().logOut()
        loaderDisplay.postValue(View.GONE)
    }

    fun facebookError(exception: FacebookException) {
        Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onError: $exception")
        loaderDisplay.postValue(View.GONE)
    }

    internal fun loginTreatment(userResponse: UserResponse, from: LoginFrom) {
        Log.d(TAG, " log 4 User = ${userResponse.user}")
        preferencesHelper = PreferencesHelper(context)
        val code = userResponse.code
        val message = userResponse.message
        when (code) {
            SUCCESS -> {
                val results = userResponse.user
                loaderDisplay.postValue(View.GONE)
                if (connectedToGoogle) {
                    googleBtnSwitch.postValue(false)
                }
                val name = results?.userName
                Log.d(TAG, "code = $code body = $userResponse")
                connectedUser = userResponse.user!!
                val homeIntent = Intent(context, BottomActivity::class.java)
                val loginIntent = Intent(context, LoginActivity::class.java)
                val token = userResponse.user?.sessionToken
                when (from) {
                    LoginFrom.FROM_LOGIN -> {
                        if (token != null && token.length == 30) {
                            preferencesHelper.deviceToken = token
                        }
                        Toast.makeText(context, String.format(context.getString(R.string.welcome, name)), Toast.LENGTH_SHORT).show()
                        resolveIntent.postValue(homeIntent)
                        finishActivityLiveData.postValue(true)
                    }
                    else -> {
                        if (token != null && token.length == 30) {
                            Toast.makeText(context, String.format(context.getString(R.string.welcome, name)), Toast.LENGTH_SHORT).show()
                            (context as SplashScreen).startActivity(homeIntent)
                        } else {
                            Toast.makeText(context, "Session expirée, merci de vous reconnecter", Toast.LENGTH_SHORT).show()
                            (context as SplashScreen).startActivity(loginIntent)
                        }
                    }
                }
            }
            else -> {
                Toast.makeText(context, String.format(context.getString(R.string.code_message), code, message), Toast.LENGTH_SHORT).show()
                loaderDisplay.postValue(View.GONE)
            }
        }
    }

}