package com.davidferrand.davidsthing

import android.app.Activity
import android.graphics.Bitmap
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Base64
import android.util.Log
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay
import com.google.android.things.contrib.driver.ht16k33.Ht16k33
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream
import java.text.Normalizer


class MainActivity : Activity(), ImageReader.OnImageAvailableListener {
    private lateinit var display: AlphanumericDisplay
    private lateinit var buttonA: Button
    private lateinit var buttonB: Button
    private lateinit var buttonC: Button

    private lateinit var displayAnimator: DisplayAnimator

    private lateinit var mImagePreprocessor: ImagePreprocessor
    private var mCameraHandler: CameraHandler? = null

    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null

    private val TAG = MainActivity::class.java.simpleName

    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private var currentMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPeriphs()
        initCamera()

        displayAnimator = DisplayAnimator(display)

        buttonA.setOnButtonEventListener { _, pressed -> if (pressed) showCurrentMessage(3) }
        buttonB.setOnButtonEventListener { _, pressed -> if (pressed) displayAnimator.clear() }

//        saveMessage("Press a button")
//
//        playMessageOnButtonPress(buttonA, "Hello")
//        playMessageOnButtonPress(buttonB, "Hola")
//        playMessageOnButtonPress(buttonC, "Bonjour")

        val messageRef = database.getReference("message")

        messageRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue(String::class.java)
                saveMessage(value ?: "No message")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })
    }

    private fun showCurrentMessage(times: Int) {
        currentMessage?.let {
            try {
                displayAnimator.display(it, times)
            } catch (e: Exception) {
                displayAnimator.display("Invalid message")
            }
        }
    }

    private fun playMessageOnButtonPress(button: Button, message: String) {
        button.setOnButtonEventListener { _, pressed -> if (pressed) saveMessage(message) }
    }

    private fun saveMessage(message: String) {
        val normalisedMsg = Normalizer.normalize(message, Normalizer.Form.NFD)
                .replace(Regex.fromLiteral("[^\\p{ASCII}]"), "")
                .toUpperCase()

        Log.v(TAG, "Message received: '$message'. Normalised: '$normalisedMsg'")

        if (normalisedMsg.equals("camera", ignoreCase = true)) {
            takePictureAndUpload()
        } else {
            currentMessage = normalisedMsg
            showCurrentMessage(3)
        }
    }

    private fun takePictureAndUpload() {
        mBackgroundHandler?.post(mBackgroundClickHandler)
    }

    private fun initCamera() {
        mBackgroundThread = HandlerThread("BackgroundThread").apply { start() }
        mBackgroundHandler = Handler(mBackgroundThread!!.looper).apply { post(mInitializeOnBackground) }
    }

    private val mInitializeOnBackground = Runnable {
        mImagePreprocessor = ImagePreprocessor()

        mCameraHandler = CameraHandler.getInstance()
        mCameraHandler!!.initializeCamera(
                this, mBackgroundHandler,
                this)
    }

    private val mBackgroundClickHandler = Runnable { mCameraHandler!!.takePicture() }

    override fun onImageAvailable(reader: ImageReader) {
        var bitmap: Bitmap? = null
        reader.acquireNextImage().use { image -> bitmap = mImagePreprocessor.preprocessImage(image) }

        bitmap?.let { database.getReference("image").setValue(bitmapToString(it)) }
    }

    private fun bitmapToString(bitmap: Bitmap): String {
        val ByteStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, ByteStream)
        val b = ByteStream.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    override fun onDestroy() {
        destroyPeriphs()
        destroyCamera()
        super.onDestroy()
    }

    private fun destroyCamera() {
        try {
            mBackgroundThread?.quit()
        } catch (t: Throwable) {
            // close quietly
        }

        mBackgroundThread = null
        mBackgroundHandler = null

        try {
            mCameraHandler?.shutDown()
        } catch (t: Throwable) {
            // close quietly
        }
    }

    private fun setupPeriphs() {
        display = RainbowHat.openDisplay()
        display.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX)
        display.setEnabled(true)

        buttonA = RainbowHat.openButtonA()
        buttonB = RainbowHat.openButtonB()
        buttonC = RainbowHat.openButtonC()

        RainbowHat.openLedBlue().apply {
            value = false
            close()
        }
        RainbowHat.openLedRed().apply {
            value = false
            close()
        }
        RainbowHat.openLedGreen().apply {
            value = false
            close()
        }
    }

    private fun destroyPeriphs() {
        display.close()
        buttonA.close()
        buttonB.close()
        buttonC.close()
    }

}

