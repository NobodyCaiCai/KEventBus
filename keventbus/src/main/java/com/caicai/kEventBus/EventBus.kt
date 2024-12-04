package com.caicai.kEventBus

import android.util.Log
import com.caicai.kEventBus.handler.AsyncEventHandler
import com.caicai.kEventBus.handler.DefaultEventHandler
import com.caicai.kEventBus.handler.EventHandler
import com.caicai.kEventBus.handler.UIThreadEventHandler
import com.caicai.kEventBus.matchpolicy.DefaultMatchPolicy

import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList

class EventBus private constructor(desc: String) {

    private val mDesc = desc

    /**
     * 事件队列, 每个线程都有自己的事件队列，保存了对应的EventType(事件的class对象，事件的tag)
     */
    private val mLocalEvents: ThreadLocal<Queue<EventType>> =
        object : ThreadLocal<Queue<EventType>>() {
            override fun initialValue(): Queue<EventType> {
                return ConcurrentLinkedQueue()
            }
        }

    private val eventDispatcher = EventDispatcher()

    private val mSubscriberMap = ConcurrentHashMap<EventType, CopyOnWriteArrayList<Subscription>>()

    private val mSubscriberMethodHunter: SubscriberMethodHunter =
        SubscriberMethodHunter(mSubscriberMap)

    private val mStickyEvents = CopyOnWriteArrayList<EventType>()

    constructor() : this(TAG)

    fun getDescriptor() = mDesc

    fun register(subscriber: Any?) {
        if (subscriber == null) {
            throw IllegalArgumentException("Subscriber must not be null")
        }

        synchronized(EventBus) {
            mSubscriberMethodHunter.findSubscriberMethods(subscriber)
        }
    }

    // 处理粘性事件，如果
    fun registerSticky(subscriber: Any?) {
        this.register(subscriber)
        eventDispatcher.dispatcherStickyEvents(subscriber)
    }

    fun unregister(subscriber: Any?) {
        if (subscriber == null) {
            throw IllegalArgumentException("Subscriber must not be null")
        }

        synchronized(EventBus) {
            mSubscriberMethodHunter.removeMethodsFromMap(subscriber)
        }
    }

    fun post(event: Any) {
        post(event, EventType.DEFAULT_TAG)
    }

    fun post(event: Any?, tag: String) {
        if (event == null) {
            throw IllegalArgumentException("Event must not be null")
        }

        mLocalEvents.get()?.offer(EventType(event.javaClass, tag))
        Log.i(TAG, "event: $event, event.javaClass: ${event.javaClass}")
        eventDispatcher.dispatcherEvents(event)
    }

    fun postSticky(event: Any?) {
        postSticky(event, EventType.DEFAULT_TAG)
    }

    fun postSticky(event: Any?, tag: String) {
        if (event == null) {
            return
        }

        val eventType = EventType(event.javaClass, tag, event)
        mStickyEvents.add(eventType)
    }

    fun removeStickyEvent(clazz: Class<*>) {
        removeStickyEvent(clazz, EventType.DEFAULT_TAG)
    }

    fun removeStickyEvent(clazz: Class<*>, tag: String): EventType? {
        val iterator = mStickyEvents.iterator()
        while (iterator.hasNext()) {
            val eventType = iterator.next()
            if (eventType.paramClass == clazz && eventType.tag == tag) {
                iterator.remove()
                return eventType
            }
        }
        return null
    }

    fun getStickyEvents() = mStickyEvents

    inner class EventDispatcher {

        private val mDefaultEventHandler = DefaultEventHandler()

        private val mUIThreadEventHandler = UIThreadEventHandler()

        private val mAsyncEventHandler = AsyncEventHandler()

        private val mCacheEventTypes: MutableMap<EventType, MutableList<EventType>> =
            ConcurrentHashMap()

        private val defaultMatchPolicy = DefaultMatchPolicy()

//        fun dispatcherEvents(aEvent: Any) {
//            val eventQueue: Queue<EventType>? = mLocalEvents.get()
//            if (eventQueue.isNullOrEmpty()) {
//                return
//            }
//
//            while (eventQueue.isNotEmpty()) {
//                eventQueue.poll()?.let {
//                    deliveryEvent(it, aEvent)
//                }
//            }
//        }

        fun dispatcherEvents(aEvent: Any) {
            val eventQueue: Queue<EventType>? = mLocalEvents.get()
            if (eventQueue.isNullOrEmpty()) {
                return
            }

            while (eventQueue.isNotEmpty()) {
                Log.i(TAG, "dispatcherEvents: $eventQueue")
                eventQueue.poll()?.let {
                    Log.i(TAG, "dispatcherEvents2: $it, ")
                    deliveryEvent(it, aEvent)
                }
            }
        }

        private fun deliveryEvent(eventType: EventType, aEvent: Any) {
            // 1. 先计算
            val eventTypeList = getMatchEventTypeList(eventType, aEvent)
            eventTypeList.forEach {
                handleEvent(it, aEvent)
            }
        }

        private fun handleEvent(eventType: EventType, aEvent: Any) {
            val subscriptions = mSubscriberMap[eventType]
            if (subscriptions.isNullOrEmpty()) {
                return
            }

            subscriptions.forEach { subscription ->
                val threadMode = subscription.threadMode
                val eventHandler = getEventHandler(threadMode)
                eventHandler.handleEvent(subscription, aEvent)
            }
        }

        private fun getEventHandler(threadMode: ThreadMode): EventHandler {
            return when (threadMode) {
                ThreadMode.MAIN -> mUIThreadEventHandler
                ThreadMode.POST -> mDefaultEventHandler
                ThreadMode.ASYNC -> mAsyncEventHandler
            }
        }

        private fun getMatchEventTypeList(eventType: EventType, aEvent: Any?): List<EventType> {
            var eventTypeList = mutableListOf<EventType>()
            // 1. 首先从缓存中取
            if (mCacheEventTypes.containsKey(eventType)) {
                val curEventTypeList = mCacheEventTypes[eventType]
                if (!curEventTypeList.isNullOrEmpty()) {
                    eventTypeList = curEventTypeList
                }
            } else {
                // 2. 缓存中没有，则先计算，先算再存
                eventTypeList = defaultMatchPolicy.findMatchEventTypes(eventType, aEvent)
                mCacheEventTypes[eventType] = eventTypeList
            }

            return eventTypeList
        }

        fun dispatcherStickyEvents(subscriber: Any?) {
            mStickyEvents.forEach { eventType ->
                deliveryStickyEvent(eventType, subscriber)
            }
        }

        private fun deliveryStickyEvent(eventType: EventType, subscriber: Any?) {
            val matchEventTypeList = getMatchEventTypeList(eventType, eventType.event)
            matchEventTypeList.forEach { foundEventType ->
                val subscriptions = mSubscriberMap[foundEventType]
                if (subscriptions.isNullOrEmpty()) {
                    return
                }

                subscriptions.filter {
                    isTarget(
                        it, subscriber
                    ) && (it.eventType == eventType || it.eventType.paramClass.isAssignableFrom(
                        eventType.paramClass
                    ))
                }.forEach {
                    val threadMode = it.threadMode
                    val eventHandler = getEventHandler(threadMode)
                    eventHandler.handleEvent(it, eventType.event)
                }
            }
        }

        private fun isTarget(subscription: Subscription, subscriber: Any?): Boolean {
            val curSubscriber = subscription.subscriber.get()
            return subscriber != null && (curSubscriber != null && curSubscriber == subscriber)
        }
    }

    companion object {
        val TAG: String = EventBus::class.java.simpleName + "K"

        @Volatile
        private var mDefaultBus: EventBus? = null

        fun getDefault(): EventBus {
            if (mDefaultBus == null) {
                synchronized(EventBus) {
                    if (mDefaultBus == null) {
                        mDefaultBus = EventBus()
                    }
                }
            }
            return mDefaultBus!!
        }
    }
}