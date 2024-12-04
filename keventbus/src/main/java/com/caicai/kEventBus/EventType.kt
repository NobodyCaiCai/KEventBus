package com.caicai.kEventBus

data class EventType(
    val paramClass: Class<*>, val tag: String = DEFAULT_TAG, val event: Any? = null
) {
    constructor(aClass: Class<*>) : this(aClass, DEFAULT_TAG)

    companion object {
        val TAG = EventType::class.simpleName
        const val DEFAULT_TAG: String = "default_tag"
    }
}

