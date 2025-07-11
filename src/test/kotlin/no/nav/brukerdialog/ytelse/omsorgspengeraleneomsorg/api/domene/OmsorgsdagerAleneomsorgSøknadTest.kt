package no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene

import no.nav.k9.søknad.JsonUtils
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.utils.SøknadUtils
import no.nav.brukerdialog.oppslag.barn.BarnOppslag
import no.nav.brukerdialog.utils.SøknadUtils.Companion.metadata
import no.nav.brukerdialog.utils.SøknadUtils.Companion.somJson
import no.nav.brukerdialog.utils.SøknadUtils.Companion.søker
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class OmsorgsdagerAleneomsorgSøknadTest {

    @Test
    fun `Oppdatering av identitetsnummer på barn fungerer`() {
        val søknad = SøknadUtils.defaultSøknad.copy(
            barn = listOf(
                Barn(
                    navn = "Barn1",
                    type = TypeBarn.FRA_OPPSLAG,
                    aktørId = "123",
                    identitetsnummer = null,
                    tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
                ),
                Barn(
                    navn = "Barn2",
                    type = TypeBarn.FRA_OPPSLAG,
                    aktørId = "1234",
                    identitetsnummer = null,
                    tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
                )
            ),
        )

        assertTrue(søknad.manglerIdentifikatorPåBarn())
        val barnFraOppslag = listOf(
            BarnOppslag(
                fødselsdato = LocalDate.now(),
                fornavn = "Barn1",
                mellomnavn = null,
                etternavn = "Barnesen",
                aktørId = "123",
                identitetsnummer = "25058118020"
            ),
            BarnOppslag(
                fødselsdato = LocalDate.now(),
                fornavn = "Barn2",
                mellomnavn = null,
                etternavn = "Barnesen",
                aktørId = "1234",
                identitetsnummer = "02119970078"
            )
        )
        søknad.leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag)
        assertFalse(søknad.manglerIdentifikatorPåBarn())
    }

    @Test
    fun `Søknad med to barn blir splittet opp i to ulike søknader per barn`() {
        val barn1 = Barn(
            navn = "Barn1",
            type = TypeBarn.FRA_OPPSLAG,
            aktørId = "123",
            identitetsnummer = "12345",
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
        )
        val barn2 = Barn(
            navn = "Barn2",
            type = TypeBarn.FRA_OPPSLAG,
            aktørId = "321",
            identitetsnummer = "54321",
            tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
        )

        val søknad = SøknadUtils.defaultSøknad.copy(barn = listOf(barn1, barn2))

        //Mapping til k9Format skal ikke fungerer før split pga flere barn i lista
        assertThrows<IllegalArgumentException> { søknad.somK9Format(søker, metadata) }

        val split = søknad.splittTilEgenSøknadPerBarn()
        assertTrue(split.size == 2)

        val k9FormatSplit = split.map { it.somK9Format(søker, metadata).somJson() }

        val søknad1 = JSONObject(k9FormatSplit.first())
        val k9Barn1 = JsonUtils.toString(barn1.somK9Barn())
        JSONAssert.assertEquals(søknad1.getJSONObject("ytelse").getJSONObject("barn").toString(), k9Barn1, true)

        val søknad2 = JSONObject(k9FormatSplit.last())
        val k9Barn2 = JsonUtils.toString(barn2.somK9Barn())
        JSONAssert.assertEquals(søknad2.getJSONObject("ytelse").getJSONObject("barn").toString(), k9Barn2, true)
    }

    @Test
    fun `Mapping av K9Format blir som forventet`() {
        val søknad = SøknadUtils.defaultSøknad.copy(
            barn = listOf(
                Barn(
                    navn = "Barn1",
                    type = TypeBarn.FRA_OPPSLAG,
                    aktørId = "123",
                    identitetsnummer = "25058118020",
                    tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
                )
            )
        )

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
                  "ytelse": {
                    "type": "OMP_UTV_AO",
                    "barn": {
                      "norskIdentitetsnummer": "25058118020",
                      "fødselsdato": null
                    },
                    "periode": "2024-01-01/..",
                    "dataBruktTilUtledning": {
                        "harBekreftetOpplysninger": true,
                        "harForståttRettigheterOgPlikter": true,
                        "soknadDialogCommitSha": "abc-123",
                        "annetData": "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
                    }
                  },
                  "språk": "nb",
                  "journalposter": [],
                  "begrunnelseForInnsending": {
                    "tekst": null
                  },
                  "kildesystem": "søknadsdialog"
                }
        """.trimIndent()
        JSONAssert.assertEquals(forventetK9Format, faktiskK9Format, true)
    }

    @Test
    fun `Gir valideringsfeil dersom Søknaden inneholder ugyldige parametere`() {
        Validator.verifiserValideringsFeil(
            SøknadUtils.defaultSøknad.copy(
                søknadId = "123ABC",
                barn = listOf(),
                harForståttRettigheterOgPlikter = false,
                harBekreftetOpplysninger = false
            ),
            4,
            "Forventet gyldig UUID, men var '123ABC'",
            "Kan ikke være en tom liste",
            "Opplysningene må bekreftes for å sende inn søknad",
            "Må ha forstått rettigheter og plikter for å sende inn søknad"
        )

        Validator.verifiserValideringsFeil(
            SøknadUtils.defaultSøknad.copy(
                barn = listOf(
                    Barn(
                        navn = "",
                        type = TypeBarn.FOSTERBARN,
                        fødselsdato = null,
                        aktørId = "123",
                        identitetsnummer = "ABC123",
                        tidspunktForAleneomsorg = TidspunktForAleneomsorg.SISTE_2_ÅRENE,
                        dato = null
                    ),
                    Barn(
                        navn = "barnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnb",
                        type = TypeBarn.FRA_OPPSLAG,
                        fødselsdato = LocalDate.now().plusDays(1),
                        aktørId = "123",
                        identitetsnummer = "25058118020",
                        tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE,
                        dato = null
                    )
                ),
            ),
            7,
            "Må være satt når 'tidspunktForAleneomsorg' er 'SISTE_2_ÅRENE'",
            "'ABC123' matcher ikke tillatt pattern '^\\d+$'",
            "Kan ikke være tomt eller blankt",
            "Må være satt når 'type' er annet enn 'FRA_OPPSLAG'",
            "Kan ikke være i fremtiden",
            "Kan ikke være mer enn 100 tegn",
            "size must be between 11 and 11"
        )
    }
}
