package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutvidetrett.domene

import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Barn
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutvidetrett.SøknadUtils.defaultSøknad
import no.nav.k9brukerdialogprosessering.utils.StringUtils.FritekstPattern
import no.nav.k9brukerdialogprosessering.utils.SøknadUtils.Companion.metadata
import no.nav.k9brukerdialogprosessering.utils.SøknadUtils.Companion.somJson
import no.nav.k9brukerdialogprosessering.utils.SøknadUtils.Companion.søker
import no.nav.k9brukerdialogprosessering.utils.TestUtils.Validator
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserValideringsFeil
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

class OmsorgspengerUtvidetRettSøknadTest {

    @Test
    fun `Validering skal ikke feile på gyldig søknad`() {
        defaultSøknad.copy(
            språk = "nb",
            kroniskEllerFunksjonshemming = true,
            barn = Barn(
                norskIdentifikator = "02119970078",
                navn = "Barn Barnesen"
            ),
            relasjonTilBarnet = SøkerBarnRelasjon.FAR,
            sammeAdresse = BarnSammeAdresse.JA,
            legeerklæring = listOf(),
            samværsavtale = listOf(),
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        ).valider()
    }

    @Test
    fun `Forvent valideringsfeil dersom parametere har ugyldig verdi`() {
        Validator.verifiserValideringsFeil(
            defaultSøknad.copy(
                harBekreftetOpplysninger = false,
                harForståttRettigheterOgPlikter = false,
                høyereRisikoForFravær = true,
                høyereRisikoForFraværBeskrivelse = "¨ er ikke tillatt",
            ),
            3,
            "Opplysningene må bekreftes for å sende inn søknad",
            "Må ha forstått rettigheter og plikter for å sende inn søknad",
            "Matcher ikke tillatt mønster: '$FritekstPattern'"
        )
    }

    @Test
    fun `Forvent valideringsfeil dersom høyereRisikoForFravær er true, men høyereRisikoForFraværBeskrivelse mangler`() {
        Validator.verifiserValideringsFeil(
            defaultSøknad.copy(
                høyereRisikoForFravær = true,
                høyereRisikoForFraværBeskrivelse = null
            ),
            1,
            "Dersom 'høyereRisikoForFravær' er true, må 'høyereRisikoForFraværBeskrivelse' være satt"
        )
    }

    @Test
    fun `Mapping av k9format blir som forventet`() {
        val søknad = defaultSøknad
        val faktiskK9Format = JSONObject(søknad.somK9Format(søker, metadata).somJson())
        //language=json
        val forventetK9Format = JSONObject(
            """
                {
                  "språk": "nb",
                  "mottattDato": "2020-01-02T03:04:05Z",
                  "søknadId": "${søknad.søknadId}",
                  "søker": {
                    "norskIdentitetsnummer": "02119970078"
                  },
                  "ytelse": {
                    "barn": {
                      "fødselsdato": null,
                      "norskIdentitetsnummer": "02119970078"
                    },
                    "kroniskEllerFunksjonshemming": true,
                    "type": "OMP_UTV_KS",
                    "dataBruktTilUtledning": {
                        "harBekreftetOpplysninger": true,
                        "harForståttRettigheterOgPlikter": true,
                        "soknadDialogCommitSha": "abc-123",
                        "annetData": "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
                    }
                  },
                  "journalposter": [],
                  "begrunnelseForInnsending": {
                    "tekst": null
                  },
                  "versjon": "1.0.0",
                  "kildesystem": "søknadsdialog"
                }
            """.trimIndent()
        )
        JSONAssert.assertEquals(forventetK9Format, faktiskK9Format, true)
    }
}
