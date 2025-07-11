package no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles

import java.time.LocalDate

data class StønadGodtgjørelse(
    val mottarStønadGodtgjørelse: Boolean?,
    val startdato: LocalDate? = null,
    val sluttdato: LocalDate? = null
)
