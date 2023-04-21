package no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Arbeidsforhold
import java.time.LocalDate

data class Frilans(
    @JsonFormat(pattern = "yyyy-MM-dd") val startdato: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val sluttdato: LocalDate? = null,
    val jobberFortsattSomFrilans: Boolean,
    val arbeidsforhold: Arbeidsforhold? = null,
    val harHattInntektSomFrilanser: Boolean? = null
)

