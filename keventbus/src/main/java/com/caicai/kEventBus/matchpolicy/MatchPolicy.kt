package com.caicai.kEventBus.matchpolicy

import com.caicai.kEventBus.EventType

interface MatchPolicy {
    fun findMatchEventTypes(type: EventType, aEvent: Any?): List<EventType>
}