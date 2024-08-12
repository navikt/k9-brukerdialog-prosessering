package no.nav.brukerdialog.api.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.JsonUtils
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class UtenlandsoppholdIPeriodenTest {
    @Test
    fun `Gyldig UtenlandsoppholdIPerioden gir ingen valideringsfeil`(){
        UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "NLD",
                    landnavn = "Nederland"
                )
            )
        ).valider().verifiserIngenValideringsFeil()
    }

    @Test
    fun `Opphold med feil landkode gir valideringsfeil`(){
        UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "X",
                    landnavn = "Nederland"
                )
            )
        ).valider().verifiserValideringsFeil(1,
            listOf(
                "utenlandsoppholdIPerioden.opphold[0].landkode/landnavn.landkode 'X' er ikke en gyldig ISO 3166-1 alpha-3 kode."
            )
        )
    }

    @Test
    fun `Genererer forventet K9Utenlandsopphold`(){
        val utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "NLD",
                    landnavn = "Nederland"
                )
            )
        )
        val forventet = """
            {
              "perioder": {
                "2022-01-01/2022-01-10": {
                  "land": "NLD",
                  "årsak": null,
                  "erSammenMedBarnet": true
                }
              },
              "perioderSomSkalSlettes": {}
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventet, JsonUtils.toString(utenlandsoppholdIPerioden.somK9Utenlandsopphold()), true)
    }
}
