package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9.søknad.JsonUtils
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingsnf.domene.Frilans
import no.nav.k9brukerdialogprosessering.utils.TestUtils.Validator
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class FrilansTest {

    @Test
    fun `Frilans blir mappet til riktig K9Frilanser`() {
        val k9Frilans = Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            sluttdato = LocalDate.parse("2022-10-01"),
            jobberFortsattSomFrilans = true
        ).somK9Frilanser()

        val forventetK9Frilanser = """
            {
                "startdato" : "2022-01-01",
                "sluttdato" : "2022-10-01"
            }
        """.trimIndent()

        JSONAssert.assertEquals(forventetK9Frilanser, JsonUtils.toString(k9Frilans), true)
    }

    @Test
    fun `Gyldig Frilans gir ingen valideringsfeil`() {
        Validator.verifiserIngenValideringsFeil(
            Frilans(
                startdato = LocalDate.parse("2022-01-01"),
                sluttdato = null,
                jobberFortsattSomFrilans = true
            )
        )
    }

    @Test
    fun `Frilans hvor sluttdato er før startdato gir valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Frilans(
                startdato = LocalDate.parse("2022-01-01"),
                sluttdato = LocalDate.parse("2021-01-01"),
                jobberFortsattSomFrilans = true
            ), 1, "frilans.sluttdato må være lik eller etter startdato."
        )
    }

    @Test
    fun `Frilans hvor jobberFortsattSomFrilans er false uten sluttdato gir valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Frilans(
                startdato = LocalDate.parse("2022-01-01"),
                sluttdato = null,
                jobberFortsattSomFrilans = false
            ), 1, "frilans.sluttdato kan ikke være null dersom jobberFortsattSomFrilans=false."
        )
    }
}
