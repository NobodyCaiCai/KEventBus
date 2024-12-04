package com.caicai.kEventBus.handler

import com.caicai.kEventBus.Subscription

interface EventHandler {
    fun handleEvent(subscription: Subscription, event: Any?)
}