package com.caicai.kEventBus.handler

import android.os.Handler
import android.os.HandlerThread
import com.caicai.kEventBus.Subscription

class AsyncEventHandler : EventHandler {

    private val mDefaultEventHandler = DefaultEventHandler()

    private val mDispatchThread = DispatchThread(AsyncEventHandler::class.simpleName)

    init {
        mDispatchThread.start()
    }

    override fun handleEvent(subscription: Subscription, event: Any?) {
        mDispatchThread.post {
            mDefaultEventHandler.handleEvent(subscription, event)
        }
    }

    inner class DispatchThread(name: String?) : HandlerThread(name) {

        private var mAsyncHandler: Handler? = null

        override fun start() {
            super.start()
            mAsyncHandler = Handler(looper)
        }

        fun post(runnable: Runnable) {
            if (mAsyncHandler == null) {
                throw Exception("AsyncHandler is null,  must call start() first.")
            }
            mAsyncHandler?.post(runnable)
        }
    }
}