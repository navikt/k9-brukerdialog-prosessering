package no.nav.k9brukerdialogprosessering.api.ytelse.felles

import no.nav.k9.søknad.JsonUtils
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Barn
import no.nav.k9brukerdialogprosessering.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogprosessering.utils.TestUtils.Validator
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserValideringsFeil
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class BarnTest {

    @Test
    fun `Barn equals test`() {
        val barn = Barn(
            norskIdentifikator = "02119970078",
            fødselsdato = LocalDate.parse("2022-01-01"),
            aktørId = "12345",
            navn = "Barn Barnesen"
        )
        assertFalse(barn.equals(null))
        assertTrue(
            barn == Barn(
                norskIdentifikator = "02119970078",
                fødselsdato = LocalDate.parse("2022-01-01"),
                aktørId = "12345",
                navn = "Barn Barnesen"
            )
        )
    }

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
        val barn = Barn(navn = "Barn uten identifikator", aktørId = "123")
        assertTrue(barn.manglerIdentifikator())
        barn.leggTilIdentifikatorHvisMangler(barnFraOppslag)
        assertFalse(barn.manglerIdentifikator())
    }

    @Test
    fun `Barn til k9Barn blir som forventet`() {
        val barn = Barn(
            norskIdentifikator = "02119970078",
            fødselsdato = LocalDate.parse("2022-01-01"),
            aktørId = "12345",
            navn = "Barn Barnesen"
        )

        val forventetK9Barn = JSONObject(
            """
            {
              "norskIdentitetsnummer": "02119970078",
              "fødselsdato": null
            }
        """.trimIndent()
        )

        JSONAssert.assertEquals(forventetK9Barn, JSONObject(JsonUtils.toString(barn.somK9Barn())), true)
    }

    @Test
    fun `Gyldig barn gir ingen valideringsfeil`() {
        Barn(
            norskIdentifikator = "02119970078",
            navn = "Barnesen"
        ).valider("barn").verifiserIngenValideringsFeil()
    }

    @Test
    fun `Forvent valideringsfeil dersom norskIdentifikator er null`() {
        Barn(
            norskIdentifikator = null,
            aktørId = "123",
            navn = "Barn"
        ).valider("barn")
            .verifiserValideringsFeil(1, listOf("barn.norskIdentifikator kan ikke være null eller blank."))
    }

    @Test
    fun `Forvent valideringsfeil dersom norskIdentifikator er ugyldig`() {
        Validator.verifiserValideringsFeil(
            Barn(
                norskIdentifikator = "123ABC",
                aktørId = "123",
                navn = "Barn"
            ),
            2,
            "'123ABC' matcher ikke tillatt pattern '^\\d+\$'",
            "size must be between 11 and 11"
        )
    }

    @Test
    fun `Forvent valideringsfeil dersom navn er blank`() {
        Barn(
            norskIdentifikator = "02119970078",
            navn = " "
        ).valider("barn").verifiserValideringsFeil(1, listOf("barn.navn kan ikke være tomt eller blank."))
    }
}
