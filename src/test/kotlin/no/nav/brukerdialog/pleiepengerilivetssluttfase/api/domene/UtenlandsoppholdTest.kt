package no.nav.brukerdialog.pleiepengerilivetssluttfase.api.domene

import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.Utenlandsopphold
import no.nav.k9.søknad.felles.personopplysninger.Bosteder.BostedPeriodeInfo
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UtenlandsoppholdTest {

    @Test
    fun `Gyldig Utenlandsopphold gir ingen valideringsfeil`(){
        Utenlandsopphold(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-10"),
            landkode = "NLD",
            landnavn = "Nederland"
        ).valider("utenlandsopphold").verifiserIngenValideringsFeil()
    }

    @Test
    fun `Ugyldig landnavn og landkode gir valideringsfeil`(){
        Utenlandsopphold(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-10"),
            landkode = "X",
            landnavn = " "
        ).valider("utenlandsopphold").verifiserValideringsFeil(2,
            listOf(
                "utenlandsopphold.landkode/landnavn.landnavn kan ikke være tomt eller blankt.",
                "utenlandsopphold.landkode/landnavn.landkode 'X' er ikke en gyldig ISO 3166-1 alpha-3 kode."
            )
        )
    }

    @Test
    fun `fraOgMed etter tilOgMed gir valideringsfeil`(){
        Utenlandsopphold(
            fraOgMed = LocalDate.parse("2022-01-04"),
            tilOgMed = LocalDate.parse("2022-01-01"),
            landkode = "NLD",
            landnavn = "Nederland"
        ).valider("utenlandsopphold").verifiserValideringsFeil(1,
            listOf(
                "utenlandsopphold.fraOgMed må være før eller lik tilOgMed."
            )
        )
    }

    @Test
    fun `somK9BostedPeriodeInfo fungerer som forventet`(){
        val utenlandsopphold = Utenlandsopphold(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-01"),
            landkode = "XKX", landnavn = "Kosovo"
        )
        val forventet = BostedPeriodeInfo().medLand(Landkode.of("XKX"))
        assertEquals(forventet.land.landkode, utenlandsopphold.somK9BostedPeriodeInfo().land.landkode)
    }

}
