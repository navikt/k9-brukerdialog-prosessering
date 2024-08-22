package no.nav.brukerdialog.kafka.processors

import io.mockk.every
import no.nav.brukerdialog.config.RetryTemplateConfiguration
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.types.TopicEntry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.*

class ProcessTest {

    private companion object {
        private val logger = LoggerFactory.getLogger(ProcessTest::class.java)
        val retryTemplate = RetryTemplateConfiguration().retryTemplate()
    }

    @Test
    fun `Gitt process ikke feiler, forvent riktig returdata`() {
        val innkommendeData = "test-data"
        val utgåendeData = "processed-test-data"
        val topicEntry =
            TopicEntry(MetaInfo(version = 1, correlationId = UUID.randomUUID().toString()), innkommendeData)

        val resultat: TopicEntry<String> = process(KafkaStreamName.PSB_SØKNAD_PREPROSESSERING, topicEntry, retryTemplate, logger) { utgåendeData }

        assertEquals(topicEntry.metadata, resultat.metadata)
        assertEquals(utgåendeData, resultat.data)
    }

    @Test
    fun `Gitt process feiler, forvent 3 forsøk til returdata returneres`() {
        val innkommendeData = "test-data"
        val testDataMock = io.mockk.mockk<TestData>()
        val topicEntry =
            TopicEntry(MetaInfo(version = 1, correlationId = UUID.randomUUID().toString()), innkommendeData)

        val forventetReturData = "processed-test-data"
        every { testDataMock.data }
            .throws(IllegalStateException("Ukjent feil..."))
            .andThenThrows(IllegalStateException("Ukjent feil..."))
            .andThen(forventetReturData)

        val result: TopicEntry<String> = process(KafkaStreamName.PSB_SØKNAD_PREPROSESSERING, topicEntry, retryTemplate, logger) { testDataMock.data }

        assertEquals(topicEntry.metadata, result.metadata)
        assertEquals(forventetReturData, result.data)
    }
}

private data class TestData(val data: String)
