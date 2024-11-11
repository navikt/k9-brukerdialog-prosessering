package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.KursPerioderMedReiseTid.Companion.tilK9Format
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Kursholder.Companion.tilK9Format
import no.nav.k9.søknad.felles.type.Periode
import java.time.LocalDate
import java.util.*
import no.nav.k9.søknad.ytelse.olp.v1.kurs.KursPeriodeMedReisetid as K9KursPeriodeMedReisetid
import no.nav.k9.søknad.ytelse.olp.v1.kurs.Kursholder as K9Kursholder

data class Kurs(
    val kursholder: Kursholder,
    @field:NotEmpty(message = "Kan ikke være tom liste") val perioder: List<KursPerioderMedReiseTid>
) {
    companion object {
        fun Kurs.tilK9Format(): no.nav.k9.søknad.ytelse.olp.v1.kurs.Kurs {
            return no.nav.k9.søknad.ytelse.olp.v1.kurs.Kurs(
                kursholder.tilK9Format(),
                perioder.map { it.tilK9Format() }
            )
        }
    }
}

data class Kursholder(
    val id: String? = null,
    val navn: String? = null,
    val erAnnen: Boolean = false
) {
    @AssertTrue(message = "id og navn må være null for annen kursholder")
    private fun isUkjentKursholder(): Boolean {
        if (erAnnen) {
            return navn == null && id == null
        }
        return true
    }

    @AssertTrue(message = "id og navn må være satt for kjent kursholder")
    private fun isKjentKursholder(): Boolean {
        if (!erAnnen) {
            return navn != null && id != null
        }
        return true
    }


    companion object {
        fun Kursholder.tilK9Format(): K9Kursholder {
            if (id != null) {
                return K9Kursholder(UUID.fromString(id))
            }
            return K9Kursholder(id)
        }
    }
}

data class KursPerioderMedReiseTid(
    @JsonFormat(pattern = "yyyy-MM-dd") val avreise: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val hjemkomst: LocalDate,
    val kursperiode: Periode,
    val beskrivelseReisetidTil: String?,
    val beskrivelseReisetidHjem: String?
) {
    companion object {
        fun KursPerioderMedReiseTid.tilK9Format(): K9KursPeriodeMedReisetid {
            return K9KursPeriodeMedReisetid(
                kursperiode,
                avreise,
                hjemkomst,
                beskrivelseReisetidTil,
                beskrivelseReisetidHjem
            )
        }
    }
}