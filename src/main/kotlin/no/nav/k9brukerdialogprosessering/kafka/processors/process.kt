package no.nav.k9brukerdialogprosessering.kafka.processors

import kotlinx.coroutines.runBlocking
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry

fun <BEFORE, AFTER> process(
    name: String,
    soknadId: String,
    entry: TopicEntry<BEFORE>,
    block: suspend () -> AFTER,
    ): TopicEntry<AFTER> = runBlocking {
        val processed = try {
            block()
        } catch (cause: Throwable) {
            throw cause
        }
        TopicEntry(metadata = entry.metadata, data = processed)
    }
