package no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles

import no.nav.k9.søknad.felles.type.Periode
import java.time.Duration
import java.time.LocalDate
import java.util.*

data class Kurs(
    val kursholder: Kursholder,
    val enkeltdagEllerPeriode: KursVarighetType,
    val kursperioder: List<Periode>? = null,
    val kursdager: List<KursDag>? = null,
    val reise: Reise? = null
)

data class Kursholder(
    val uuid: UUID? = null,
    val navn: String
)

data class KursDag(
    val dato: LocalDate,
    val tidKurs: Duration,
    val tidReise: Duration? = null
)

data class Reise(
    val reiserUtenforKursdager: Boolean,
    val reisedager: List<LocalDate>? = null,
    val reisedagerBeskrivelse: String? = null
)

enum class KursVarighetType {
    ENKELTDAG,
    PERIODE
}