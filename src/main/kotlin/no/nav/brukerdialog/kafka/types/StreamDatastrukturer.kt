package no.nav.brukerdialog.kafka.types

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.k9.søknad.Innsending
import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.domenetjenester.mottak.Preprosessert

data class TopicEntry<V>(val metadata: MetaInfo, val data: V)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(JournalfortSøknad::class, name = "JournalfortSøknad"),
    JsonSubTypes.Type(JournalfortEttersendelse::class, name = "JournalfortEttersendelse"),
    JsonSubTypes.Type(JournalfortOppgavebekreftelse::class, name = "JournalfortOppgavebekreftelse"),
)
open class Journalfort(open val journalpostId: String, open val søknad: Innsending)
data class JournalfortSøknad(override val journalpostId: String, override val søknad: Søknad) :
    Journalfort(journalpostId, søknad)

data class JournalfortEttersendelse(
    override val journalpostId: String,
    override val søknad: no.nav.k9.ettersendelse.Ettersendelse,
) : Journalfort(journalpostId, søknad)

data class JournalfortOppgavebekreftelse(
    override val journalpostId: String,
    override val søknad: no.nav.k9.oppgave.OppgaveBekreftelse,
) : Journalfort(journalpostId, søknad)

data class Cleanup<T : Preprosessert>(val metadata: MetaInfo, val melding: T, val journalførtMelding: Journalfort)
