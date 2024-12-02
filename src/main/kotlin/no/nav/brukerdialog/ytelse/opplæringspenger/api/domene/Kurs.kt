package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotEmpty
import no.nav.k9.søknad.felles.type.Periode
import java.time.LocalDate
import no.nav.k9.søknad.ytelse.olp.v1.kurs.KursPeriodeMedReisetid as K9KursPeriodeMedReisetid
import no.nav.k9.søknad.ytelse.olp.v1.kurs.Kursholder as K9Kursholder

data class Kurs(
    val kursholder: Kursholder,
    @field:NotEmpty(message = "Kan ikke være tom liste") val perioder: List<KursPerioderMedReiseTid>
) {
    fun tilK9Format(): no.nav.k9.søknad.ytelse.olp.v1.kurs.Kurs {
        return no.nav.k9.søknad.ytelse.olp.v1.kurs.Kurs(
            kursholder.tilK9Format(),
            perioder.map { it.tilK9Format() }
        )
    }
}

data class Kursholder(
    val navn: String? = null
) {

    fun tilK9Format(): K9Kursholder {
        return K9Kursholder(null)
    }
}

data class KursPerioderMedReiseTid(
    @JsonFormat(pattern = "yyyy-MM-dd") val avreise: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val hjemkomst: LocalDate,
    val kursperiode: Periode,
    val harTaptArbeidstid: Boolean,
    val begrunnelseForReiseOverEnDag: String? = null
) {
    fun tilK9Format(): K9KursPeriodeMedReisetid {
        return K9KursPeriodeMedReisetid(
            kursperiode,
            avreise,
            hjemkomst,
            begrunnelseForReiseOverEnDag,
            null
        )
    }
}