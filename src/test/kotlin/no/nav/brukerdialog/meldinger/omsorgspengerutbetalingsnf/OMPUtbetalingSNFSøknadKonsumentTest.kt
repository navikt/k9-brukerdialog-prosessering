package no.nav.brukerdialog.meldinger.omsorgspengerutbetalingsnf

import io.mockk.coEvery
import io.mockk.coVerify
import no.nav.brukerdialog.AbstractIntegrationTest
import no.nav.brukerdialog.api.ytelse.omsorgspengerutbetalingsnf.SøknadUtils
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.config.JacksonConfiguration.Companion.zonedDateTimeFormatter
import no.nav.brukerdialog.dittnavvarsel.DittnavVarselTopologyConfiguration.Companion.K9_DITTNAV_VARSEL_TOPIC
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.meldinger.omsorgpengerutbetalingsnf.OMPUtbetalingSNFTopologyConfiguration.Companion.OMP_UTB_SNF_CLEANUP_TOPIC
import no.nav.brukerdialog.meldinger.omsorgpengerutbetalingsnf.OMPUtbetalingSNFTopologyConfiguration.Companion.OMP_UTB_SNF_MOTTATT_TOPIC
import no.nav.brukerdialog.meldinger.omsorgpengerutbetalingsnf.OMPUtbetalingSNFTopologyConfiguration.Companion.OMP_UTB_SNF_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.meldinger.omsorgspengerutbetalingsnf.utils.OMPUtbetalingSNFSøknadUtils
import no.nav.brukerdialog.utils.KafkaUtils.leggPåTopic
import no.nav.brukerdialog.utils.KafkaUtils.lesMelding
import no.nav.brukerdialog.utils.MockMvcUtils.sendInnSøknad
import no.nav.brukerdialog.utils.SøknadUtils.Companion.metadata
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.net.URI
import java.time.ZonedDateTime
import java.util.*

class OMPUtbetalingSNFSøknadKonsumentTest : AbstractIntegrationTest() {

    override val consumerGroupPrefix = "omsorgspengerutbetaling-snf"
    override val consumerGroupTopics = listOf(
        OMP_UTB_SNF_MOTTATT_TOPIC, OMP_UTB_SNF_PREPROSESSERT_TOPIC, OMP_UTB_SNF_CLEANUP_TOPIC
    )

    @Test
    fun `forvent at melding konsumeres riktig og dokumenter blir slettet`() {
        val søker = mockSøker()
        mockBarn()
        mockLagreDokument()
        mockJournalføring()

        val søknadId = UUID.randomUUID().toString()
        mockMvc.sendInnSøknad(SøknadUtils.defaultSøknad.copy(søknadId = søknadId), mockOAuth2Server.hentToken())

        coVerify(exactly = 1, timeout = 120 * 1000) {
            k9DokumentMellomlagringService.slettDokumenter(any(), any())
        }

        k9DittnavVarselConsumer.lesMelding(
            key = søknadId,
            topic = K9_DITTNAV_VARSEL_TOPIC
        ).value().assertDittnavVarsel(
            K9Beskjed(
                metadata = metadata,
                grupperingsId = søknadId,
                tekst = "Søknad om utbetaling av omsorgspenger er mottatt.",
                link = null,
                dagerSynlig = 7,
                søkerFødselsnummer = søker.fødselsnummer,
                eventId = "testes ikke",
                ytelse = "OMSORGSPENGER_UT_SNF",
            )
        )
    }

    @Test
    fun `Forvent at melding bli prosessert på 5 forsøk etter 4 feil`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15Z"
        val mottatt = ZonedDateTime.parse(mottattString, zonedDateTimeFormatter)
        val søknadMottatt = OMPUtbetalingSNFSøknadUtils.defaultSøknad(
            søknadId = søknadId,
            mottatt = mottatt
        )
        val correlationId = UUID.randomUUID().toString()
        val metadata = MetaInfo(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, søknadMottatt)
        val topicEntryJson = objectMapper.writeValueAsString(topicEntry)

        coEvery { k9DokumentMellomlagringService.lagreDokument(any()) }
            .throws(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenMany(listOf("123456789", "987654321").map { URI("http://localhost:8080/dokument/$it") })

        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = OMP_UTB_SNF_MOTTATT_TOPIC)
        val lesMelding =
            consumer.lesMelding(key = søknadId, topic = OMP_UTB_SNF_PREPROSESSERT_TOPIC).value()

        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
        JSONAssert.assertEquals(preprosessertSøknadSomJson(søknadId, mottattString), preprosessertSøknadJson, true)
    }

    @Language("JSON")
    private fun preprosessertSøknadSomJson(søknadId: String, mottatt: String) = """
       {
          "soknadId": "$søknadId",
          "mottatt": "$mottatt",
          "søker": {
            "etternavn": "Nordmann",
            "mellomnavn": "Mellomnavn",
            "aktørId": "123456",
            "fødselsdato": "2020-08-02",
            "fornavn": "Ola",
            "fødselsnummer": "02119970078"
          },
          "harDekketTiFørsteDagerSelv": true,
          "harSyktBarn": true,
          "harAleneomsorg": true,
          "opphold": [
            {
              "fraOgMed": "2020-01-16",
              "landkode": "Eng",
              "landnavn": "England",
              "tilOgMed": "2020-01-21",
              "erEØSLand": true
            },
            {
              "fraOgMed": "2019-12-22",
              "landkode": "CRO",
              "landnavn": "Kroatia",
              "tilOgMed": "2019-12-27",
              "erEØSLand": true
            }
          ],
          "selvstendigNæringsdrivende": {
            "varigEndring": {
              "forklaring": "Forklaring som handler om varig endring",
              "dato": "2023-04-09",
              "inntektEtterEndring": 234543
            },
            "harFlereAktiveVirksomheter": true,
            "næringsinntekt": 123456789,
            "fraOgMed": "2023-04-29",
            "erNyoppstartet": true,
            "yrkesaktivSisteTreFerdigliknedeÅrene": null,
            "næringstype": "FISKE",
            "tilOgMed": "2023-05-09",
            "fiskerErPåBladB": false,
            "navnPåVirksomheten": "Kjells Møbelsnekkeri",
            "registrertINorge": true,
            "organisasjonsnummer": "111111",
            "registrertIUtlandet": null,
            "regnskapsfører": null
          },
          "frilans": {
            "startdato": "2022-01-01",
            "sluttdato": "2022-10-01",
            "jobberFortsattSomFrilans": false
          },
          "bosteder": [
            {
              "fraOgMed": "2020-01-01",
              "landkode": "SWE",
              "landnavn": "Sverige",
              "tilOgMed": "2020-01-06",
              "erEØSLand": true
            },
            {
              "fraOgMed": "2020-01-11",
              "landkode": "NOR",
              "landnavn": "Norge",
              "tilOgMed": "2020-01-11",
              "erEØSLand": true
            }
          ],
          "utbetalingsperioder": [
            {
              "årsak": "ORDINÆRT_FRAVÆR",
              "aktivitetFravær": [
                "FRILANSER"
              ],
              "fraOgMed": "2020-01-01",
              "antallTimerBorte": null,
              "tilOgMed": "2020-01-11",
              "antallTimerPlanlagt": null
            },
            {
              "årsak": "SMITTEVERNHENSYN",
              "aktivitetFravær": [
                "SELVSTENDIG_VIRKSOMHET"
              ],
              "fraOgMed": "2020-01-21",
              "antallTimerBorte": null,
              "tilOgMed": "2020-01-21",
              "antallTimerPlanlagt": null
            },
            {
              "årsak": "STENGT_SKOLE_ELLER_BARNEHAGE",
              "aktivitetFravær": [
                "FRILANSER",
                "SELVSTENDIG_VIRKSOMHET"
              ],
              "fraOgMed": "2020-01-31",
              "antallTimerBorte": null,
              "tilOgMed": "2020-02-05",
              "antallTimerPlanlagt": null
            }
          ],
          "språk": "nb",
          "barn": [
           {
             "aktørId": null,
             "fødselsdato": "2020-01-01",
             "identitetsnummer": "1234",
             "navn": "Barn 1 Barnesen",
             "type": "FOSTERBARN"
           },
           {
             "aktørId": "123456789",
             "fødselsdato": "2019-01-01",
             "identitetsnummer": "5677",
             "navn": "Barn 2 Barnesen",
             "type": "FRA_OPPSLAG"
           },
           {
             "aktørId": null,
             "fødselsdato": "2018-01-01",
             "identitetsnummer": "8888",
             "navn": "Barn 3 Barnesen",
             "type": "ANNET"
           }
          ],
          "spørsmål": [
            {
              "svar": true,
              "spørsmål": "Har du vært hjemme?"
            },
            {
              "svar": false,
              "spørsmål": "Skal du være hjemme?"
            }
          ],
          "dokumentId": [
            [
              "123456789",
              "987654321"
            ],
            [
              "123"
            ],
            [
              "456"
            ],
            [
              "789"
            ]
          ],
          "bekreftelser": {
            "harForståttRettigheterOgPlikter": true,
            "harBekreftetOpplysninger": true
          },
          "k9FormatSøknad": {
            "språk": "nb",
            "kildesystem": "søknadsdialog",
            "mottattDato": "$mottatt",
            "søknadId": "$søknadId",
            "søker": {
              "norskIdentitetsnummer": "12345678910"
            },
            "ytelse": {
              "utenlandsopphold": {
                "perioder": {
                  "2020-01-01\/2020-01-05": {
                    "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd",
                    "land": "CAN",
                    "erSammenMedBarnet": true
                  },
                  "2020-01-06\/2020-01-10": {
                    "årsak": "barnetInnlagtIHelseinstitusjonForNorskOffentligRegning",
                    "land": "SWE",
                    "erSammenMedBarnet": true
                  }
                },
                "perioderSomSkalSlettes": {}
              },
              "fraværsperioderKorrigeringIm": null,
              "fosterbarn": [
                {
                  "fødselsdato": null,
                  "norskIdentitetsnummer": "10987654321"
                }
              ],
              "aktivitet": {
                "selvstendigNæringsdrivende": [
                  {
                    "perioder": {
                      "2018-01-01\/2020-01-01": {
                        "regnskapsførerNavn": "Regnskapsfører Svensen",
                        "erNyoppstartet": true,
                        "landkode": "NOR",
                        "erVarigEndring": true,
                        "regnskapsførerTlf": "+4799887766",
                        "endringBegrunnelse": "Grunnet Covid-19",
                        "endringDato": "2020-01-01",
                        "erNyIArbeidslivet": true,
                        "virksomhetstyper": [
                          "DAGMAMMA"
                        ],
                        "bruttoInntekt": 5000000,
                        "registrertIUtlandet": false
                      }
                    },
                    "virksomhetNavn": "Mamsen Bamsen AS",
                    "organisasjonsnummer": "12345678910112233444455667"
                  }
                ],
                "frilanser": {
                  "startdato": "2020-01-01",
                  "sluttdato": null
                }
              },
              "fraværsperioder": [
                {
                  "duration": "PT7H",
                  "årsak": "STENGT_SKOLE_ELLER_BARNEHAGE",
                  "aktivitetFravær": [
                    "FRILANSER"
                  ],
                  "delvisFravær": null,
                  "arbeidsforholdId": null,
                  "søknadÅrsak": null,
                  "periode": "2020-01-01\/2020-01-05",
                  "arbeidsgiverOrgNr": null
                },
                {
                  "duration": "PT4H",
                  "årsak": "SMITTEVERNHENSYN",
                  "aktivitetFravær": [
                    "SELVSTENDIG_VIRKSOMHET"
                  ],
                  "delvisFravær": null,
                  "arbeidsforholdId": null,
                  "søknadÅrsak": null,
                  "periode": "2020-01-06\/2020-01-10",
                  "arbeidsgiverOrgNr": null
                }
              ],
              "type": "OMP_UT",
              "bosteder": {
                "perioder": {
                  "2020-01-01\/2020-01-05": {
                    "land": "ESP"
                  },
                  "2020-01-06\/2020-01-10": {
                    "land": "NOR"
                  }
                },
                "perioderSomSkalSlettes": {}
              },
              "dataBruktTilUtledning": null
            },
            "journalposter": [],
            "begrunnelseForInnsending": {
              "tekst": null
            },
            "versjon": "1.1"
          }
        }
        """.trimIndent()

}

private fun String.assertDittnavVarsel(k9Beskjed: K9Beskjed) {
    val k9BeskjedJson = JSONObject(this)
    assertEquals(k9Beskjed.grupperingsId, k9BeskjedJson.getString("grupperingsId"))
    assertEquals(k9Beskjed.tekst, k9BeskjedJson.getString("tekst"))
    assertEquals(k9Beskjed.ytelse, k9BeskjedJson.getString("ytelse"))
    assertEquals(k9Beskjed.dagerSynlig, k9BeskjedJson.getLong("dagerSynlig"))
}
