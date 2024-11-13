package no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.type.Periode
import java.time.LocalDate

data class Kurs(
    val kursholder: Kursholder,
    val perioder: List<KursPerioderMedReiseTid>
)

data class Kursholder(
    val id: String? = null,
    val navn: String? = null,
    val erAnnen: Boolean = false
)

data class KursPerioderMedReiseTid(
    @JsonFormat(pattern = "yyyy-MM-dd") val avreise: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val hjemkomst: LocalDate,
    val kursperiode: Periode,
    val beskrivelseReisetidTil: String?,
    val beskrivelseReisetidHjem: String?
)
