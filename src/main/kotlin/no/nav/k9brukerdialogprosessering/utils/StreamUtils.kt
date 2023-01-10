package no.nav.k9brukerdialogprosessering.utils

import no.nav.k9brukerdialogprosessering.common.TopicEntry
import org.apache.kafka.streams.kstream.KStream
import org.slf4j.MDC

object StreamUtils {
    val soknadIdKey = "soknad_id"
    val correlationIdKey = "correlation_id"
    fun <K, V> KStream<K, V>.metadataToMDC(): KStream<K, V> = peek { key, value ->
        key as String
        value as TopicEntry<*>
        MDC.put(soknadIdKey, key)
        MDC.put(correlationIdKey, value.metadata.correlationId)
    }
}
