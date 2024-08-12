package no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.api.domene

import no.nav.k9.søknad.JsonUtils
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.api.domene.AnnenForelder
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.api.domene.Situasjon
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class AnnenForelderTest {

    @Test
    fun `AnnenForelder equals test`() {
        val annenForelder = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeTilOgMed = LocalDate.parse("2021-08-01")
        )
        assertTrue(
            annenForelder.equals(
                AnnenForelder(
                    navn = "Navnesen",
                    fnr = "26104500284",
                    situasjon = Situasjon.FENGSEL,
                    periodeFraOgMed = LocalDate.parse("2021-01-01"),
                    periodeTilOgMed = LocalDate.parse("2021-08-01")
                )
            )
        )
        assertFalse(annenForelder.equals(null))
    }

    @Test
    fun `AnnenForelder blir mappet til forventet K9Format`() {
        val faktisk = AnnenForelder(
            navn = "Navnesen",
            fnr = "26104500284",
            situasjon = Situasjon.FENGSEL,
            periodeFraOgMed = LocalDate.parse("2021-01-01"),
            periodeTilOgMed = LocalDate.parse("2021-08-01")
        ).somK9AnnenForelder()

        val forventet = """
            {
              "norskIdentitetsnummer": "26104500284",
              "situasjon": "FENGSEL",
              "situasjonBeskrivelse": null,
              "periode": "2021-01-01/2021-08-01"
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventet, JsonUtils.toString(faktisk), true)
    }

    @Test
    fun `Skal gi valideringsfeile dersom navn er blankt`() {
        Validator.verifiserValideringsFeil(
            AnnenForelder(
                navn = " ",
                fnr = "26104500284",
                situasjon = Situasjon.FENGSEL,
                periodeFraOgMed = LocalDate.parse("2021-01-01"),
                periodeTilOgMed = LocalDate.parse("2021-08-01")
            ), 1, "Kan ikke være tomt eller blankt"
        )
    }

    @Test
    fun `Skal gi valideringsfeile dersom fnr er ugyldig`() {
        Validator.verifiserValideringsFeil(
            AnnenForelder(
                navn = "Navnesen",
                fnr = "123ABC",
                situasjon = Situasjon.FENGSEL,
                periodeFraOgMed = LocalDate.parse("2021-01-01"),
                periodeTilOgMed = LocalDate.parse("2021-08-01")
            ),
            2,
            "size must be between 11 and 11",
            "'123ABC' matcher ikke tillatt pattern '^\\d+$'"
        )
    }

    @Test
    fun `Skal gi valideringsfeile dersom fraOgMed er etter tilOgMed`() {
        Validator.verifiserValideringsFeil(
            AnnenForelder(
                navn = "Navnesen",
                fnr = "26104500284",
                situasjon = Situasjon.FENGSEL,
                periodeOver6Måneder = true,
                periodeFraOgMed = LocalDate.parse("2021-01-02"),
                periodeTilOgMed = LocalDate.parse("2021-01-01")
            ),
            1,
            "Derom 'periodeTilOgMed' er satt må den være lik eller etter 'periodeFraOgMed'"
        )
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon INNLAGT_I_HELSEINSTITUSJON hvor periodeOver6Måneder er satt`() {
        Validator.verifiserIngenValideringsFeil(
            AnnenForelder(
                navn = "Navnesen",
                fnr = "26104500284",
                situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
                periodeOver6Måneder = true,
                periodeFraOgMed = LocalDate.parse("2021-01-01")
            )
        )
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon INNLAGT_I_HELSEINSTITUSJON hvor periodeFraOgMed og periodeTilOgMed er satt`() {
        Validator.verifiserIngenValideringsFeil(
            AnnenForelder(
                navn = "Navnesen",
                fnr = "26104500284",
                situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
                periodeFraOgMed = LocalDate.parse("2020-01-01"),
                periodeTilOgMed = LocalDate.parse("2020-07-01")
            )
        )
    }

    @Test
    fun `Ved situasjon INNLAGT_I_HELSEINSTITUSJON skal det gi feil dersom periodeFraOgMed=null og periodeOver6Måneder=null`() {
        Validator.verifiserValideringsFeil(
            AnnenForelder(
                navn = "Navnesen",
                fnr = "26104500284",
                situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
                periodeFraOgMed = LocalDate.parse("2020-01-01"),
                periodeTilOgMed = null,
                periodeOver6Måneder = null
            ),
            1,
            "Derom 'situasjon' er 'INNLAGT_I_HELSEINSTITUSJON', 'SYKDOM', eller 'ANNET' må 'periodeTilOgMed' eller 'periodeOver6Måneder' være satt"
        )
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon FENGSEL`() {
        Validator.verifiserIngenValideringsFeil(
            AnnenForelder(
                navn = "Navnesen",
                fnr = "26104500284",
                situasjon = Situasjon.FENGSEL,
                periodeFraOgMed = LocalDate.parse("2020-01-01"),
                periodeTilOgMed = LocalDate.parse("2021-08-01")
            )
        )
    }

    @Test
    fun `Ved situasjon FENGSEL skal det gi valideringsfeil dersom periodeTilOgMed er null`() {
        Validator.verifiserValideringsFeil(
            AnnenForelder(
                navn = "Navnesen",
                fnr = "26104500284",
                situasjon = Situasjon.FENGSEL,
                periodeFraOgMed = LocalDate.parse("2021-01-01"),
                periodeTilOgMed = null
            ),
            1,
            "Derom 'situasjon' er 'UTØVER_VERNEPLIKT', 'SYKDOM', 'ANNET' eller 'FENGSEL' må 'periodeTilOgMed' være satt"
        )
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon UTØVER_VERNEPLIKT`() {
        Validator.verifiserIngenValideringsFeil(
            AnnenForelder(
                navn = "Navnesen",
                fnr = "26104500284",
                situasjon = Situasjon.UTØVER_VERNEPLIKT,
                periodeFraOgMed = LocalDate.parse("2020-01-01"),
                periodeTilOgMed = LocalDate.parse("2021-08-01")
            )
        )
    }

    @Test
    fun `Ved situasjon UTØVER_VERNEPLIKT skal det gi valideringsfeil dersom periodeTilOgMed er null`() {
        Validator.verifiserValideringsFeil(
            AnnenForelder(
                navn = "Navnesen",
                fnr = "26104500284",
                situasjon = Situasjon.UTØVER_VERNEPLIKT,
                periodeFraOgMed = LocalDate.parse("2021-01-01"),
                periodeTilOgMed = null
            ),
            1,
            "Derom 'situasjon' er 'UTØVER_VERNEPLIKT', 'SYKDOM', 'ANNET' eller 'FENGSEL' må 'periodeTilOgMed' være satt"
        )
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon ANNET`() {
        Validator.verifiserIngenValideringsFeil(
            AnnenForelder(
                navn = "Navnesen",
                fnr = "26104500284",
                situasjon = Situasjon.ANNET,
                situasjonBeskrivelse = "Blabla noe skjedde",
                periodeFraOgMed = LocalDate.parse("2021-01-01"),
                periodeOver6Måneder = true
            )
        )
    }

    @Test
    fun `Ved situasjon ANNET skal det gi feil dersom situasjonBeskrivelse er tom`() {
        Validator.verifiserValideringsFeil(
            AnnenForelder(
                navn = "Navnesen",
                fnr = "26104500284",
                situasjon = Situasjon.ANNET,
                situasjonBeskrivelse = "",
                periodeOver6Måneder = true,
                periodeFraOgMed = LocalDate.parse("2021-01-01")
            ),
            1,
            "Derom 'situasjon' er 'SYKDOM', eller 'ANNET' må 'situasjonBeskrivelse' være satt"
        )
    }

    @Test
    fun `Ved situasjon ANNET skal det gi feil dersom periodeOver6Måneder=null og periodeTilOgMed=null`() {
        Validator.verifiserValideringsFeil(
            AnnenForelder(
                navn = "Navnesen",
                fnr = "26104500284",
                situasjon = Situasjon.ANNET,
                situasjonBeskrivelse = "COVID-19",
                periodeFraOgMed = LocalDate.parse("2021-01-01"),
                periodeTilOgMed = null,
                periodeOver6Måneder = null
            ),
            1,
            "Derom 'situasjon' er 'INNLAGT_I_HELSEINSTITUSJON', 'SYKDOM', eller 'ANNET' må 'periodeTilOgMed' eller 'periodeOver6Måneder' være satt"
        )
    }

    @Test
    fun `Gyldig AnnenForelder med situasjon SYKDOM`() {
        Validator.verifiserIngenValideringsFeil(
            AnnenForelder(
                navn = "Navnesen",
                fnr = "26104500284",
                situasjon = Situasjon.SYKDOM,
                situasjonBeskrivelse = "Blabla noe skjedde",
                periodeFraOgMed = LocalDate.parse("2021-01-01"),
                periodeOver6Måneder = true
            )
        )
    }

    @Test
    fun `Ved situasjon SYKDOM skal det gi feil dersom situasjonBeskrivelse er tom`() {
        Validator.verifiserValideringsFeil(
            AnnenForelder(
                navn = "Navnesen",
                fnr = "26104500284",
                situasjon = Situasjon.SYKDOM,
                situasjonBeskrivelse = "",
                periodeOver6Måneder = true,
                periodeFraOgMed = LocalDate.parse("2021-01-01")
            ),
            1,
            "Derom 'situasjon' er 'SYKDOM', eller 'ANNET' må 'situasjonBeskrivelse' være satt"
        )
    }

    @Test
    fun `Ved situasjon SYKDOM skal det gi feil dersom periodeOver6Måneder ikke er satt`() {
        Validator.verifiserValideringsFeil(
            AnnenForelder(
                navn = "Navnesen",
                fnr = "26104500284",
                situasjon = Situasjon.SYKDOM,
                situasjonBeskrivelse = "COVID-19",
                periodeFraOgMed = LocalDate.parse("2021-01-01")
            ),
            1,
            "Derom 'situasjon' er 'INNLAGT_I_HELSEINSTITUSJON', 'SYKDOM', eller 'ANNET' må 'periodeTilOgMed' eller 'periodeOver6Måneder' være satt"
        )
    }
}
