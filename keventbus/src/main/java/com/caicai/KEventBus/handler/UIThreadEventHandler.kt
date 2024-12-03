package com.caicai.KEventBus.handler

import android.os.Handler
import android.os.Looper
import com.caicai.KEventBus.Subscription

class UIThreadEventHandler: EventHandler {

    private val mUiHandler = Handler(Looper.getMainLooper())

    private val mDefaultEventHandler = DefaultEventHandler()

    override fun handleEvent(subscription: Subscription, event: Any?) {
        mUiHandler.post {
            mDefaultEventHandler.handleEvent(subscription, event)
        }
    }
}