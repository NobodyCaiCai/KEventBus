package com.caicai.kEventBus_demo

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity

import com.caicai.KEventBus.EventBus
import com.caicai.KEventBus.Subscriber
import com.caicai.kEventBus_demo.bean.StickyUser

class StickyActivity : ComponentActivity() {

    companion object {
        private val TAG = StickyActivity::class.java.simpleName
    }

    private var nameTv: TextView? = null

    private var ageTv: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sticky_activity)
        nameTv = findViewById(R.id.name_tv)
        ageTv = findViewById(R.id.age_tv)
        EventBus.getDefault().registerSticky(this)
        Log.i(TAG, "onCreate")
    }

    @Subscriber
    private fun onReceiveStickyEvent(info: StickyUser) {
        nameTv?.text = info.name
        ageTv?.text = getString(R.string.age_text)
        EventBus.getDefault().removeStickyEvent(info.javaClass)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}