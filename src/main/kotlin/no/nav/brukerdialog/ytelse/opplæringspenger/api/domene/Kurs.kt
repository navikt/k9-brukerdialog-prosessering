package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.KursPerioderMedReiseTid.Companion.tilK9Format
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Kursholder.Companion.tilK9Format
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import no.nav.k9.søknad.ytelse.olp.v1.kurs.Kursholder as K9Kursholder
import no.nav.k9.søknad.ytelse.olp.v1.kurs.KursPeriodeMedReisetid as K9KursPeriodeMedReisetid
import no.nav.k9.søknad.felles.type.Periode as K9Periode

data class Kurs(
    val kursholder: Kursholder,
    val perioder: List<KursPerioderMedReiseTid>
) {
    companion object {
        fun Kurs.tilK9Format(): no.nav.k9.søknad.ytelse.olp.v1.kurs.Kurs {
            return no.nav.k9.søknad.ytelse.olp.v1.kurs.Kurs(
                kursholder.tilK9Format(),
                perioder.map{ it.tilK9Format() }
            )
        }
    }
}

data class Kursholder(
    val id: String,
    val navn: String
){
    companion object {
        fun Kursholder.tilK9Format(): K9Kursholder {
            return K9Kursholder(UUID.fromString(id))
        }
    }
}

data class KursPerioderMedReiseTid(
    @JsonFormat(pattern = "yyyy-MM-dd") val avreise: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val hjemkomst: LocalDate,
    val fraOgMed: LocalDateTime,
    val tilOgMed: LocalDateTime,
){
    companion object {
        fun KursPerioderMedReiseTid.tilK9Format(): K9KursPeriodeMedReisetid {
            val periode = K9Periode(fraOgMed.toLocalDate(), tilOgMed.toLocalDate())
            return K9KursPeriodeMedReisetid(periode, avreise, hjemkomst, null, null)
        }
    }
}