package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import no.nav.brukerdialog.utils.erFørEllerLik
import no.nav.brukerdialog.utils.krever
import no.nav.brukerdialog.validation.landkode.ValidLandkode
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.ytelse.aktivitetspenger.v1.Bosteder
import java.time.LocalDate
import no.nav.k9.søknad.felles.type.Periode as K9Periode

data class ForutgåendeBosteder(
    val harBoddIUtlandetSiste5År: Boolean,
    val utenlandsoppholdSiste5År: List<Bosted> = listOf(),
) {
    fun tilK9Bosteder(): Bosteder {
        if (!harBoddIUtlandetSiste5År || utenlandsoppholdSiste5År.isEmpty()) {
            return Bosteder()
        }

        return Bosteder().medPerioder(utenlandsoppholdSiste5År.associate { bosted ->
            K9Periode(bosted.fraOgMed, bosted.tilOgMed) to
                    Bosteder.BostedPeriodeInfo().medLand(Landkode.of(bosted.landkode))
        })
    }
}

data class Bosted(
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate,

    @field:NotBlank
    @field:ValidLandkode
    val landkode: String,
    val landnavn: String,
) {
    override fun toString(): String {
        return "Bosted(fraOgMed=$fraOgMed, tilOgMed=$tilOgMed, landkode='$landkode', landnavn='$landnavn')"
    }

    fun valider(felt: String) = mutableListOf<String>().apply {
        krever(fraOgMed.erFørEllerLik(tilOgMed), "$felt.fraOgMed må være før $felt.tilOgMed")
        krever(landkode.isNotEmpty(), "$felt.landkode kan ikke være tomt")
        krever(landnavn.isNotEmpty(), "$felt.landnavn kan ikke være tomt")
    }
}