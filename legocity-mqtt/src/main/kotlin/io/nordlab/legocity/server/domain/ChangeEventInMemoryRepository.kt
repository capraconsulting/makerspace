package io.nordlab.legocity.server.domain

import io.micronaut.context.annotation.Primary
import io.nordlab.legocity.server.services.RandomStringService
import io.nordlab.legocity.server.services.RandomStringServiceException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.suspendCancellableCoroutine
import mu.KotlinLogging
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicLong

private const val MAX_SIZE = 10

private val logger = KotlinLogging.logger {}

/**
 * # Array backed in-memory repo.
 */
@Singleton
@Named("in-memory")
@Primary
open class ChangeEventInMemoryRepository(
    private val randomStringService: RandomStringService
) : ChangeEventRepository {

    /* Used as a version count to number the generated updates */
    private val counter = AtomicLong()

    /* hold last 100 generated random string in memory */
    private val buffer = ArrayBlockingQueue<ChangeEvent>(MAX_SIZE)

    override suspend fun all(): Flow<ChangeEvent> {
        logger.info("Retrieve all events in buffer: ${buffer.size}")
        return buffer.asFlow().take(10)
    }

    override suspend fun count(): Int {
        return buffer.size
    }

    override suspend fun clear() {
        buffer.clear()
    }

    /**
     * ## Create new ChangeEvent.
     * Will also broadcast the newly created event on Micronaut
     * internal eventbus to notify observers.
     */
    override suspend fun create(): ChangeEvent {
        val randomString = requestRandomString()
        val changeEvent = ChangeEvent(
            randomString,
            counter.incrementAndGet()
        )

        if (buffer.size == MAX_SIZE) {
            logger.info { "Remove old ChangeEvent from repo before adding new." }
            buffer.remove()
        }

        logger.info { "Create new ChangeEvent $changeEvent" }
        buffer.add(changeEvent)

        return changeEvent
    }

    /**
     * ## Request Random String.
     *
     * Named "Request" to fake the apperance of a
     * remote system that generate the random string.
     */
    private suspend fun requestRandomString(): String {
        val randomString = suspendCancellableCoroutine { cont ->
            // simulate 250ms response time to create new random string,
            // as if the change event was generated for example from
            // and external system.
            Thread.sleep(250)
            if (true) {
                cont.resume(randomStringService.randomString())
            } else {
                cont.resumeWithException(RandomStringServiceException())
            }
        }
        return randomString
    }
}