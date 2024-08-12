package no.nav.brukerdialog.meldinger.omsorgspengerutbetalingat

import io.mockk.coEvery
import io.mockk.coVerify
import no.nav.brukerdialog.AbstractIntegrationTest
import no.nav.brukerdialog.api.ytelse.omsorgspengerutbetalingarbeidstaker.SøknadUtils
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.config.JacksonConfiguration.Companion.zonedDateTimeFormatter
import no.nav.brukerdialog.dittnavvarsel.DittnavVarselTopologyConfiguration.Companion.K9_DITTNAV_VARSEL_TOPIC
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.meldinger.omsorgpengerutbetalingat.OMPUtbetalingATTopologyConfiguration.Companion.OMP_UTB_AT_CLEANUP_TOPIC
import no.nav.brukerdialog.meldinger.omsorgpengerutbetalingat.OMPUtbetalingATTopologyConfiguration.Companion.OMP_UTB_AT_MOTTATT_TOPIC
import no.nav.brukerdialog.meldinger.omsorgpengerutbetalingat.OMPUtbetalingATTopologyConfiguration.Companion.OMP_UTB_AT_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.meldinger.omsorgspengerutbetalingat.utils.OMPUtbetalingATSøknadUtils
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

class OMPUtbetalingATSøknadKonsumentTest : AbstractIntegrationTest() {

    override val consumerGroupPrefix = "omsorgspengerutbetaling-arbeidstaker"
    override val consumerGroupTopics = listOf(
        OMP_UTB_AT_MOTTATT_TOPIC, OMP_UTB_AT_PREPROSESSERT_TOPIC, OMP_UTB_AT_CLEANUP_TOPIC
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
                ytelse = "OMSORGSPENGER_UT_ARBEIDSTAKER",
            )
        )
    }

    @Test
    fun `Forvent at melding bli prosessert på 5 forsøk etter 4 feil`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15Z"
        val mottatt = ZonedDateTime.parse(mottattString, zonedDateTimeFormatter)
        val søknadMottatt = OMPUtbetalingATSøknadUtils.defaultSøknad(
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

        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = OMP_UTB_AT_MOTTATT_TOPIC)
        val lesMelding =
            consumer.lesMelding(key = søknadId, topic = OMP_UTB_AT_PREPROSESSERT_TOPIC).value()

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
            "mellomnavn": null,
            "aktørId": "123456",
            "fødselsdato": "1999-11-02",
            "fornavn": "Ola",
            "fødselsnummer": "02119970078"
          },
          "fosterbarn": [
            {
              "navn": "Fosterbarn 1",
              "identitetsnummer": "01011012345"
            },
            {
              "navn": "Fosterbarn 2",
              "identitetsnummer": "01011012234"
            }
          ],
          "dineBarn": {
            "harDeltBosted": true,
            "barn": [
              {
                "identitetsnummer": "01010101011",
                "aktørId": "01010101011",
                "fødselsdato": "2024-05-31",
                "navn": "Van Li Barnesen",
                "type": "FRA_OPPSLAG"
              },
              {
                "identitetsnummer": "02020202020",
                "aktørId": "02020202020",
                "fødselsdato": "2024-05-30",
                "navn": "Foster Barnesen",
                "type": "FOSTERBARN"
              },
              {
                "identitetsnummer": "02020202020",
                "aktørId": "02020202020",
                "fødselsdato": "2024-05-29",
                "navn": "Anna Barnesen",
                "type": "ANNET"
              }
            ]
          },
          "opphold": [
            {
              "fraOgMed": "2019-12-12",
              "landkode": "GB",
              "landnavn": "Great Britain",
              "tilOgMed": "2019-12-22",
              "erEØSLand": true
            }
          ],
          "titler": [
            "vedlegg1"
          ],
          "bosteder": [
            {
              "fraOgMed": "2019-12-12",
              "landkode": "GB",
              "landnavn": "Great Britain",
              "tilOgMed": "2019-12-22",
              "erEØSLand": true
            },
            {
              "fraOgMed": "2019-12-12",
              "landkode": "US",
              "landnavn": "USA",
              "tilOgMed": "2019-12-22",
              "erEØSLand": false
            }
          ],
          "språk": "nb",
          "arbeidsgivere": [
            {
              "perioder": [
                {
                  "årsak": "SMITTEVERNHENSYN",
                  "fraOgMed": "2020-01-01",
                  "antallTimerBorte": "PT8H",
                  "tilOgMed": "2020-01-11",
                  "antallTimerPlanlagt": "PT8H"
                },
                {
                  "årsak": null,
                  "fraOgMed": "2020-01-11",
                  "antallTimerBorte": "PT8H",
                  "tilOgMed": "2020-01-21",
                  "antallTimerPlanlagt": "PT8H"
                }
              ],
              "utbetalingsårsak": "KONFLIKT_MED_ARBEIDSGIVER",
              "harHattFraværHosArbeidsgiver": true,
              "navn": "Arbeidsgiver 1",
              "arbeidsgiverHarUtbetaltLønn": false,
              "konfliktForklaring": "Har en konflikt med arbeidsgiver fordi ....",
              "årsakNyoppstartet": null,
              "organisasjonsnummer": "917755736"
            },
            {
              "perioder": [
                {
                  "årsak": "ORDINÆRT_FRAVÆR",
                  "fraOgMed": "2020-01-21",
                  "antallTimerBorte": "PT8H",
                  "tilOgMed": "2020-01-21",
                  "antallTimerPlanlagt": "PT8H"
                }
              ],
              "utbetalingsårsak": "NYOPPSTARTET_HOS_ARBEIDSGIVER",
              "harHattFraværHosArbeidsgiver": true,
              "navn": "Arbeidsgiver 2",
              "arbeidsgiverHarUtbetaltLønn": false,
              "konfliktForklaring": null,
              "årsakNyoppstartet": "UTØVDE_VERNEPLIKT",
              "organisasjonsnummer": "917755736"
            },
            {
              "perioder": [
                {
                  "årsak": "ORDINÆRT_FRAVÆR",
                  "fraOgMed": "2020-02-01",
                  "antallTimerBorte": null,
                  "tilOgMed": "2020-02-06",
                  "antallTimerPlanlagt": null
                }
              ],
              "utbetalingsårsak": "NYOPPSTARTET_HOS_ARBEIDSGIVER",
              "harHattFraværHosArbeidsgiver": true,
              "navn": "Arbeidsgiver 3",
              "arbeidsgiverHarUtbetaltLønn": false,
              "konfliktForklaring": null,
              "årsakNyoppstartet": "SØKTE_ANDRE_UTBETALINGER",
              "organisasjonsnummer": "917755736"
            }
          ],
          
          "dokumentId": [
            [
              "123456789",
              "987654321"
            ],
            [
              "1234"
            ],
            [
              "5678"
            ]
          ],
          "hjemmePgaStengtBhgSkole": true,
          "hjemmePgaSmittevernhensyn": true,
          "bekreftelser": {
            "harForståttRettigheterOgPlikter": true,
            "harBekreftetOpplysninger": true
          },
          "k9Format": {
            "språk": "nb",
            "kildesystem": null,
            "mottattDato": "$mottatt",
            "søknadId": "$søknadId",
            "søker": {
              "norskIdentitetsnummer": "02119970078"
            },
            "ytelse": {
              "utenlandsopphold": {
                "perioder": {
                  "2020-01-01\/2020-01-10": {
                    "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd",
                    "land": "ESP",
                    "erSammenMedBarnet": true
                  }
                },
                "perioderSomSkalSlettes": {}
              },
              "fraværsperioderKorrigeringIm": null,
              "fosterbarn": [
                {
                  "fødselsdato": null,
                  "norskIdentitetsnummer": "26128027024"
                }
              ],
              "aktivitet": null,
              "fraværsperioder": [
                {
                  "duration": "PT7H30M",
                  "årsak": "STENGT_SKOLE_ELLER_BARNEHAGE",
                  "aktivitetFravær": [
                    "ARBEIDSTAKER"
                  ],
                  "delvisFravær": null,
                  "arbeidsforholdId": null,
                  "søknadÅrsak": "KONFLIKT_MED_ARBEIDSGIVER",
                  "periode": "2020-01-01\/2020-01-10",
                  "arbeidsgiverOrgNr": "917755736"
                }
              ],
              "type": "OMP_UT",
              "bosteder": {
                "perioder": {
                  "2020-01-01\/2020-01-10": {
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
            "versjon": "1.0.0"
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
