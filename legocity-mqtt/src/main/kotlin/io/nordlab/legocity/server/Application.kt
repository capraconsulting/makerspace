package io.nordlab.legocity.server

import io.micronaut.runtime.Micronaut

/**
 * Main application object
 * Started by Docker container.
 */
object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("io.nordlab.legocity.server")
            .mainClass(Application.javaClass)
            .eagerInitSingletons(true)
            .banner(false)
            .start()
    }
}