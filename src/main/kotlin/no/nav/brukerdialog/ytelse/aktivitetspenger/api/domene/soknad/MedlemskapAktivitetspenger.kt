package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import no.nav.brukerdialog.utils.erFørEllerLik
import no.nav.brukerdialog.utils.krever
import no.nav.brukerdialog.validation.landkode.ValidLandkode
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.ytelse.aktivitetspenger.v1.medlemskap.Medlemskap
import no.nav.k9.søknad.ytelse.aktivitetspenger.v1.medlemskap.Utenlandsopphold
import no.nav.k9.søknad.ytelse.aktivitetspenger.v1.medlemskap.Utenlandsopphold.UtenlandsoppholdPeriodeInfo
import java.time.LocalDate
import no.nav.k9.søknad.felles.type.Periode as K9Periode

data class MedlemskapAktivitetspenger(
    val harBoddINorge: Boolean,
    val harJobbetINorge: Boolean?,
    val utenlandsopphold: List<@Valid UtenlandsoppholdAktivitetspenger> = listOf(),
) {
    fun tilK9Medlemskap(): Medlemskap = Medlemskap(
        Utenlandsopphold(utenlandsopphold.associate { opphold ->
            K9Periode(opphold.fraOgMed, opphold.tilOgMed) to UtenlandsoppholdPeriodeInfo(
                Landkode.of(opphold.landkode),
                opphold.jobbetIPerioden,
                opphold.utenlandskNasjonalId
            )
        })
    )

    fun valider(felt: String) = mutableListOf<String>().apply {
        utenlandsopphold.forEachIndexed { index, opphold ->
            addAll(opphold.valider("$felt.utenlandsopphold[$index]"))
        }
        krever(
            utenlandsopphold.map { it.fraOgMed to it.tilOgMed }.distinct().size == utenlandsopphold.size,
            "$felt.utenlandsopphold kan ikke inneholde flere opphold med samme periode"
        )
    }
}

data class UtenlandsoppholdAktivitetspenger(
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate,

    @field:NotBlank
    @field:ValidLandkode
    val landkode: String,
    val landnavn: String,
    val jobbetIPerioden: Boolean,

    @field:Size(max = 50)
    val utenlandskNasjonalId: String? = null,
) {
    override fun toString(): String {
        return "UtenlandsoppholdAktivitetspenger(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed, landkode='$landkode', landnavn='$landnavn', jobbetIPerioden=$jobbetIPerioden)"
    }

    fun valider(felt: String) = mutableListOf<String>().apply {
        krever(fraOgMed.erFørEllerLik(tilOgMed), "$felt.fraOgMed må være før $felt.tilOgMed")
        krever(landkode.isNotEmpty(), "$felt.landkode kan ikke være tomt")
        krever(landnavn.isNotEmpty(), "$felt.landnavn kan ikke være tomt")
    }
}
