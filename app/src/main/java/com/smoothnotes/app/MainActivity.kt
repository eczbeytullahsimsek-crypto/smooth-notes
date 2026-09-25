package com.smoothnotes.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Window

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE

        val drawingView = DrawingView(this)

        setContentView(drawingView)
    }
}
