package com.davidferrand.davidsthing

import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import java.util.*

private const val INITIAL_DELAY_MS = 1000L
private const val PERIOD_MS = 200L
private const val FINAL_SPACE = "    " // 4 spaces

class DisplayAnimator(private val display: AlphanumericDisplay) {
    private lateinit var rotatingString: String

    private var currentTimer: Timer? = null

//    private val thread: HandlerThread by lazy { HandlerThread("DisplayThread").apply { start() } }
//    private val handler: Handler by lazy { Handler(thread.looper) }

    fun display(string: String, times: Int = 1) {
        clear()

        displaying = true

        if (string.length <= 4) {
            // That's it, no need to animate
            display.display(string)
            return
        }

        display.display(string.substring(0, 4))
        rotatingString = (string + FINAL_SPACE).repeat(times)
        currentTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    displayNextPart()
                }
            }, INITIAL_DELAY_MS, PERIOD_MS)
        }
    }

    private fun displayNextPart() {
        rotatingString = (rotatingString /*+ rotatingString.first()*/).drop(1)
        if (rotatingString.isEmpty()) {
            clear()
            return
        }
        display.display(rotatingString/*.substring(0, 4)*/)
    }

    var displaying: Boolean = false
        private set

    fun clear() {
        currentTimer?.cancel()
        display.clear()

        displaying = false
    }
}