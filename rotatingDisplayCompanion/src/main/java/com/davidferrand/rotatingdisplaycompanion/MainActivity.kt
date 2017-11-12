package com.davidferrand.rotatingdisplaycompanion

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener { uploadMessage(editText.text.toString()) }

        listenToImage()
    }

    private fun listenToImage() {
        database.getReference("image").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(String::class.java)
                displayImage(bitmapFromString(value))
            }

            override fun onCancelled(p0: DatabaseError?) {
            }
        })
    }

    private fun displayImage(value: Bitmap?) {
        image.setImageBitmap(value)
    }

    fun bitmapFromString(encodedString: String?): Bitmap? {
        return try {
            val encodeByte = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message
            null
        }

    }

    private fun uploadMessage(message: String) {
        val messageRef = database.getReference("message")
        messageRef.setValue(message)
    }
}
