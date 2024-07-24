package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgsdageraleneomsorg.domene

import no.nav.k9.søknad.JsonUtils
import no.nav.k9brukerdialogprosessering.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogprosessering.utils.TestUtils.VALIDATOR
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserFeil
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserIngenFeil
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class OmsorgsdagerAleneomsorgBarnTest {

    @Test
    fun `Oppdatering av identifikator på barn som mangler`() {
        val barnFraOppslag = listOf(
            BarnOppslag(
                fødselsdato = LocalDate.now(),
                fornavn = "Barn",
                mellomnavn = null,
                etternavn = "Barnesen",
                aktørId = "123",
                identitetsnummer = "02119970078"
            )
        )
        val barn = Barn(
            navn = "Barn uten identifikator",
            type = TypeBarn.FRA_OPPSLAG,
            aktørId = "123",
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.SISTE_2_ÅRENE,
            dato = LocalDate.parse("2022-01-01")
        )
        assertTrue(barn.manglerIdentifikator())
        barn.leggTilIdentifikatorHvisMangler(barnFraOppslag)
        assertFalse(barn.manglerIdentifikator())
    }

    @Test
    fun `Barn blir mappet til forventet K9Barn`() {
        val barn = Barn(
            navn = "Barn uten identifikator",
            type = TypeBarn.FRA_OPPSLAG,
            aktørId = "123",
            identitetsnummer = "02119970078",
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
        )
        val forventetK9Barn = """
            {
              "norskIdentitetsnummer": "02119970078",
              "fødselsdato": null
            }
        """.trimIndent()

        JSONAssert.assertEquals(forventetK9Barn, JsonUtils.toString(barn.somK9Barn()), true)
    }

    @Test
    fun `Skal kunne registrere fosterbarn uten aktørId`() {
        VALIDATOR.validate(
            Barn(
                navn = "Barn uten identifikator",
                type = TypeBarn.FOSTERBARN,
                fødselsdato = LocalDate.now().minusMonths(4),
                identitetsnummer = "02119970078",
                tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
            )
        ).verifiserIngenFeil()
    }

    @Test
    fun `Forvent valideringsfeil dersom norskIdentifikator er ugyldig`() {
        VALIDATOR.validate(
            Barn(
                navn = "Barn",
                type = TypeBarn.FRA_OPPSLAG,
                aktørId = "123",
                identitetsnummer = "ABC123",
                tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
            )
        ).verifiserFeil(
            2,
            "'ABC123' matcher ikke tillatt pattern '^\\d+$'",
            "size must be between 11 and 11"
        )
    }

    @Test
    fun `Forvent valideringsfeil dersom navn er blank`() {
        VALIDATOR.validate(
            Barn(
                navn = " ",
                type = TypeBarn.FRA_OPPSLAG,
                aktørId = "123",
                identitetsnummer = "02119970078",
                tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
            )
        ).verifiserFeil(1, "Kan ikke være tomt eller blankt")
    }

    @Test
    fun `Forvent valideringsfeil dersom navn er 101 tegn`() {
        VALIDATOR.validate(
            Barn(
                navn = "barnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnb",
                type = TypeBarn.FRA_OPPSLAG,
                aktørId = "123",
                identitetsnummer = "02119970078",
                tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
            )
        ).verifiserFeil(1, "Kan ikke være mer enn 100 tegn")
    }

    @Test
    fun `Forvent valideringsfeil dersom fødselsdato er null og barnet er fosterbarn`() {
        VALIDATOR.validate(
            Barn(
                navn = "Navnesen",
                type = TypeBarn.FOSTERBARN,
                fødselsdato = null,
                aktørId = "123",
                identitetsnummer = "02119970078",
                tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
            )
        ).verifiserFeil(1, "Må være satt når 'type' er annet enn 'FRA_OPPSLAG'")
    }

    @Test
    fun `Skal feile dersom tidspunktForAleneomsorg er siste 2 år, men dato er ikke satt`() {
        VALIDATOR.validate(
            Barn(
                navn = "Barnesen",
                type = TypeBarn.FRA_OPPSLAG,
                aktørId = "123",
                identitetsnummer = "02119970078",
                tidspunktForAleneomsorg = TidspunktForAleneomsorg.SISTE_2_ÅRENE,
                dato = null
            )
        ).verifiserFeil(1, "Må være satt når 'tidspunktForAleneomsorg' er 'SISTE_2_ÅRENE'")
    }
}
