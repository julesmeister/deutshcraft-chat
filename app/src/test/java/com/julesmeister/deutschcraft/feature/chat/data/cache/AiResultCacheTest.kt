package com.julesmeister.deutschcraft.feature.chat.data.cache

import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test

class AiResultCacheTest {
    @Test
    fun cachesNullResultsAndEvictsLeastRecentlyUsedEntry() = runBlocking {
        val cache = AiResultCache<String, String?>(maximumEntries = 2)
        var calls = 0
        suspend fun load(key: String): String? = cache.getOrLoad(key) {
            calls++
            if (key == "correct") null else key
        }

        assertNull(load("correct"))
        assertNull(load("correct"))
        assertEquals("second", load("second"))
        assertNull(load("correct")) // Touch the first entry, making "second" oldest.
        assertEquals("third", load("third"))
        assertNull(load("correct"))
        assertEquals(3, calls)
        assertEquals("second", load("second"))
        assertEquals(4, calls)
    }

    @Test
    fun sharesIdenticalWorkWithoutBlockingOtherKeys() = runBlocking {
        val cache = AiResultCache<String, String>()
        val release = CompletableDeferred<Unit>()
        var calls = 0
        val first = async(start = CoroutineStart.UNDISPATCHED) {
            cache.getOrLoad("same") {
                calls++
                release.await()
                "result"
            }
        }
        val second = async(start = CoroutineStart.UNDISPATCHED) {
            cache.getOrLoad("same") {
                fail("An identical request must reuse the in-flight operation.")
                "unexpected"
            }
        }

        assertEquals("other", cache.getOrLoad("different") { "other" })
        assertFalse(first.isCompleted)
        release.complete(Unit)
        assertEquals("result", first.await())
        assertEquals("result", second.await())
        assertEquals(1, calls)
    }

    @Test
    fun neverCachesFailuresOrRejectedResults() = runBlocking {
        val cache = AiResultCache<String, String?>()
        val failure = IOException("offline")
        try {
            cache.getOrLoad("key") { throw failure }
            fail("The failure must propagate.")
        } catch (actual: IOException) {
            assertSame(failure, actual)
        }

        assertNull(cache.getOrLoad("key", cacheable = { it != null }) { null })
        assertEquals("valid", cache.getOrLoad("key") { "valid" })
        assertEquals("valid", cache.getOrLoad("key") { fail("Must be cached"); null })
    }

    @Test
    fun sharesFailureWithWaitersAndAllowsRetry() = runBlocking {
        val cache = AiResultCache<String, String>()
        val release = CompletableDeferred<Unit>()
        val failure = IOException("offline")
        val owner = async(start = CoroutineStart.UNDISPATCHED) {
            try {
                cache.getOrLoad("key") { release.await(); throw failure }
                fail("Must propagate the failure.")
                null
            } catch (actual: IOException) {
                actual
            }
        }
        val waiter = async(start = CoroutineStart.UNDISPATCHED) {
            try {
                cache.getOrLoad("key") { fail("Must share the request."); "" }
                fail("Must propagate the shared failure.")
                null
            } catch (actual: IOException) {
                actual
            }
        }
        release.complete(Unit)
        assertSame(failure, owner.await())
        assertEquals(failure.message, waiter.await()?.message)
        assertEquals("retry", cache.getOrLoad("key") { "retry" })
    }

    @Test
    fun cancellingWaiterDoesNotCancelOwner() = runBlocking {
        val cache = AiResultCache<String, String>()
        val release = CompletableDeferred<Unit>()
        val owner = async(start = CoroutineStart.UNDISPATCHED) {
            cache.getOrLoad("key") { release.await(); "result" }
        }
        val waiter = async(start = CoroutineStart.UNDISPATCHED) {
            cache.getOrLoad("key") { fail("Must share owner"); "" }
        }
        waiter.cancelAndJoin()
        assertFalse(owner.isCancelled)
        release.complete(Unit)
        assertEquals("result", owner.await())
        assertEquals("result", cache.getOrLoad("key") { fail("Must be cached"); "" })
    }

    @Test
    fun activeWaiterRetriesWhenOwnerIsCancelled() = runBlocking {
        val cache = AiResultCache<String, String>()
        val neverReleased = CompletableDeferred<Unit>()
        val owner = async(start = CoroutineStart.UNDISPATCHED) {
            cache.getOrLoad("key") { neverReleased.await(); "cancelled result" }
        }
        val waiter = async(start = CoroutineStart.UNDISPATCHED) {
            cache.getOrLoad("key") { "replacement" }
        }
        owner.cancelAndJoin()
        assertEquals("replacement", waiter.await())
        assertEquals("replacement", cache.getOrLoad("key") { fail("Must be cached"); "" })
    }
}
