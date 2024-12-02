package no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.type.Periode
import java.time.LocalDate

data class Kurs(
    val kursholder: String,
    val perioder: List<KursPerioderMedReiseTid>
)

data class KursPerioderMedReiseTid(
    @JsonFormat(pattern = "yyyy-MM-dd") val avreise: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val hjemkomst: LocalDate,
    val kursperiode: Periode,
    val harTaptArbeidstid: Boolean,
    val begrunnelseForReiseOverEnDag: String?
)
