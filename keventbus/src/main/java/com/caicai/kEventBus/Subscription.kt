package com.caicai.kEventBus

import java.lang.ref.WeakReference
import java.lang.reflect.Method

class Subscription(mSubscriber: Any?, mTargetMethod: TargetMethod) {

    var subscriber: WeakReference<Any?> = WeakReference<Any?>(mSubscriber)

    var method: Method = mTargetMethod.method

    val threadMode: ThreadMode = mTargetMethod.threadMode

    val eventType: EventType = mTargetMethod.eventType

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        other as Subscription

        if (subscriber.get() != other.subscriber.get()) return false
        if (method != other.method) return false
        if (threadMode != other.threadMode) return false
        if (eventType != other.eventType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = subscriber.get()?.hashCode() ?: 0
        result = 31 * result + method.hashCode()
        result = 31 * result + threadMode.hashCode()
        result = 31 * result + eventType.hashCode()
        return result
    }

    override fun toString(): String {
        return "Subscription(subscriber=$subscriber, method=$method, threadMode=$threadMode, eventType=$eventType)"
    }
}