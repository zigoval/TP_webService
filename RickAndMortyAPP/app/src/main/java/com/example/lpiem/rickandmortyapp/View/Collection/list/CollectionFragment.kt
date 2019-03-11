package com.example.lpiem.rickandmortyapp.View.Collection.list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lpiem.rickandmortyapp.Manager.LoginAppManager
import com.example.lpiem.rickandmortyapp.Manager.collection.CollectionManager
import com.example.lpiem.rickandmortyapp.Model.ListOfCards
import com.example.lpiem.rickandmortyapp.Model.User
import com.example.lpiem.rickandmortyapp.R
import com.example.lpiem.rickandmortyapp.Util.RecyclerTouchListener
import com.example.lpiem.rickandmortyapp.View.Collection.detail.CollectionDetailActivity
import com.example.lpiem.rickandmortyapp.View.TAG
import kotlinx.android.synthetic.main.fragment_collection.*
import kotlinx.android.synthetic.main.price_input.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CollectionFragment : androidx.fragment.app.Fragment(), CardListDisplay {

    private var param1: String? = null
    private var param2: String? = null

    var listOfCards: ListOfCards? = null
    private lateinit var collectionManager: CollectionManager
    private lateinit var loginAppManager: LoginAppManager
    private var user : User? = null
    private var adapter: CollectionAdapter? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        loginAppManager = LoginAppManager.getInstance(context!!)
        user = loginAppManager.connectedUser
        Log.d(TAG, "user : $user")

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
        rv_collection.addOnItemTouchListener(RecyclerTouchListener(context!!, rv_collection, object : RecyclerTouchListener.ClickListener {

            override fun onClick(view: View, position: Int) {
                val detailIntent = Intent(context, CollectionDetailActivity::class.java)
                detailIntent.putExtra("current_card", (rv_collection.adapter as CollectionAdapter).getDataSet().cards?.get(position))
                context!!.startActivity(detailIntent)
            }

            override fun onLongClick(view: View, position: Int) {
                val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)
                val card = (rv_collection.adapter as CollectionAdapter).getDataSet().cards?.get(position)
                val customview = layoutInflater.inflate(R.layout.price_input, null, false)
                builder.setView(customview)
                builder.setTitle(getString(R.string.sellThisCard))
                        .setMessage(getString(R.string.which_price_for_this_card))
                        .setPositiveButton(android.R.string.yes) { dialog, which ->
                            val price = customview.et_price.text
                            var isValid = true
                            if(price.isBlank()){
                                isValid = false
                            }
                            if(isValid){
                                collectionManager.sellACard(user!!.userId!!, card!!, price.toString().toInt())
                            }
                            if(isValid){
                                dialog.dismiss()
                            }
                        }
                        .setNegativeButton(android.R.string.no) { dialog, which ->
                            // do nothing
                        }
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
            }

        }))
    }

    private fun updateAdapter(list: ListOfCards) {
        if (adapter == null) {
            adapter = CollectionAdapter(list)
            rv_collection.adapter = adapter
            adapter!!.updateList(list)
        } else {
            adapter!!.updateList(list)
        }
    }

    override fun displayResult(list: ListOfCards) {
        if (adapter == null) {
            collection_loader.visibility = GONE
            rv_collection.visibility = VISIBLE
        }
        updateAdapter(list)
    }

    override fun onDestroyView() {
        collectionManager.cancelCall()
        super.onDestroyView()
    }

    override fun onResume() {
        if (adapter == null) {
            rv_collection.visibility = GONE
            collection_loader.visibility = VISIBLE
        }
        collectionManager.getListOfDecks(user, this)
        super.onResume()
    }
}
