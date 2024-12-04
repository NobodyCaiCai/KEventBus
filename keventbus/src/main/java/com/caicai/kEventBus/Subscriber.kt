package com.caicai.kEventBus

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Subscriber(val tag: String = EventType.DEFAULT_TAG, val threadMode: ThreadMode = ThreadMode.MAIN)
