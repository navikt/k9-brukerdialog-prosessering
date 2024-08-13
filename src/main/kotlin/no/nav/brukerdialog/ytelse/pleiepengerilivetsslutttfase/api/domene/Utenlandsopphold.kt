package no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import no.nav.k9.søknad.felles.personopplysninger.Bosteder.BostedPeriodeInfo
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.Periode
import no.nav.brukerdialog.validation.landkode.ValidLandkode
import no.nav.brukerdialog.ytelse.fellesdomene.Land
import no.nav.brukerdialog.utils.erFørEllerLik
import no.nav.brukerdialog.utils.krever
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold as K9Utenlandsopphold

class Utenlandsopphold(
    @JsonFormat(pattern = "yyyy-MM-dd")
    private val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    private val tilOgMed: LocalDate,

    @field:NotBlank
    @field:ValidLandkode
    private val landkode: String,

    private val landnavn: String, // TODO: 02/09/2022 Refaktorere til å bruke klassen land i stedet. Må endre frontend
) {
    companion object {
        internal fun List<Utenlandsopphold>.valider(felt: String) =
            flatMapIndexed { index: Int, utenlandsopphold: Utenlandsopphold ->
                utenlandsopphold.valider("$felt[$index]")
            }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        addAll(Land(landkode = landkode, landnavn = landnavn).valider("$felt.landkode/landnavn"))
        krever(fraOgMed.erFørEllerLik(tilOgMed), "$felt.fraOgMed må være før eller lik tilOgMed.")
    }

    internal fun k9Periode() = Periode(fraOgMed, tilOgMed)
    internal fun somK9BostedPeriodeInfo() = BostedPeriodeInfo().medLand(Landkode.of(landkode))
    internal fun somK9UtenlandsoppholdPeriodeInfo() =
        K9Utenlandsopphold.UtenlandsoppholdPeriodeInfo().medLand(Landkode.of(landkode))
}
