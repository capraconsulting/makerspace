package io.nordlab.legocity.server.system.mqtt

import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@MqttSubscriber
class MqttSubscriber {
    var event: String? = null

    /**
     * Listen for all messages on the MQTT broker.
     */
    @Topic("#")
    fun receive(data: String) {
        logger.info { "Received message: $data" }
        event = data
    }
}