package no.nav.brukerdialog.ytelse.pleiepengerilivetssluttfase.api.domene

import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.Pleietrengende
import no.nav.k9.søknad.JsonUtils
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.ÅrsakManglerIdentitetsnummer.BOR_I_UTLANDET
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
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
        Validator.verifiserIngenValideringsFeil(Pleietrengende(navn = "Ole", norskIdentitetsnummer = "06098523047"))
    }

    @Test
    fun `Blankt navn skal gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Pleietrengende(navn = " ", norskIdentitetsnummer = "06098523047"),
            1,
            "Kan ikke være tomt eller blankt"
        )
    }

    @Test
    fun `Fødselsdato i fremtiden skal gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Pleietrengende(
                navn = "Ole",
                fødselsdato = LocalDate.now().plusDays(1),
                årsakManglerIdentitetsnummer = BOR_I_UTLANDET
            ), 1, "Kan ikke være i fremtiden"
        )
    }

    @Test
    fun `NorskIdentitetsnummer og årsak som null skal gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Pleietrengende(
                navn = "Ole",
                fødselsdato = LocalDate.now(),
                norskIdentitetsnummer = null,
                årsakManglerIdentitetsnummer = null
            ),
            1,
            "'ÅrsakManglerIdentitetsnummer' må være satt dersom 'norskIdentitetsnummer' er null"
        )
    }

    @Test
    fun `NorskIdentitetsnummer og fødselsdato som null skal gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Pleietrengende(
                navn = "Ole",
                fødselsdato = null,
                norskIdentitetsnummer = null,
                årsakManglerIdentitetsnummer = BOR_I_UTLANDET
            ), 1, "'Fødselsdato' må være satt dersom 'norskIdentitetsnummer' er null"
        )
    }

    @Test
    fun `Ugyldig norskIdentitetsnummer skal gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Pleietrengende(navn = "Ole", norskIdentitetsnummer = "IKKE_GYLDIG"),
            1,
            "'IKKE_GYLDIG' matcher ikke tillatt pattern '^\\d+$'"
        )
    }
}
