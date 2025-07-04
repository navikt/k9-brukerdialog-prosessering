package no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles

import java.time.LocalDate

data class Utenlandsopphold(
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String,
)
