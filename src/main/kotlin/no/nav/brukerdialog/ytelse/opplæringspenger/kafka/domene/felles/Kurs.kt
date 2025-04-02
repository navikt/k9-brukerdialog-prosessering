package no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles

import no.nav.k9.søknad.felles.type.Periode
import java.time.LocalDate
import java.util.*

data class Kurs(
    val kursholder: Kursholder,
    val kursperioder: List<Periode>,
    val reise: Reise
)

data class Kursholder(
    val uuid: UUID? = null,
    val navn: String
)

data class Reise(
    val reiserUtenforKursdager: Boolean,
    val reisedager: List<LocalDate>? = null,
    val reisedagerBeskrivelse: String? = null
)