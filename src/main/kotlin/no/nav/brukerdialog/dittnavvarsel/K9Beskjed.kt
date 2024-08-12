package no.nav.brukerdialog.dittnavvarsel

import no.nav.brukerdialog.common.MetaInfo

data class K9Beskjed(
    val metadata: MetaInfo,
    val grupperingsId: String,
    val tekst: String,
    val link: String?,
    val dagerSynlig: Long,
    val søkerFødselsnummer: String,
    val eventId: String,
    val ytelse: String
)
