package no.nav.k9brukerdialogprosessering.meldinger.endringsmelding

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.k9brukerdialogprosessering.K9brukerdialogprosesseringApplication
import no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.utils.EndringsmeldingUtils
import no.nav.k9brukerdialogprosessering.journalforing.JournalføringsResponse
import no.nav.k9brukerdialogprosessering.journalforing.K9JoarkService
import no.nav.k9brukerdialogprosessering.kafka.types.Metadata
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.mellomlagring.K9MellomlagringService
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.PSBTopologyConfiguration.Companion.PSB_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.PSBTopologyConfiguration.Companion.PSB_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.PSBTopologyConfiguration.Companion.PSB_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.utils.KafkaUtils.leggPåTopic
import no.nav.k9brukerdialogprosessering.utils.KafkaUtils.lesMelding
import no.nav.k9brukerdialogprosessering.utils.KafkaUtils.opprettKafkaConsumer
import no.nav.k9brukerdialogprosessering.utils.KafkaUtils.opprettKafkaProducer
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Producer
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URI
import java.time.ZonedDateTime
import java.util.*

@EmbeddedKafka( // Setter opp og tilgjengligjør embeded kafka broker
    partitions = 1,
    count = 3,
    bootstrapServersProperty = "KAFKA_BROKERS", // Setter bootstrap-servers for consumer og producer.
    topics = [
        PSB_ENDRINGSMELDING_MOTTATT_TOPIC, PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC, PSB_ENDRINGSMELDING_CLEANUP_TOPIC,
        PSB_MOTTATT_TOPIC, PSB_PREPROSESSERT_TOPIC, PSB_CLEANUP_TOPIC
    ]
)
@DirtiesContext // Sørger for at context blir re-instantiert mellom hver test.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@EnableMockOAuth2Server // Tilgjengliggjør en oicd-provider for test. Se application-test.yml -> no.nav.security.jwt.issuer.selvbetjening for konfigurasjon
@ActiveProfiles("test")
@SpringBootTest(
    classes = [K9brukerdialogprosesseringApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
) // Integrasjonstest - Kjører opp hele Spring Context med alle konfigurerte beans.
class PleiepengerSyktBarnEndringsmeldingKonsumentTest {

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @MockkBean
    private lateinit var k9MellomlagringService: K9MellomlagringService

    @MockkBean(relaxed = true)
    private lateinit var k9JoarkService: K9JoarkService

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker // Broker som brukes til å konfigurere opp en kafka producer.

    lateinit var producer: Producer<String, Any> // Kafka producer som brukes til å legge på kafka meldinger. Mer spesifikk, Hendelser om pp-sykt-barn
    lateinit var consumer: Consumer<String, String> // Kafka producer som brukes til å legge på kafka meldinger. Mer spesifikk, Hendelser om pp-sykt-barn

    @BeforeAll
    fun setUp() {
        producer = embeddedKafkaBroker.opprettKafkaProducer()
        consumer = embeddedKafkaBroker.opprettKafkaConsumer(
            groupPrefix = "pleiepenger-sykt-barn-endringsmelding", topics = listOf(
                PSB_ENDRINGSMELDING_MOTTATT_TOPIC,
                PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC,
                PSB_ENDRINGSMELDING_CLEANUP_TOPIC
            )
        )
    }

    @AfterAll
    fun tearDown() {
        producer.close()
        consumer.close()
    }

    @Test
    fun `forvent at melding konsumeres riktig og dokumenter blir slettet`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15.000Z"
        val mottatt = ZonedDateTime.parse(mottattString)
        val søknadMottatt = EndringsmeldingUtils.defaultEndringsmelding(søknadsId = søknadId, mottatt = mottatt)
        val correlationId = UUID.randomUUID().toString()
        val metadata = Metadata(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, søknadMottatt)
        val topicEntryJson = mapper.writeValueAsString(topicEntry)

        val forventetDokmentIderForSletting = listOf("123456789", "987654321")
        coEvery { k9MellomlagringService.lagreDokument(any()) }.returnsMany(forventetDokmentIderForSletting.map { URI("http://localhost:8080/dokument/$it") })
        coEvery { k9JoarkService.journalfør(any()) } returns JournalføringsResponse("123456789")

        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = PSB_ENDRINGSMELDING_MOTTATT_TOPIC)
        verify(exactly = 1, timeout = 120 * 1000) {
            runBlocking {
                k9MellomlagringService.slettDokumenter(any(), any())
            }
        }
    }

    @Test
    fun `Forvent at melding bli prosessert på 5 forsøk etter 4 feil`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15.000Z"
        val mottatt = ZonedDateTime.parse(mottattString)
        val søknadMottatt = EndringsmeldingUtils.defaultEndringsmelding(søknadsId = søknadId, mottatt = mottatt)
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

        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = PSB_ENDRINGSMELDING_MOTTATT_TOPIC)
        val lesMelding = consumer.lesMelding(key = søknadId, topic = PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC, maxWaitInSeconds = 40).value()

        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
        println("---> " + preprosessertSøknadJson)
        JSONAssert.assertEquals(preprosessertEndringsmeldingSomJson(søknadId, mottattString), preprosessertSøknadJson, true)
    }

    @Language("JSON")
    private fun preprosessertEndringsmeldingSomJson(søknadId: String, mottatt: String) = """
         {
            "søker": {
               "aktørId": "123456",
               "fødselsnummer": "02119970078",
               "fornavn": "Ola",
               "mellomnavn": "Mellomnavn",
               "etternavn": "Nordmann"
            },
            "pleietrengendeNavn": "Barn Barnesen",
            "dokumentId": [
                [
                  "123456789",
                  "987654321"
                ]
              ],
            "k9FormatSøknad": {
              "søknadId": "$søknadId",
              "mottattDato": "$mottatt",
              "språk": "nb",
              "søker": {
                "norskIdentitetsnummer": "12345678910"
              },
              "versjon": "1.0.0",
              "ytelse": {
                "type": "PLEIEPENGER_SYKT_BARN",
                "arbeidstid": {
                  "arbeidstakerList": [
                    {
                      "arbeidstidInfo": {
                        "perioder": {
                          "2018-01-01/2020-01-05": {
                            "faktiskArbeidTimerPerDag": "PT4H",
                            "jobberNormaltTimerPerDag": "PT8H"
                          },
                          "2020-01-06/2020-01-10": {
                            "faktiskArbeidTimerPerDag": "PT2H",
                            "jobberNormaltTimerPerDag": "PT8H"
                          }
                        }
                      },
                      "norskIdentitetsnummer": "12345678910",
                      "organisasjonsnummer": "926032925"
                    }
                  ],
                  "frilanserArbeidstidInfo": null,
                  "selvstendigNæringsdrivendeArbeidstidInfo": null
                },
                "barn": {
                  "fødselsdato": null,
                  "norskIdentitetsnummer": "10987654321"
                },
                "beredskap": {
                  "perioder": {
                    "2020-01-01/2020-01-05": {
                      "tilleggsinformasjon": "Jeg skal være i beredskap. Basta!"
                    },
                    "2020-01-07/2020-01-10": {
                      "tilleggsinformasjon": "Jeg skal være i beredskap i denne perioden også. Basta!"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "bosteder": {
                  "perioder": {
                    "2020-01-01/2020-01-05": {
                      "land": "ESP"
                    },
                    "2020-01-06/2020-01-10": {
                      "land": "NOR"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "dataBruktTilUtledning": {
                  "bekrefterPeriodeOver8Uker": true,
                  "harBekreftetOpplysninger": true,
                  "harForståttRettigheterOgPlikter": true,
                  "harMedsøker": true,
                  "samtidigHjemme": true,
                  "soknadDialogCommitSha": "commit-abc-123"
                },
                "endringsperiode": [],
                "infoFraPunsj": null,
                "lovbestemtFerie": {
                  "perioder": {
                    "2020-01-01/2020-01-05": {
                      "skalHaFerie": true
                    },
                    "2020-01-06/2020-01-10": {
                      "skalHaFerie": true
                    }
                  }
                },
                "nattevåk": {
                  "perioder": {
                    "2020-01-01/2020-01-05": {
                      "tilleggsinformasjon": "Jeg skal ha nattevåk. Basta!"
                    },
                    "2020-01-07/2020-01-10": {
                      "tilleggsinformasjon": "Jeg skal ha nattevåk i perioden også. Basta!"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "omsorg": {
                  "beskrivelseAvOmsorgsrollen": "Blabla beskrivelse",
                  "relasjonTilBarnet": "MOR"
                },
                "opptjeningAktivitet": {
                  "frilanser": {
                    "sluttdato": null,
                    "startdato": "2020-01-01"
                  },
                  "selvstendigNæringsdrivende": [
                    {
                      "organisasjonsnummer": "12345678910112233444455667",
                      "perioder": {
                        "2018-01-01/2020-01-01": {
                          "bruttoInntekt": 5000000,
                          "endringBegrunnelse": "Grunnet Covid-19",
                          "endringDato": "2020-01-01",
                          "erNyoppstartet": true,
                          "erVarigEndring": true,
                          "landkode": "NOR",
                          "registrertIUtlandet": false,
                          "regnskapsførerNavn": "Regnskapsfører Svensen",
                          "regnskapsførerTlf": "+4799887766",
                          "virksomhetstyper": [
                            "DAGMAMMA",
                            "ANNEN"
                          ]
                        }
                      },
                      "virksomhetNavn": "Mamsen Bamsen AS"
                    },
                    {
                      "organisasjonsnummer": "54549049090490498048940940",
                      "perioder": {
                        "2015-01-01/2017-01-01": {
                          "bruttoInntekt": 500000,
                          "erNyoppstartet": false,
                          "erVarigEndring": false,
                          "landkode": "ESP",
                          "registrertIUtlandet": true,
                          "virksomhetstyper": [
                            "FISKE"
                          ]
                        }
                      },
                      "virksomhetNavn": "Something Fishy AS"
                    }
                  ]
                },
                "søknadsperiode": [
                  "2020-01-01/2020-01-10"
                ],
                "tilsynsordning": {
                  "perioder": {
                    "2020-01-01/2020-01-05": {
                      "etablertTilsynTimerPerDag": "PT8H"
                    },
                    "2020-01-06/2020-01-10": {
                      "etablertTilsynTimerPerDag": "PT4H"
                    }
                  }
                },
                "trekkKravPerioder": [],
                "utenlandsopphold": {
                  "perioder": {
                    "2020-01-01/2020-01-05": {
                      "land": "CAN",
                      "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd"
                    },
                    "2020-01-06/2020-01-10": {
                      "land": "SWE",
                      "årsak": "barnetInnlagtIHelseinstitusjonForNorskOffentligRegning"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "uttak": {
                  "perioder": {
                    "2020-01-01/2020-01-05": {
                      "timerPleieAvBarnetPerDag": "PT4H"
                    },
                    "2020-01-06/2020-01-10": {
                      "timerPleieAvBarnetPerDag": "PT2H"
                    }
                  }
                }
              },
              "begrunnelseForInnsending": {
                "tekst": null
              },
              "kildesystem": null,
              "journalposter": []
            }
          }
        """.trimIndent()
}
