package no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles

import no.nav.k9.søknad.felles.type.Periode
import java.time.LocalDate

data class Kurs(
    val kursholder: String,
    val perioder: List<Periode>,
    val reise: Reise
)

data class Reise(
    val reiserUtenforKursdager: Boolean,
    val reisedager: List<LocalDate>? = null,
    val reisedagerBeskrivelse: String? = null
)