package io.nordlab.legocity.server.system.mqtt

import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.annotation.v5.MqttPublisher
import io.nordlab.legocity.server.domain.ChangeEvent

@MqttPublisher
interface MqttPublisher {

    @Topic("legocity/server/randomstring")
    fun publishEvent(message: ChangeEvent)
}