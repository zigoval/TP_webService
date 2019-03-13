package com.example.lpiem.rickandmortyapp.Manager

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View.GONE
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.lpiem.rickandmortyapp.Data.RickAndMortyRetrofitSingleton
import com.example.lpiem.rickandmortyapp.Data.SUCCESS
import com.example.lpiem.rickandmortyapp.Model.ResponseFromApi
import com.example.lpiem.rickandmortyapp.R
import com.example.lpiem.rickandmortyapp.Util.SingletonHolder
import com.example.lpiem.rickandmortyapp.Util.observeOnce
import com.example.lpiem.rickandmortyapp.View.BottomActivity
import com.example.lpiem.rickandmortyapp.View.TAG

class SignInManager private constructor(private var context: Context) {

    private var loginAppManager: LoginAppManager = LoginAppManager.getInstance(context)
    private val rickAndMortyAPI = RickAndMortyRetrofitSingleton.getInstance(context)
    private var responseFromApiLiveData = MutableLiveData<ResponseFromApi>()
    var loaderLiveData = MutableLiveData<Int>()

    companion object : SingletonHolder<SignInManager, Context>(::SignInManager)

    fun cancelCall() {
        rickAndMortyAPI.cancelCall()
    }

    private fun regularConnection(email: String, password: String) {
        loaderLiveData.postValue(GONE)
        Toast.makeText(context, context.getString(R.string.account_created), Toast.LENGTH_SHORT).show()
        responseFromApiLiveData = rickAndMortyAPI.regularConnection(email, password)
        responseFromApiLiveData.observeOnce(Observer {
            connectionTreatment(it)
        })
    }

    fun signIn(userName: String, email: String, password: String) {
        responseFromApiLiveData = rickAndMortyAPI.signIn(userName, email, password)
        responseFromApiLiveData.observeOnce(Observer {
            signInTreatment(it, email, password)
        })
    }

    private fun connectionTreatment(response: ResponseFromApi) {
        val code = response.code
        val name = response.results?.userName
        val userId = response.results?.userId
        Log.d(TAG, "code = $code body = $response userId = $userId")
        Toast.makeText(context, String.format(context.getString(R.string.welcome), name), Toast.LENGTH_SHORT).show()
        loginAppManager.connectedUser = response.results!!
        val intent = Intent(context, BottomActivity::class.java)
        context.startActivity(intent)
    }

    private fun signInTreatment(response: ResponseFromApi, email: String, password: String) {
        loaderLiveData.postValue(GONE)
        val code = response.code
        val message = response.message
        if (code == SUCCESS) {
            regularConnection(email, password)
        } else {
            Toast.makeText(context, String.format(context.getString(R.string.code_message), code, message), Toast.LENGTH_SHORT).show()
        }
    }

}