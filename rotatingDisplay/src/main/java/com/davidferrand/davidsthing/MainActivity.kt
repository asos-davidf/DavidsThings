package com.davidferrand.davidsthing

import android.app.Activity
import android.os.Bundle
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import com.google.android.things.contrib.driver.ht16k33.Ht16k33
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat

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

        displayMessage("Press a button")

        playMessageOnButtonPress(buttonA, "Hello")
        playMessageOnButtonPress(buttonB, "Hola")
        playMessageOnButtonPress(buttonC, "Bonjour")
    }

    private fun playMessageOnButtonPress(button: Button, message: String) {
        button.setOnButtonEventListener { _, pressed -> if (pressed) displayMessage(message) }
    }

    private fun displayMessage(message: String) {
        displayAnimator.display(message.toUpperCase())
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

