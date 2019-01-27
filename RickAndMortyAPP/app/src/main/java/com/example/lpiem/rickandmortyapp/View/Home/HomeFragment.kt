package com.example.lpiem.rickandmortyapp.View.Home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.lpiem.rickandmortyapp.Model.User
import com.example.lpiem.rickandmortyapp.Presenter.HomeManager
import com.example.lpiem.rickandmortyapp.Presenter.LoginAppManager
import com.example.lpiem.rickandmortyapp.R
import com.example.lpiem.rickandmortyapp.View.Collection.CollectionFragment
import com.example.lpiem.rickandmortyapp.View.TAG
import kotlinx.android.synthetic.main.fragment_home.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : androidx.fragment.app.Fragment() {

    private var homeManager: HomeManager? = null
    private lateinit var loginAppManager: LoginAppManager
    private var user : User? = null


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        loginAppManager = LoginAppManager.getInstance(context!!)
        user = loginAppManager.connectedUser
        Log.d(TAG, "user : $user")

        homeManager = HomeManager.getInstance(context!!)
        homeManager?.getFragment(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        makeGameDisplayed(false)
        homeManager!!.getRandomQuote()

    }

    private fun isThereWinner(button: Button, solution: String) {
        if (button.text == solution ) {
            Toast.makeText(context, "Bonne réponse !!!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Mauvaise réponse ...", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                CollectionFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    internal fun updateUI(listResult: Triple<String, String, List<String>>) {
        val solution = listResult.second
        if (solution != "") {
            tv_citation.text = listResult.first
            var shuffleList = listOf(solution, listResult.third[0], listResult.third[1], listResult.third[2])
            shuffleList = shuffleList.shuffled()
            btn_perso1.text = shuffleList[0]
            btn_perso2.text = shuffleList[1]
            btn_perso3.text = shuffleList[2]
            btn_perso4.text = shuffleList[3]

            btn_perso1.setOnClickListener { isThereWinner(it as Button, solution) }
            btn_perso2.setOnClickListener { isThereWinner(it as Button, solution) }
            btn_perso3.setOnClickListener { isThereWinner(it as Button, solution) }
            btn_perso4.setOnClickListener { isThereWinner(it as Button, solution) }
            makeGameDisplayed(true)
        } else {
            Log.d(TAG, "listResult from API : $listResult")
        }
    }

    private fun makeGameDisplayed(display: Boolean) {
        val displayedItems = listOf(tv_citation, btn_perso1, btn_perso2, btn_perso3, btn_perso4)
        if (display) {
            for (item in displayedItems) {
                item.visibility = VISIBLE
            }
        } else {
            for (item in displayedItems) {
                item.visibility = GONE
            }
        }
    }

}

