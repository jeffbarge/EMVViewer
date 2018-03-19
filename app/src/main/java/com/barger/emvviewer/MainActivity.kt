package com.barger.emvviewer

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tags = EMVParser().parse(intent.getStringExtra(Intent.EXTRA_TEXT))
        val adapter = EMVAdapter()
        adapter.data = tags
        recycler_view.adapter = adapter
    }

    companion object {
        fun createIntent(context: Context, data: String): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(Intent.EXTRA_TEXT, data)
            return intent
        }
    }
}
