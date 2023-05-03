package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene.domene

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class AnnenForelder(
    val navn: String,
    val fnr: String,
    val situasjon: Situasjon,
    val situasjonBeskrivelse: String? = null,
    val periodeOver6Måneder: Boolean? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") val periodeFraOgMed: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") val periodeTilOgMed: LocalDate? = null
) {
    fun somMapTilPdf(): Map<String, Any?> = mapOf(
        "navn" to navn,
        "fnr" to fnr,
        "situasjon" to situasjon.utskriftvennlig,
        "beskrivelse" to situasjonBeskrivelse,
        "periodeOver6Måneder" to periodeOver6Måneder,
        "periodeFraOgMed" to periodeFraOgMed.formaterDato(),
        "periodeTilOgMed" to periodeTilOgMed.formaterDato()
    )

    override fun toString(): String {
        return "AnnenForelder(navn='$navn', fnr='*****')"
    }
}

private fun LocalDate?.formaterDato(): String?{
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.of("Europe/Oslo"))
    return if(this == null) null else dateFormatter.format(this)
}

enum class Situasjon(val utskriftvennlig: String){
    INNLAGT_I_HELSEINSTITUSJON("Innlagt i helseinstitusjon"),
    UTØVER_VERNEPLIKT("Utøver verneplikt"),
    FENGSEL("Fengsel"),
    SYKDOM("Sykdom, skade eller funksjonshemning"),
    ANNET("Annet")
}
