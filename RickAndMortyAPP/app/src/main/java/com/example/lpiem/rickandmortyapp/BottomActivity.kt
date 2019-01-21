package com.example.lpiem.rickandmortyapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_bottom.*

class BottomActivity : AppCompatActivity() {
    private var i =0
    private lateinit var characterList: List<Character>

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                val fragmentManager = supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                val fragment = HomeFragment.newInstance("coucou", "ca va ${i++}")
                fragmentTransaction.addToBackStack("stack")
                fragmentTransaction.replace(R.id.fragmentLayout, fragment)
                fragmentTransaction.commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_collection -> {
                message.setText(R.string.collection)
                val fragmentManager = supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                val fragment = CollectionFragment()
                fragmentTransaction.addToBackStack("stack")
                fragmentTransaction.replace(R.id.fragmentLayout, fragment)
                fragmentTransaction.commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_social -> {
                message.setText(R.string.social)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profil -> {
                message.setText(R.string.profilSettings)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }


}
