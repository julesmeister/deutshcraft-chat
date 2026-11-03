package com.julesmeister.deutschcraft.feature.chat.data.cache

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Process-local LRU for validated, context-independent AI results.
 * Identical concurrent requests share work; failures are never cached.
 */
class AiResultCache<K, V>(
    private val maximumEntries: Int = 64
) {
    init {
        require(maximumEntries > 0)
    }

    private val lock = Any()
    private val results = LinkedHashMap<K, V>(maximumEntries, 0.75f, true)
    private val inFlight = mutableMapOf<K, CompletableDeferred<V>>()

    suspend fun getOrLoad(
        key: K,
        cacheable: (V) -> Boolean = { true },
        load: suspend () -> V
    ): V {
        while (true) {
            currentCoroutineContext().ensureActive()
            var ownsRequest = false
            val pending = synchronized(lock) {
                // containsKey distinguishes a cached null (no correction) from a miss.
                if (results.containsKey(key)) {
                    return results.getValue(key)
                }
                inFlight[key] ?: CompletableDeferred<V>().also {
                    inFlight[key] = it
                    ownsRequest = true
                }
            }

            if (!ownsRequest) {
                try {
                    return pending.await()
                } catch (cancellation: CancellationException) {
                    // A cancelled owner must not cancel an independently active caller.
                    currentCoroutineContext().ensureActive()
                    continue
                }
            }

            try {
                val result = load()
                currentCoroutineContext().ensureActive()
                synchronized(lock) {
                    if (cacheable(result)) {
                        results[key] = result
                        if (results.size > maximumEntries) {
                            val oldest = results.entries.iterator()
                            oldest.next()
                            oldest.remove()
                        }
                    }
                    inFlight.remove(key)
                    pending.complete(result)
                }
                return result
            } catch (throwable: Throwable) {
                synchronized(lock) {
                    inFlight.remove(key)
                    pending.completeExceptionally(throwable)
                }
                throw throwable
            }
        }
    }
}
