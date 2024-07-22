package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.JsonUtils
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.ÅrsakManglerIdentitetsnummer.BOR_I_UTLANDET
import no.nav.k9brukerdialogprosessering.utils.TestUtils.VALIDATOR
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserFeil
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserIngenFeil
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class PleietrengendeTest {


    @Test
    fun `Mapping til K9Pleietrengende blir som forventet`() {
        Pleietrengende(navn = "Ole", norskIdentitetsnummer = "06098523047")
            .somK9Pleietrengende().also {
                JSONAssert.assertEquals(
                    """{"norskIdentitetsnummer":"06098523047","fødselsdato":null}""",
                    JsonUtils.toString(it),
                    true
                )
            }

        Pleietrengende(navn = "Ole", fødselsdato = LocalDate.parse("2022-01-01"))
            .somK9Pleietrengende().also {
                JSONAssert.assertEquals(
                    """{"norskIdentitetsnummer":null,"fødselsdato":"2022-01-01"}""",
                    JsonUtils.toString(it),
                    true
                )
            }
    }

    @Test
    fun `Gyldig pleietrengende gir ingen valideringsfeil`() {
        VALIDATOR.validate(Pleietrengende(navn = "Ole", norskIdentitetsnummer = "06098523047"))
            .verifiserIngenFeil()
    }

    @Test
    fun `Blankt navn skal gi valideringsfeil`() {
        VALIDATOR.validate(Pleietrengende(navn = " ", norskIdentitetsnummer = "06098523047"))
            .verifiserFeil(1, "Kan ikke være tomt eller blankt")
    }

    @Test
    fun `Fødselsdato i fremtiden skal gi valideringsfeil`() {
        VALIDATOR.validate(
            Pleietrengende(
                navn = "Ole",
                fødselsdato = LocalDate.now().plusDays(1),
                årsakManglerIdentitetsnummer = BOR_I_UTLANDET
            )
        )
            .verifiserFeil(1, "Kan ikke være i fremtiden")
    }

    @Test
    fun `NorskIdentitetsnummer og årsak som null skal gi valideringsfeil`() {
        VALIDATOR.validate(
            Pleietrengende(
                navn = "Ole",
                fødselsdato = LocalDate.now(),
                norskIdentitetsnummer = null,
                årsakManglerIdentitetsnummer = null
            )
        )
            .verifiserFeil(
                1,
                "'ÅrsakManglerIdentitetsnummer' må være satt dersom 'norskIdentitetsnummer' er null"
            )
    }

    @Test
    fun `NorskIdentitetsnummer og fødselsdato som null skal gi valideringsfeil`() {
        VALIDATOR.validate(
            Pleietrengende(
                navn = "Ole",
                fødselsdato = null,
                norskIdentitetsnummer = null,
                årsakManglerIdentitetsnummer = BOR_I_UTLANDET
            )
        )
            .verifiserFeil(1, "'Fødselsdato' må være satt dersom 'norskIdentitetsnummer' er null")
    }

    @Test
    fun `Ugyldig norskIdentitetsnummer skal gi valideringsfeil`() {
        VALIDATOR.validate(Pleietrengende(navn = "Ole", norskIdentitetsnummer = "IKKE_GYLDIG"))
            .verifiserFeil(
                1,
                "'IKKE_GYLDIG' matcher ikke tillatt pattern '^\\d+$'"
            )
    }
}
