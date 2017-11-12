package com.davidferrand.davidsthing

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import com.google.android.things.contrib.driver.ht16k33.Ht16k33
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.Normalizer


class MainActivity : Activity() {
    private lateinit var display: AlphanumericDisplay
    private lateinit var buttonA: Button
    private lateinit var buttonB: Button
    private lateinit var buttonC: Button

    private lateinit var displayAnimator: DisplayAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPeriphs()

        displayAnimator = DisplayAnimator(display)

//        displayMessage("Press a button")
//
//        playMessageOnButtonPress(buttonA, "Hello")
//        playMessageOnButtonPress(buttonB, "Hola")
//        playMessageOnButtonPress(buttonC, "Bonjour")

        val database = FirebaseDatabase.getInstance()
        val messageRef = database.getReference("message")

        messageRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue(String::class.java)
                displayMessage(value ?: "No message")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })
    }

    private fun playMessageOnButtonPress(button: Button, message: String) {
        button.setOnButtonEventListener { _, pressed -> if (pressed) displayMessage(message) }
    }

    private val TAG = MainActivity::class.java.simpleName

    private fun displayMessage(message: String) {
        val normalisedMsg = Normalizer.normalize(message, Normalizer.Form.NFD)
                .replace(Regex.fromLiteral("[^\\p{ASCII}]"), "")
                .toUpperCase()

        Log.v(TAG, "Message received: '$message'. Normalised: '$normalisedMsg'")

        try {
            displayAnimator.display(normalisedMsg)
        } catch (e: Exception) {
            displayMessage("Invalid message")
        }
    }

    override fun onDestroy() {
        destroyPeriphs()
        super.onDestroy()
    }

    private fun setupPeriphs() {
        display = RainbowHat.openDisplay()
        display.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX)
        display.setEnabled(true)

        buttonA = RainbowHat.openButtonA()
        buttonB = RainbowHat.openButtonB()
        buttonC = RainbowHat.openButtonC()
    }

    private fun destroyPeriphs() {
        display.close()
        buttonA.close()
        buttonB.close()
        buttonC.close()
    }

}

