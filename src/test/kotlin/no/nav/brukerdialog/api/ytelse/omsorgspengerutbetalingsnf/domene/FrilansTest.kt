package no.nav.k9brukerdialogapi.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9.søknad.JsonUtils
import no.nav.brukerdialog.api.ytelse.omsorgspengerutbetalingsnf.domene.Frilans
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
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
            ), 2, "Dersom 'jobberFortsattSomFrilans' er true, kan ikke 'sluttdato' være satt",
            "'Sluttdato' må være lik eller etter 'startdato'"
        )
    }

    @Test
    fun `Frilans hvor jobberFortsattSomFrilans er false uten sluttdato gir valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Frilans(
                startdato = LocalDate.parse("2022-01-01"),
                sluttdato = null,
                jobberFortsattSomFrilans = false
            ), 1, "Dersom 'jobberFortsattSomFrilans' er false, må 'sluttdato' være satt"
        )
    }
}
