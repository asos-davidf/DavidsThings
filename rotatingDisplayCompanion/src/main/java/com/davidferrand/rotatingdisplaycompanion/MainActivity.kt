package com.davidferrand.rotatingdisplaycompanion

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener { uploadMessage(editText.text.toString()) }
    }

    private fun uploadMessage(message: String) {
        val database = FirebaseDatabase.getInstance()
        val messageRef = database.getReference("message")

        messageRef.setValue(message)
    }
}
