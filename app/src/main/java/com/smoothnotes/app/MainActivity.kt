package com.smoothnotes.app

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = "Smooth Notes"
            textSize = 28f
            gravity = Gravity.CENTER
        }

        setContentView(textView)
    }
}
