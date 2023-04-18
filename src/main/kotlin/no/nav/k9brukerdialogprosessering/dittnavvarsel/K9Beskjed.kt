package no.nav.k9brukerdialogprosessering.dittnavvarsel

import no.nav.k9brukerdialogprosessering.kafka.types.Metadata

data class K9Beskjed(
    val metadata: Metadata,
    val grupperingsId: String,
    val tekst: String,
    val link: String?,
    val dagerSynlig: Long,
    val søkerFødselsnummer: String,
    val eventId: String,
    val ytelse: String
)
