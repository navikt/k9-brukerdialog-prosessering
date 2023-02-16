package no.nav.k9brukerdialogprosessering.kafka.processors

import kotlinx.coroutines.runBlocking
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamName
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.utils.RetryContextUtils.logStreamingRetries
import org.slf4j.Logger
import org.springframework.retry.support.RetryTemplate

fun <BEFORE, AFTER> process(
    name: KafkaStreamName,
    entry: TopicEntry<BEFORE>,
    retryTemplate: RetryTemplate,
    logger: Logger,
    block: suspend () -> AFTER,
): TopicEntry<AFTER> {
    val processed = try {
        retryTemplate.execute<AFTER, Throwable> { context ->
            runBlocking {
                context.logStreamingRetries(name.name, logger)
                block()
            }
        }
    } catch (cause: Throwable) {
        throw cause
    }
    return TopicEntry(metadata = entry.metadata, data = processed)
}
