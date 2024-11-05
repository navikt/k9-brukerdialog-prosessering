package no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime

data class Kurs(
    val kursholder: Kursholder,
    val perioder: List<KursPerioderMedReiseTid>
)

data class Kursholder(
    val id: String,
    val navn: String
)

data class KursPerioderMedReiseTid(
    @JsonFormat(pattern = "yyyy-MM-dd") val avreise: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val hjemkomst: LocalDate,
    val fraOgMed: LocalDateTime,
    val tilOgMed: LocalDateTime,
)
