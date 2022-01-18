package io.collective

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class SimpleAgedKache {
    var clock: Clock? = null
    var cache = arrayOfNulls<KacheEntry>(10)
    var idx = 0
    constructor(clock: Clock?) {
        this.clock = clock
    }

    constructor() {
        clock = object : Clock() {
            override fun getZone(): ZoneId {
                return systemDefaultZone().zone
            }

            override fun withZone(zone: ZoneId): Clock {
                return system(zone)
            }

            override fun instant(): Instant {
                return systemDefaultZone().instant()
            }
        }
    }

    fun put(key: Any?, value: Any?, retentionInMillis: Int) {
        val curr_time: Clock = object : Clock() {
            override fun getZone(): ZoneId {
                return systemDefaultZone().zone
            }

            override fun withZone(zone: ZoneId): Clock {
                return system(zone)
            }

            override fun instant(): Instant {
                return systemDefaultZone().instant()
            }
        }
        cache[idx++] = KacheEntry(key, value, retentionInMillis, curr_time)
    }

    fun isEmpty(): Boolean {
        return idx == 0
    }

    fun swap(swapIdx: Int) {
        for (i in idx - 1 downTo -1 + 1) {
            if (clock!!.millis() - cache[i]!!.insert_time.millis() >= cache[i]!!.retentionInMillis) {
                --idx
            } else {
                break
            }
        }
        cache[swapIdx] = cache[idx - 1]
        --idx
    }

    fun checkExpired() {
        for (i in 0..cache.size) {
            if (cache[i] == null) {
                return
            }
            if (i < idx) {
                if (clock!!.millis() - cache[i]!!.insert_time.millis() >= cache[i]!!.retentionInMillis) {
                    swap(i)
                }
            }
        }
    }

    fun size(): Int {
        checkExpired()
        return idx
    }

    fun get(key: Any?): Any? {
        checkExpired()
        if (isEmpty()) {
            return null
        }
        for (i in 0 until idx) {
            if (cache[i]!!.key === key) {
                return cache[i]!!.value
            }
        }
        return null
    }
}


class KacheEntry(var key: Any?, var value: Any?, var retentionInMillis: Int, var insert_time: Clock)