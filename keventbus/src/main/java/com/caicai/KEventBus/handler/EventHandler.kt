package com.caicai.KEventBus.handler

import com.caicai.KEventBus.Subscription

interface EventHandler {
    fun handleEvent(subscription: Subscription, event: Any?)
}