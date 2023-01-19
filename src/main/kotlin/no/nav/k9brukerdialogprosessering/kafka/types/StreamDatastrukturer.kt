package no.nav.k9brukerdialogprosessering.kafka.types

import no.nav.k9.søknad.Søknad

data class TopicEntry<V>(val metadata: Metadata, val data: V)
data class Metadata(val version: Int, val correlationId: String)
data class Journalfort(val journalpostId: String, val søknad: Søknad)
data class Cleanup(val metadata: Metadata, val melding: Any, val journalførtMelding: Journalfort)
