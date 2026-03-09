package no.nav.brukerdialog.utils

import io.aiven.testcontainers.fakegcsserver.FakeGcsServerContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer

@Testcontainers
object TestContainers {

    @Container
    @JvmField
    val FAKE_GCS_SERVER_CONTAINER = FakeGcsServerContainer()

    val KAFKA_CONTAINER = KafkaContainer("apache/kafka:3.8.1").apply { start() }

    init {
        FAKE_GCS_SERVER_CONTAINER.start()
    }
}
