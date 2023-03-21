package no.nav.k9brukerdialogprosessering.kafka.types

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.k9.søknad.Innsending
import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.innsending.Preprosessert

data class TopicEntry<V>(val metadata: Metadata, val data: V)
data class Metadata(val version: Int, val correlationId: String)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(JournalfortSøknad::class, name = "JournalfortSøknad"),
    JsonSubTypes.Type(JournalfortEttersendelse::class, name = "JournalfortEttersendelse")
)
open class Journalfort(open val journalpostId: String, open val søknad: Innsending)
data class JournalfortSøknad(override val journalpostId: String, override val søknad: Søknad): Journalfort(journalpostId, søknad)
data class JournalfortEttersendelse(override val journalpostId: String, override val søknad: no.nav.k9.ettersendelse.Ettersendelse): Journalfort(journalpostId, søknad)
data class Cleanup<T: Preprosessert>(val metadata: Metadata, val melding: T, val journalførtMelding: Journalfort)
