package no.nav.brukerdialog.utils

import io.aiven.testcontainers.fakegcsserver.FakeGcsServerContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
object TestContainers {

    @Container
    @JvmField
    val FAKE_GCS_SERVER_CONTAINER = FakeGcsServerContainer()

    init {
        FAKE_GCS_SERVER_CONTAINER.start()
    }
}
