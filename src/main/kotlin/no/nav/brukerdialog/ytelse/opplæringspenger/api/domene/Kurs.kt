package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.KursPerioderMedReiseTid.Companion.tilK9Format
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Kursholder.Companion.tilK9Format
import no.nav.k9.søknad.felles.type.Periode
import java.time.LocalDate
import java.util.*
import no.nav.k9.søknad.ytelse.olp.v1.kurs.Kursholder as K9Kursholder
import no.nav.k9.søknad.ytelse.olp.v1.kurs.KursPeriodeMedReisetid as K9KursPeriodeMedReisetid

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
    val kursperiode: Periode,
    val beskrivelseReisetidTil: String?,
    val beskrivelseReisetidHjem: String?
){
    companion object {
        fun KursPerioderMedReiseTid.tilK9Format(): K9KursPeriodeMedReisetid {
            return K9KursPeriodeMedReisetid(kursperiode, avreise, hjemkomst, beskrivelseReisetidTil, beskrivelseReisetidHjem)
        }
    }
}