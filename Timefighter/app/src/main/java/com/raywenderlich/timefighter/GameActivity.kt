package com.raywenderlich.timefighter

import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class GameActivity : AppCompatActivity() {
    internal lateinit var gameScoreTextView: TextView
    internal lateinit var timeLeftTextView: TextView
    internal lateinit var tapMeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        // connect views to variables
    }

    private fun incrementScore() {
        // increment score logic
    }

    private fun resetGame() {
        // reset game logic
    }

    private fun startGame() {
        // start game logic
    }

    private fun endGame() {
        // end game logic
    }
}