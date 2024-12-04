package com.caicai.kEventBus.handler

import android.util.Log
import com.caicai.kEventBus.Subscription

class DefaultEventHandler : EventHandler {
    override fun handleEvent(subscription: Subscription, event: Any?) {
        if (subscription.subscriber.get() == null) {
            return
        }

        try {
            subscription.method.invoke(subscription.subscriber.get(), event)
        } catch (e: Exception) {
            Log.i("DefaultEventHandler", "handleEvent: " + e.message)
        }
    }
}