package com.caicai.KEventBus

import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList

class SubscriberMethodHunter(subscriberMap: MutableMap<EventType, CopyOnWriteArrayList<Subscription>>?) {

    companion object {
        private val TAG = SubscriberMethodHunter::class.java.simpleName
    }
    private val mSubscriberMap = subscriberMap

    fun findSubscriberMethods(subscriber: Any?) {
        if (mSubscriberMap == null) {
            throw NullPointerException("Subscriber map is null")
        }

        var clazz = subscriber?.javaClass
        while (clazz != null && !isSystemClass(clazz.name)) {
            /**
             * getDeclaredMethods 是 Java 反射 API 中的一个方法
             * 这个方法用于获取一个类中声明的所有方法，包括公共、保护、默认（包）访问和私有方法，
             * 但不包括继承来的方法。
             */
            val declaredMethods = clazz.declaredMethods
            for (method in declaredMethods) {
                val annotation = method.getAnnotation(Subscriber::class.java)
                if (annotation != null) {
                    val parameterTypes = method.parameterTypes
                    if (parameterTypes.size == 1) {
                        val paramType = convertType(parameterTypes[0])
                        Log.i(TAG, "paramType: $paramType")
                        val eventType = EventType(paramType, annotation.tag)
                        val targetMethod = TargetMethod(method, eventType, annotation.threadMode)
                        subscribe(eventType, targetMethod, subscriber)
                    }
                }
            }

            // 从父类找
            clazz = clazz.superclass
        }
    }

    private fun subscribe(eventType: EventType, targetMethod: TargetMethod, subscriber: Any?) {
        var subscriptionList = mSubscriberMap?.get(eventType)
        if (subscriptionList == null) {
            subscriptionList = CopyOnWriteArrayList<Subscription>()
        }
        val subscription = Subscription(subscriber, targetMethod)
        if (subscriptionList.contains(subscription)) {
            // 防止重复注册
            return
        }

        subscriptionList.add(subscription)
//        Log.i(TAG, "subscription: $subscription")
        mSubscriberMap?.put(eventType, subscriptionList)
    }

    private fun isSystemClass(name: String): Boolean {
        return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")
    }

    fun removeMethodsFromMap(subscriber: Any?) {
        if (subscriber == null) return
        val iterator = mSubscriberMap?.values?.iterator()
        while (iterator?.hasNext() == true) {
            val curSubscriptionList = iterator.next()
            if (curSubscriptionList.isNotEmpty()) {
                val foundSubscriptionList = mutableListOf<Subscription>()
                val subIterator = curSubscriptionList.iterator()
                while (subIterator.hasNext()) {
                    val curSubscription = subIterator.next()
                    val curSubscriber = curSubscription.subscriber.get()
                    if (curSubscriber?.equals(subscriber) == true) {
                        foundSubscriptionList.add(curSubscription)
                    }
                }
                curSubscriptionList.removeAll(foundSubscriptionList.toSet())
            }

            if (curSubscriptionList.isEmpty()) {
                iterator.remove()
            }
        }
    }

    private fun convertType(eventType: Class<*>): Class<*> {
        var returnClass = eventType
        when (eventType) {
            Boolean::class.javaPrimitiveType -> {
                returnClass = java.lang.Boolean::class.java
            }
            Int::class.javaPrimitiveType -> {
                returnClass = java.lang.Integer::class.java
            }
            Float::class.javaPrimitiveType -> {
                returnClass = java.lang.Float::class.java
            }
            Double::class.javaPrimitiveType -> {
                returnClass = java.lang.Double::class.java
            }
        }

        return returnClass
    }
}