package com.odtheking.odin.events.core

import com.odtheking.odin.utils.logError

interface Event {

    fun postAndCatch(): Boolean {
        runCatching {
            EventBus.post(this)
        }.onFailure {
            logError(it, this)
        }
        return false
    }
}