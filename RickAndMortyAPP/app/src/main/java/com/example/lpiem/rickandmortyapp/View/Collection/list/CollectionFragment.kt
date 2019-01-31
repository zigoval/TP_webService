package com.example.lpiem.rickandmortyapp.View.Collection.list

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lpiem.rickandmortyapp.Data.RickAndMortyAPI
import com.example.lpiem.rickandmortyapp.Data.RickAndMortyRetrofitSingleton
import com.example.lpiem.rickandmortyapp.Model.Character
import com.example.lpiem.rickandmortyapp.Model.ListOfCards
import com.example.lpiem.rickandmortyapp.Model.User
import com.example.lpiem.rickandmortyapp.Presenter.LoginAppManager
import com.example.lpiem.rickandmortyapp.Presenter.collection.CollectionManager
import com.example.lpiem.rickandmortyapp.R
import com.example.lpiem.rickandmortyapp.Util.RecyclerTouchListener
import com.example.lpiem.rickandmortyapp.View.Collection.detail.CollectionDetailActivity
import com.example.lpiem.rickandmortyapp.View.TAG
import kotlinx.android.synthetic.main.fragment_collection.*

private const val ARG_DATASET = "dataSet"
private const val ARG_PARAM2 = "param2"

class CollectionFragment : androidx.fragment.app.Fragment(), CardListDisplay {

    private var param1: Parcelable? = null
    private var param2: String? = null

    private var rickAndMortyAPI: RickAndMortyAPI? = null
    var listOfCards: ListOfCards? = null
    private lateinit var collectionManager: CollectionManager
    private lateinit var loginAppManager: LoginAppManager
    private var user : User? = null
    private var adapter: CollectionAdapter? = null

    companion object {
        @JvmStatic
        fun newInstance(dataset: Character, param2: String) =
                CollectionFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_DATASET, dataset)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getParcelable<Character>(ARG_DATASET)
            param2 = it.getString(ARG_PARAM2)
        }
        loginAppManager = LoginAppManager.getInstance(context!!)
        user = loginAppManager.connectedUser
        Log.d(TAG, "user : $user")

        rickAndMortyAPI = RickAndMortyRetrofitSingleton.instance
        collectionManager = CollectionManager.getInstance(context!!)
        collectionManager.captureFragmentInstance(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_collection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_collection.layoutManager = GridLayoutManager(context, 3)
        collectionManager.captureRecyclerView(rv_collection)
        collectionManager.getListOfDecks(user, this)
        rv_collection.addOnItemTouchListener(RecyclerTouchListener(context!!, rv_collection, object : RecyclerTouchListener.ClickListener {

            override fun onClick(view: View, position: Int) {
                //Toast.makeText(context, "click", Toast.LENGTH_SHORT).show()
                val detailIntent = Intent(context, CollectionDetailActivity::class.java)
                detailIntent.putExtra("current_card", (rv_collection.adapter as CollectionAdapter).getDataSet().cards?.get(position))
                context!!.startActivity(detailIntent)
            }

            override fun onLongClick(view: View, position: Int) {
                Toast.makeText(context, "long click", Toast.LENGTH_SHORT).show()
            }
        }))
    }

    private fun updateAdapter(list: ListOfCards) {
        if (adapter == null) {
            adapter = CollectionAdapter(list)
            rv_collection.adapter = adapter
            adapter!!.notifyDataSetChanged()
        } else {
            adapter!!.setDataSet(list)
            adapter!!.notifyDataSetChanged()
        }
    }

    override fun displayResult(list: ListOfCards) {
        updateAdapter(list)
    }

    override fun onDestroyView() {
        collectionManager.cancelCall()
        super.onDestroyView()
    }

}
