package no.nav.brukerdialog.api.ytelse.felles

import no.nav.k9.søknad.JsonUtils
import no.nav.brukerdialog.api.ytelse.fellesdomene.Bosted
import no.nav.brukerdialog.api.ytelse.fellesdomene.Bosted.Companion.somK9Bosteder
import no.nav.brukerdialog.api.ytelse.fellesdomene.Bosted.Companion.somK9Utenlandsopphold
import no.nav.brukerdialog.api.ytelse.fellesdomene.Opphold
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class BostedOgOppholdTest {

    @Test
    fun `Gyldig bosted gir ingen feil`() {
        Validator.verifiserIngenValideringsFeil(
            Bosted(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(2),
                landkode = "BEL",
                landnavn = "Belgia",
                erEØSLand = true
            )
        )
    }

    @Test
    fun `Bosted hvor fraOgMed er etter tilOgMed gir feil`() {
        Validator.verifiserValideringsFeil(
            Bosted(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().minusDays(2),
                landkode = "BEL",
                landnavn = "Belgia",
                erEØSLand = true
            ), 1, "'tilOgMed' må være lik eller etter 'fraOgMed'"
        )
    }

    @Test
    fun `Bosted hvor landnavn er blank gir feil`() {
        Validator.verifiserValideringsFeil(
            Bosted(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(2),
                landkode = "BEL",
                landnavn = " ",
                erEØSLand = true
            ), 1, "Kan ikke være tomt eller blankt"
        )
    }

    @Test
    fun `Bosted hvor landkode er blank gir feil`() {
        Validator.verifiserValideringsFeil(
            Bosted(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(2),
                landkode = " ",
                landnavn = "Belgia",
                erEØSLand = true
            ), 2, "  er ikke en gyldig ISO 3166-1 alpha-3 kode",
            "Kan ikke være tomt eller blankt"
        )
    }

    @Test
    fun `Bosted blir til forventet K9Bosted`() {
        val bosted = Bosted(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-05"),
            landkode = "BE",
            landnavn = "Belgia",
            erEØSLand = true
        )
        val (periode, bostedPeriodeInfo) = bosted.somK9Bosted()
        assertEquals(LocalDate.parse("2022-01-01"), periode.fraOgMed)
        assertEquals(LocalDate.parse("2022-01-05"), periode.tilOgMed)
        assertEquals("BE", bostedPeriodeInfo.land.landkode)
    }

    @Test
    fun `Liste med bosteder blir til forventet K9Bosteder`() {
        val bosteder = listOf(
            Bosted(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-05"),
                landkode = "BE",
                landnavn = "Belgia",
                erEØSLand = true
            ),
            Bosted(
                fraOgMed = LocalDate.parse("2022-01-15"),
                tilOgMed = LocalDate.parse("2022-01-20"),
                landkode = "FJ",
                landnavn = "Fiji",
                erEØSLand = true
            )
        )
        val k9Bosteder = bosteder.somK9Bosteder()
        val forventetK9Bosted = """
            {
              "perioder": {
                "2022-01-01/2022-01-05": {
                  "land": "BE"
                },
                "2022-01-15/2022-01-20": {
                  "land": "FJ"
                }
              },
              "perioderSomSkalSlettes": {}
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventetK9Bosted, JsonUtils.toString(k9Bosteder), true)
    }

    @Test
    fun `Opphold blir til forventet K9Utenlandsopphold`() {
        val opphold = Opphold(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-05"),
            landkode = "BE",
            landnavn = "Belgia",
            erEØSLand = true
        )
        val (periode, bostedPeriodeInfo) = opphold.somK9Utenlandsopphold()
        assertEquals(LocalDate.parse("2022-01-01"), periode.fraOgMed)
        assertEquals(LocalDate.parse("2022-01-05"), periode.tilOgMed)
        assertEquals("BE", bostedPeriodeInfo.land.landkode)
    }

    @Test
    fun `Liste med opphold blir til forventet K9Utenlandsopphold`() {
        val opphold = listOf(
            Opphold(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-05"),
                landkode = "BE",
                landnavn = "Belgia",
                erEØSLand = true
            ),
            Opphold(
                fraOgMed = LocalDate.parse("2022-01-15"),
                tilOgMed = LocalDate.parse("2022-01-20"),
                landkode = "FJ",
                landnavn = "Fiji",
                erEØSLand = true
            )
        )
        val k9Bosteder = opphold.somK9Utenlandsopphold()
        val forventetK9Utenlandsopphold = """
            {
              "perioder": {
                "2022-01-01/2022-01-05": {
                  "land": "BE",
                  "årsak": null,
                  "erSammenMedBarnet": true
                },
                "2022-01-15/2022-01-20": {
                  "land": "FJ",
                  "årsak": null,
                  "erSammenMedBarnet": true
                }
              },
              "perioderSomSkalSlettes": {}
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventetK9Utenlandsopphold, JsonUtils.toString(k9Bosteder), true)
    }
}
