package com.caicai.KEventBus

import java.lang.reflect.Method

data class TargetMethod(val method: Method, val eventType: EventType, val threadMode: ThreadMode) {
    init {
        method.isAccessible = true
    }
}