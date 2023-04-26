package no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles

import java.time.LocalDate

data class StønadGodtgjørelse(
    val mottarStønadGodtgjørelse: Boolean?,
    val startdato: LocalDate? = null,
    val sluttdato: LocalDate? = null
)
