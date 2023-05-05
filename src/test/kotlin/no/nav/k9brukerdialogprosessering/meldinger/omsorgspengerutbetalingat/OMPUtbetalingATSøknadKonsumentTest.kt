package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerutbetalingat

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.k9brukerdialogprosessering.dittnavvarsel.DittnavVarselTopologyConfiguration
import no.nav.k9brukerdialogprosessering.dittnavvarsel.K9Beskjed
import no.nav.k9brukerdialogprosessering.journalforing.JournalføringsResponse
import no.nav.k9brukerdialogprosessering.journalforing.K9JoarkService
import no.nav.k9brukerdialogprosessering.kafka.types.Metadata
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingat.OMPUtbetalingATTopologyConfiguration.Companion.OMP_UTB_AT_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingat.OMPUtbetalingATTopologyConfiguration.Companion.OMP_UTB_AT_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingat.OMPUtbetalingATTopologyConfiguration.Companion.OMP_UTB_AT_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerutbetalingat.utils.OMPUtbetalingATSøknadUtils
import no.nav.k9brukerdialogprosessering.mellomlagring.K9MellomlagringService
import no.nav.k9brukerdialogprosessering.utils.KafkaIntegrationTest
import no.nav.k9brukerdialogprosessering.utils.KafkaUtils.leggPåTopic
import no.nav.k9brukerdialogprosessering.utils.KafkaUtils.lesMelding
import no.nav.k9brukerdialogprosessering.utils.KafkaUtils.opprettKafkaConsumer
import no.nav.k9brukerdialogprosessering.utils.KafkaUtils.opprettKafkaProducer
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Producer
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.test.EmbeddedKafkaBroker
import java.net.URI
import java.time.ZonedDateTime
import java.util.*

@KafkaIntegrationTest
class OMPUtbetalingATSøknadKonsumentTest {

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker // Broker som brukes til å konfigurere opp en kafka producer.

    @MockkBean(relaxed = true)
    private lateinit var k9MellomlagringService: K9MellomlagringService

    @MockkBean(relaxed = true)
    private lateinit var k9JoarkService: K9JoarkService

    lateinit var producer: Producer<String, Any>
    lateinit var consumer: Consumer<String, String>
    lateinit var k9DittnavVarselConsumer: Consumer<String, String>

    @BeforeAll
    fun setUp() {
        producer = embeddedKafkaBroker.opprettKafkaProducer()
        consumer = embeddedKafkaBroker.opprettKafkaConsumer(
            groupPrefix = "omsorgspengerutbetaling-arbeidstaker", topics = listOf(
                OMP_UTB_AT_MOTTATT_TOPIC, OMP_UTB_AT_PREPROSESSERT_TOPIC, OMP_UTB_AT_CLEANUP_TOPIC
            )
        )
        k9DittnavVarselConsumer = embeddedKafkaBroker.opprettKafkaConsumer(
            groupPrefix = "k9-dittnav-varsel",
            topics = listOf(DittnavVarselTopologyConfiguration.K9_DITTNAV_VARSEL_TOPIC)
        )
    }

    @AfterAll
    fun tearDown() {
        producer.close()
        consumer.close()
        k9DittnavVarselConsumer.close()
    }

    @Test
    fun `forvent at melding konsumeres riktig og dokumenter blir slettet`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15.000Z"
        val mottatt = ZonedDateTime.parse(mottattString)
        val søknadMottatt = OMPUtbetalingATSøknadUtils.defaultSøknad(
            søknadId = søknadId,
            mottatt = mottatt
        )

        val correlationId = UUID.randomUUID().toString()
        val metadata = Metadata(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, søknadMottatt)
        val topicEntryJson = mapper.writeValueAsString(topicEntry)

        val forventetDokmentIderForSletting = listOf("123456789", "987654321")
        coEvery { k9MellomlagringService.lagreDokument(any()) }.returnsMany(forventetDokmentIderForSletting.map { URI("http://localhost:8080/dokument/$it") })
        coEvery { k9JoarkService.journalfør(any()) } returns JournalføringsResponse("123456789")

        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = OMP_UTB_AT_MOTTATT_TOPIC)
        verify(exactly = 1, timeout = 120 * 1000) {
            runBlocking {
                k9MellomlagringService.slettDokumenter(any(), any())
            }
        }

        k9DittnavVarselConsumer.lesMelding(
            key = søknadId,
            topic = DittnavVarselTopologyConfiguration.K9_DITTNAV_VARSEL_TOPIC,
            maxWaitInSeconds = 40
        ).value().assertDittnavVarsel(
            K9Beskjed(
                metadata = metadata,
                grupperingsId = søknadId,
                tekst = "Søknad om utbetaling av omsorgspenger er mottatt.",
                link = null,
                dagerSynlig = 7,
                søkerFødselsnummer = søknadMottatt.søkerFødselsnummer(),
                eventId = "testes ikke",
                ytelse = "OMSORGSPENGER_UT_ARBEIDSTAKER",
            )
        )
    }

    @Test
    fun `Forvent at melding bli prosessert på 5 forsøk etter 4 feil`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15.000Z"
        val mottatt = ZonedDateTime.parse(mottattString)
        val søknadMottatt = OMPUtbetalingATSøknadUtils.defaultSøknad(
            søknadId = søknadId,
            mottatt = mottatt
        )
        val correlationId = UUID.randomUUID().toString()
        val metadata = Metadata(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, søknadMottatt)
        val topicEntryJson = mapper.writeValueAsString(topicEntry)

        coEvery { k9MellomlagringService.lagreDokument(any()) }
            .throws(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenMany(listOf("123456789", "987654321").map { URI("http://localhost:8080/dokument/$it") })

        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = OMP_UTB_AT_MOTTATT_TOPIC)
        val lesMelding =
            consumer.lesMelding(key = søknadId, topic = OMP_UTB_AT_PREPROSESSERT_TOPIC, maxWaitInSeconds = 40).value()

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
                    "land": "ESP"
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
              }
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
