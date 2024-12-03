package com.caicai.KEventBus.matchpolicy

import com.caicai.KEventBus.EventType

interface MatchPolicy {
    fun findMatchEventTypes(type: EventType, aEvent: Any?): List<EventType>
}