package no.nav.brukerdialog.api.ytelse.ettersending.domene

import no.nav.k9.ettersendelse.EttersendelseType
import no.nav.brukerdialog.api.ytelse.ettersending.EttersendelseUtils.defaultEttersendelse
import no.nav.brukerdialog.utils.SøknadUtils.Companion.metadata
import no.nav.brukerdialog.utils.SøknadUtils.Companion.somJson
import no.nav.brukerdialog.utils.SøknadUtils.Companion.søker
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

class EttersendingSøknadTest {

    @Test
    fun `Mapping av K9Format blir som forventet`() {
        val søknad = defaultEttersendelse
        val forventetK9Format = """
            {
              "søknadId": "${søknad.søknadId}",
              "versjon": "0.0.1",
              "mottattDato": "2020-01-02T03:04:05Z",
              "søker": {
                "norskIdentitetsnummer": "02119970078"
              },
              "type": "LEGEERKLÆRING",
              "pleietrengende": {
                "norskIdentitetsnummer": "02119970078"
              },
              "ytelse": "PLEIEPENGER_LIVETS_SLUTTFASE"
            }
        """.trimIndent()
        val faktiskK9Format = søknad.somK9Format(søker, metadata).somJson()
        JSONAssert.assertEquals(forventetK9Format, faktiskK9Format, true)

    }

    @Test
    fun `Gyldig søknad gir ingen valideringsfeil`() {
        Validator.verifiserIngenValideringsFeil(defaultEttersendelse)
    }

    @Test
    fun `Forventer valideringsfeil dersom ettersendelse inneholder feil parametere`() {
        Validator.verifiserValideringsFeil(
            defaultEttersendelse.copy(
                søknadId = "123ABC",
                vedlegg = listOf(),
                harForståttRettigheterOgPlikter = false,
                harBekreftetOpplysninger = false,
                ettersendelsesType = EttersendelseType.LEGEERKLÆRING,
                pleietrengende = null,
            ),
            5,
            "Forventet gyldig UUID, men var '123ABC'",
            "Kan ikke være tom",
            "Pleietrengende må være satt dersom ettersendelsen gjelder legeerklæring",
            "Opplysningene må bekreftes for å sende inn ettersendelse",
            "Må ha forstått rettigheter og plikter for å sende inn ettersendelse",
        )
    }
}
