package io.nordlab.legocity.server.system

import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.kotest.assertions.until.fixed
import io.kotest.assertions.until.until
import io.kotest.core.spec.style.BehaviorSpec
import io.nordlab.legocity.server.domain.ChangeEvent
import io.nordlab.legocity.server.system.mqtt.MqttPublisher
import io.nordlab.legocity.server.system.mqtt.MqttSubscriber
import mu.KotlinLogging
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import jakarta.inject.Inject

private val logger = KotlinLogging.logger {}

/**
 * # MqttPublisherSpec
 *
 * Test the MqttPublisher and MqttSubscriber
 * to see if they can communicate with each other.
 *
 */
@MicronautTest
class MqttPublisherSpec(
    @Inject var mqttPublisher: MqttPublisher,
    @Inject var mqttSubscriber: MqttSubscriber,
) : BehaviorSpec({

    /**
     * Perform some testing here.
     */
    given("some test data") {
        val event = ChangeEvent("a test message with text", 1)

        `when`("publish something") {
            mqttPublisher.publishEvent(event)

            then("we should get a result") {
                until(5.seconds, 250.milliseconds.fixed()) {
                    mqttSubscriber.event!!.contains(event.payload)
                }
            }
        }
    }
})