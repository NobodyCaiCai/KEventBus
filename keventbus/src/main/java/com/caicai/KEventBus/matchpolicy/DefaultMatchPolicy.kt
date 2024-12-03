package com.caicai.KEventBus.matchpolicy

import com.caicai.KEventBus.EventType

class DefaultMatchPolicy : MatchPolicy {

    override fun findMatchEventTypes(type: EventType, aEvent: Any?): MutableList<EventType> {
        var clazz: Class<*>? = aEvent?.javaClass
        val result = mutableListOf<EventType>()
        while (clazz != null) {
            result.add(EventType(clazz, type.tag))
            addInterface(clazz, result, type.tag)
            clazz = clazz.superclass
        }
        return result
    }

    private fun addInterface(clazz: Class<*>?, result: MutableList<EventType>, tag: String) {
        clazz?.interfaces?.takeWhile { it != null }?.forEach {
            result.add(EventType(it, tag))
            addInterface(it, result, tag)
        }
    }
}