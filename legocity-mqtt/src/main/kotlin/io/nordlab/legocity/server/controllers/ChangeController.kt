/*
 * Copyright (c)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nordlab.legocity.server.controllers

import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.Status
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.nordlab.legocity.server.domain.ChangeEvent
import io.nordlab.legocity.server.domain.ChangeEventRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.tags.Tags
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * # Controller that uses Flow the Coroutines integrations of Micronaut.
 *
 * ## How do we use coroutines in Micronaut?
 * Micronaut has support for suspending functions and flows. Micronaut is built using
 * the Reactor framework. However, an integration layer sits on top to support coroutines.
 *
 */
@Controller("/changes")
@Secured(SecurityRule.IS_ANONYMOUS)
@Tags(Tag(name = "Changes"))
class ChangeController(
    private val repo: ChangeEventRepository,
) {

    private val events = MutableSharedFlow<ChangeEvent>(
        10,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    /**
     * Listen for ApplicationEvents where new
     * ChangeEvents has been created.
     */
    @EventListener
    fun onNewChangeEvent(event: ChangeEvent) {
        logger.info { "Received new ChangeEvent: $event" }
        runBlocking {
            logger.info { "Emit new ChangeEvent to SharedFlow" }
            events.emit(event) // suspends until all subscribers receive it
        }
    }

    /**
     * ## Streaming Change Events.
     *
     * This endpoint will publish all received ChangeEvents
     * from the [events] shared flow.
     */
    @Operation(
        summary = "Stream change events",
        description = "This endpoint will publish all received ChangeEvents from the events shared flow",
    )
    @Get
    @Produces(MediaType.APPLICATION_JSON_STREAM)
    @Secured(SecurityRule.IS_ANONYMOUS)
    fun changeEventUpdates(): Flow<ChangeEvent> {
        logger.info { "Streaming ChangeEvents" }
        return events
    }

    /**
     * ## Return all Change Events from repository.
     *
     * ### Suspending Method Support Under the Hood in Micronaut.
     * An [interceptor in Micronaut](https://github.com/micronaut-projects/micronaut-core/blob/4.0.x/aop/src/main/java/io/micronaut/aop/internal/intercepted/KotlinInterceptedMethodImpl.java)
     * is defined that checks if your controller method is marked with the suspend keyword.
     *
     * Should use [coroutineScope](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope)
     * for the endpoint. But due to error uses runBlocking as a workaround until solution found.
     */
    @Operation(
        summary = "Return all change events",
        description = "Return all change events",
    )
    @Get
    @Produces(MediaType.APPLICATION_JSON)
    @Secured(SecurityRule.IS_ANONYMOUS)
    suspend fun changeEventsAll() = runBlocking {
        logger.info { "All ChangeEvents." }
        val a = async {
            logger.info { "Async 1 ChangeEvents." }
            repo.all().toCollection(mutableListOf())
        }

        a.await()
    }

    /**
     * ## Create and return a single change event.
     *
     * Cant get coroutineScope to work with async.
     * See issue https://github.com/micronaut-projects/micronaut-core/issues/8555
     */
    @Operation(
        summary = "Create and return a single change event",
        description = "Create and return a single change event",
    )
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Status(HttpStatus.ACCEPTED)
    suspend fun changeEventCreate() = coroutineScope {
        logger.info { "Create ChangeEvent." }
        val a = async {
            logger.info { "Async Create ChangeEvents." }
            repo.create()
        }

        logger.info { "Start await." }
        a.await()
    }
}