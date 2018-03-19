package com.barger.emvviewer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_input.*

class InputActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        button.setOnClickListener {
            startActivity(MainActivity.createIntent(this, input.text.toString()))
        }
    }
}