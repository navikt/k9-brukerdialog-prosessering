package no.nav.brukerdialog.omsorgspengermidlertidigalene.api.domene

import no.nav.brukerdialog.api.ytelse.fellesdomene.Barn
import no.nav.brukerdialog.omsorgspengermidlertidigalene.utils.SøknadUtils
import no.nav.brukerdialog.utils.SøknadUtils.Companion.metadata
import no.nav.brukerdialog.utils.SøknadUtils.Companion.somJson
import no.nav.brukerdialog.utils.SøknadUtils.Companion.søker
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class OmsorgspengerMidlertidigAleneSøknadTest {
    @Test
    fun `Forventer valideringsfeil søknaden inneholder ugyldige parametere`() {
        Validator.verifiserValideringsFeil(
            SøknadUtils.defaultSøknad.copy(
                annenForelder = AnnenForelder(
                    navn = "",
                    fnr = "123ABC",
                    situasjon = Situasjon.INNLAGT_I_HELSEINSTITUSJON,
                    situasjonBeskrivelse = "",
                    periodeOver6Måneder = null,
                    periodeFraOgMed = LocalDate.parse("2020-01-01"),
                    periodeTilOgMed = null
                ),
                barn = listOf(
                    Barn(
                        navn = "Ole Dole",
                        norskIdentifikator = "25058118020",
                        aktørId = null
                    )
                ),
                harBekreftetOpplysninger = false,
                harForståttRettigheterOgPlikter = false
            ),
            6,
            "'123ABC' matcher ikke tillatt pattern '^\\d+$'",
            "size must be between 11 and 11",
            "Derom 'situasjon' er 'INNLAGT_I_HELSEINSTITUSJON', 'SYKDOM', eller 'ANNET' må 'periodeTilOgMed' eller 'periodeOver6Måneder' være satt",
            "Kan ikke være tomt eller blankt",
            "Må ha forstått rettigheter og plikter for å sende inn søknad",
            "Opplysningene må bekreftes for å sende inn søknad"
        )
    }

    @Test
    fun `K9Format blir som forventet`() {
        val søknad = SøknadUtils.defaultSøknad
        val faktiskK9Format = søknad.somK9Format(søker, metadata).somJson()
        val forventetK9Format =
            //language=json
            """
            {
              "søknadId": ${søknad.søknadId},
              "versjon": "1.0.0",
              "mottattDato": "2020-01-02T03:04:05Z",
              "søker": {
                "norskIdentitetsnummer": "02119970078"
              },
              "språk": "nb",
              "ytelse": {
                "type": "OMP_UTV_MA",
                "barn": [
                  {
                    "norskIdentitetsnummer": "25058118020",
                    "fødselsdato": null
                  }
                ],
                "annenForelder": {
                  "norskIdentitetsnummer": "02119970078",
                  "situasjon": "FENGSEL",
                  "situasjonBeskrivelse": "Sitter i fengsel..",
                  "periode": "2020-01-01/2020-10-01"
                },
                "begrunnelse": null,
                "dataBruktTilUtledning": {
                    "harBekreftetOpplysninger": true,
                    "harForståttRettigheterOgPlikter": true,
                    "soknadDialogCommitSha": "abc-123",
                    "annetData": "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
                }
              },
              "begrunnelseForInnsending" : {
                "tekst" : null
              },
              "journalposter": [],
              "kildesystem": "søknadsdialog"
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventetK9Format, faktiskK9Format, true)
    }
}

