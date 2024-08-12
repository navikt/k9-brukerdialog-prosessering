package no.nav.brukerdialog.kafka.processors

import no.nav.brukerdialog.common.Constants
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.kafka.types.TopicEntry
import org.apache.kafka.streams.processor.api.ContextualProcessor
import org.apache.kafka.streams.processor.api.Record
import org.apache.kafka.streams.processor.internals.ProcessorContextImpl
import org.slf4j.LoggerFactory
import org.slf4j.MDC


class LoggingToMDCProcessor<T> : ContextualProcessor<String, TopicEntry<T>, String, TopicEntry<T>>() {
    private companion object {
        private val logger = LoggerFactory.getLogger(LoggingToMDCProcessor::class.java)
    }

    override fun process(record: Record<String, TopicEntry<T>>) {
        val søknadId = record.key()
        val topicEntry = record.value()
        val recordContext = (this.context() as ProcessorContextImpl).recordContext()
        val topic = recordContext.topic()
        val partition = recordContext.partition()
        val offset = recordContext.offset()
        val ytelse = Ytelse.fraTopic(topic)

        MDC.put(Constants.SOKNAD_ID_KEY, søknadId)
        MDC.put(Constants.CORRELATION_ID_KEY, topicEntry.metadata.correlationId)
        MDC.put(Constants.YTELSE, ytelse.name)
        MDC.put(Constants.TOPIC, topic)
        MDC.put(Constants.PARTITION, partition.toString())
        MDC.put(Constants.OFFSET, offset.toString())
        context().forward(record)
    }
}
