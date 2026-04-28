package no.nav.brukerdialog.ytelse.omsorgspengerutbetalingsnf.api.domene

import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene.Barn
import no.nav.k9.søknad.JsonUtils
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene.Barn.Companion.somK9BarnListe
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene.TypeBarn
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class FosterbarnTest {

    @Test
    fun `Barn blir til forventet K9Barn`() {
        val k9Barn = Barn(
            navn = "Barnesen",
            fødselsdato = LocalDate.parse("2022-01-01"),
            type = TypeBarn.FRA_OPPSLAG,
            identitetsnummer = "17420373147"
        ).somK9Barn()
        val forventetK9Barn =
            """
                {
                    "fødselsdato" :null,
                    "norskIdentitetsnummer":"17420373147"
                }
            """.trimIndent()

        JSONAssert.assertEquals(forventetK9Barn, JsonUtils.toString(k9Barn), true)
    }

    @Test
    fun `Liste med barn blir til forventet K9BarnListe hvor kun barn fra oppslag blir mappet opp`() {
        val barn = listOf(
            Barn(
                navn = "Barnesen",
                fødselsdato = LocalDate.parse("2022-01-01"),
                type = TypeBarn.FOSTERBARN,
                identitetsnummer = "17420373147"
            ),
            Barn(
                navn = "Barnesen v2",
                fødselsdato = LocalDate.parse("2022-01-01"),
                type = TypeBarn.FOSTERBARN,
                identitetsnummer = "01010010006"
            ),
            Barn(
                navn = "Barnesen v2",
                fødselsdato = LocalDate.parse("2022-01-01"),
                type = TypeBarn.ANNET,
                identitetsnummer = "01010010008"
            )
        )
        val forventetK9Barn =
            """
                [{
                    "fødselsdato" :null,
                    "norskIdentitetsnummer":"17420373147"
                },
                {
                    "fødselsdato" :null,
                    "norskIdentitetsnummer":"01010010006"
                }]
            """.trimIndent()

        JSONAssert.assertEquals(forventetK9Barn, JsonUtils.toString(barn.somK9BarnListe()), true)
    }

    @Test
    fun `Gyldig barn gir ingen valideringsfeil`() {
        Validator.verifiserIngenValideringsFeil(
            Barn(
                navn = "Barnesen",
                fødselsdato = LocalDate.parse("2022-01-01"),
                type = TypeBarn.FRA_OPPSLAG,
                aktørId = null,
                identitetsnummer = "17420373147"
            )
        )
    }

    @Test
    fun `Barn uten fødselsnummer gir valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Barn(
                navn = "Barnesen",
                fødselsdato = LocalDate.parse("2022-01-01"),
                type = TypeBarn.FOSTERBARN,
                aktørId = null,
                identitetsnummer = null
            ), 1, "Kan ikke være null når 'type' er annet enn 'FRA_OPPSLAG'"
        )
    }

    @Test
    fun `Barn med blankt navn gir valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Barn(
                navn = " ",
                fødselsdato = LocalDate.parse("2022-01-01"),
                type = TypeBarn.FRA_OPPSLAG,
                aktørId = null,
                identitetsnummer = "17420373147"
            ), 1, "Kan ikke være tomt eller blankt"
        )
    }
}
