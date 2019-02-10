package com.example.lpiem.rickandmortyapp.View

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.lpiem.rickandmortyapp.Model.Card
import com.example.lpiem.rickandmortyapp.Presenter.OpenDeckManager
import com.example.lpiem.rickandmortyapp.R
import kotlinx.android.synthetic.main.activity_open_deck.*

class OpenDeckActivity : AppCompatActivity(), OpenDecksInterface {



    var openDeckManager: OpenDeckManager? = null
    var deckOpened: List<Card>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_deck)
        openDeckManager = OpenDeckManager.getInstance(this)
        iv_closeOpenDeck.setOnClickListener { finish() }

    }

    override fun onResume() {
        super.onResume()
        tv_openYourDeck.text = "You have ${openDeckManager!!.loginAppManager.connectedUser!!.deckToOpen} deck to open"
        iv_peaceAmongWorld.setOnClickListener { openDeckManager!!.openRandomDeck(openDeckManager!!.loginAppManager.connectedUser!!.deckToOpen, this) }
    }

    override fun showAnimation(show: Boolean) {
        if (show) {
            fl_DeckToOpen.visibility = View.GONE
            tv_openYourDeck.setOnClickListener { }
            fl_animation.visibility = View.VISIBLE
            av_from_code.setAnimation("portal_loop.json")
            av_from_code.playAnimation()
            av_from_code.loop(true)
        } else {
            av_from_code.pauseAnimation()
            fl_animation.visibility = View.GONE
            fl_DeckToOpen.visibility = View.VISIBLE
        }
    }

    override fun updateDecksCount(newCount: Int) {
        tv_openYourDeck.text = "You have $newCount deck to open"
    }

}
