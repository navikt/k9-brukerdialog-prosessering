package no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.felles

import java.time.LocalDate

data class StønadGodtgjørelse(
    private val mottarStønadGodtgjørelse: Boolean,
    private val startDato: LocalDate? = null,
    private val slutttDato: LocalDate? = null
) 
