package no.nav.k9brukerdialogprosessering.kafka.types

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.innsending.Preprosessert

data class TopicEntry<V>(val metadata: Metadata, val data: V)
data class Metadata(val version: Int, val correlationId: String)
data class Journalfort(@JsonProperty("journalpostId") val journalpostId: String, val søknad: Søknad)
data class Cleanup<V: Preprosessert>(val metadata: Metadata, val melding: V, val journalførtMelding: Journalfort)
