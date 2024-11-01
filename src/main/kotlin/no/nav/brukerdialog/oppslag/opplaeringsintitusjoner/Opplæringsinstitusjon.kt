package no.nav.brukerdialog.oppslag.opplaeringsintitusjoner

import no.nav.k9.søknad.felles.type.Periode
import java.util.*

data class Opplæringsinstitusjon(
    val uuid: UUID,
    val navn: String,
    val perioder: List<Periode>
)