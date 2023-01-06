package no.nav.k9brukerdialogprosessering.pleiepengersyktbarn

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.SpykBean
import no.nav.k9brukerdialogprosessering.K9brukerdialogprosesseringApplication
import no.nav.k9brukerdialogprosessering.common.Metadata
import no.nav.k9brukerdialogprosessering.common.TopicEntry
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.utils.PSBSøknadUtils
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
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.ZonedDateTime
import java.util.*

@EmbeddedKafka( // Setter opp og tilgjengligjør embeded kafka broker
    partitions = 3,
    bootstrapServersProperty = "kafka-servers", // Setter bootstrap-servers for consumer og producer.
    topics = [
        PleiepengerSyktBarnSøknadKonsument.PSB_MOTTATT_TOPIC,
        PleiepengerSyktBarnSøknadKonsument.PSB_PREPROSESSERT_TOPIC,
        PleiepengerSyktBarnSøknadKonsument.PSB_CLEANUP_TOPIC
    ]
)
@DirtiesContext // Sørger for at context blir re-instantiert mellom hver test.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@EnableMockOAuth2Server // Tilgjengliggjør en oicd-provider for test. Se application-test.yml -> no.nav.security.jwt.issuer.selvbetjening for konfigurasjon
@ActiveProfiles("test")
@SpringBootTest(
    classes = [K9brukerdialogprosesseringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
) // Integrasjonstest - Kjører opp hele Spring Context med alle konfigurerte beans.
class PleiepengerSyktBarnSøknadKonsumentTest {

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker // Broker som brukes til å konfigurere opp en kafka producer.

    @SpykBean
    private lateinit var psbPreprosesseringsService: PSBPreprosesseringsService

    lateinit var producer: Producer<String, Any> // Kafka producer som brukes til å legge på kafka meldinger. Mer spesifikk, Hendelser om pp-sykt-barn
    lateinit var consumer: Consumer<String, String> // Kafka producer som brukes til å legge på kafka meldinger. Mer spesifikk, Hendelser om pp-sykt-barn

    @BeforeAll
    fun setUp() {
        producer = embeddedKafkaBroker.opprettKafkaProducer()
        consumer = embeddedKafkaBroker.opprettKafkaConsumer(
            groupPrefix = "pleiepenger-sykt-barn",
            topics = listOf(
                PleiepengerSyktBarnSøknadKonsument.PSB_MOTTATT_TOPIC,
                PleiepengerSyktBarnSøknadKonsument.PSB_PREPROSESSERT_TOPIC,
                PleiepengerSyktBarnSøknadKonsument.PSB_CLEANUP_TOPIC
            )
        )
    }

    @AfterAll
    fun tearDown() {
        producer.close()
        consumer.close()
    }

    @Test
    fun `forvent riktig serialisering og deserialisering`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15.000Z"
        val mottatt = ZonedDateTime.parse(mottattString)
        val søknadMottatt = PSBSøknadUtils.defaultSøknad(søknadId = søknadId, mottatt = mottatt)
        val metadata = Metadata(version = 1, correlationId = UUID.randomUUID().toString())
        val topicEntry = TopicEntry(metadata, søknadMottatt)
        val topicEntryJson = mapper.writeValueAsString(topicEntry)
        producer.leggPåTopic(
            key = søknadId,
            value = topicEntryJson,
            topic = PleiepengerSyktBarnSøknadKonsument.PSB_MOTTATT_TOPIC,
        )
        val lesMelding = consumer.lesMelding(key = søknadId, topic = PleiepengerSyktBarnSøknadKonsument.PSB_PREPROSESSERT_TOPIC).value()
        LoggerFactory.getLogger(PleiepengerSyktBarnSøknadKonsumentTest::class.java).info("---> {}", lesMelding)
        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
        JSONAssert.assertEquals(preprosessertSøknadSomJson(søknadId, mottattString), preprosessertSøknadJson, true)
    }

    @Language("JSON")
    private fun preprosessertSøknadSomJson(søknadId: String, mottatt: String) =
        """
           {
             "apiDataVersjon": null,
             "søknadId": "$søknadId",
             "mottatt": "$mottatt",
             "språk": "nb",
             "søker": {
               "aktørId": "123456",
               "etternavn": "Nordmann",
               "fornavn": "Ola",
               "fødselsnummer": "02119970078",
               "mellomnavn": "Mellomnavn"
             },
             "barn": {
               "aktørId": "11111111111",
               "fødselsdato": null,
               "fødselsnummer": "02119970078",
               "navn": "Ole Dole",
               "årsakManglerIdentitetsnummer": null
             },
             "tilOgMed": "2021-01-01",
             "fraOgMed": "2021-01-01",
             "arbeidsgivere": [
               {
                 "arbeidsforhold": {
                   "arbeidIPeriode": {
                     "arbeiderIPerioden": "SOM_VANLIG",
                     "arbeidsuker": null,
                     "prosentAvNormalt": null,
                     "timerPerUke": null,
                     "type": "ARBEIDER_VANLIG"
                   },
                   "normalarbeidstid": {
                     "timerPerUkeISnitt": "PT37H30M"
                   }
                 },
                 "erAnsatt": true,
                 "navn": "Peppes",
                 "organisasjonsnummer": "917755736",
                 "sluttetFørSøknadsperiode": null
               },
               {
                 "arbeidsforhold": {
                   "arbeidIPeriode": {
                     "arbeiderIPerioden": "SOM_VANLIG",
                     "arbeidsuker": null,
                     "prosentAvNormalt": null,
                     "timerPerUke": null,
                     "type": "ARBEIDER_VANLIG"
                   },
                   "normalarbeidstid": {
                     "timerPerUkeISnitt": "PT37H30M"
                   }
                 },
                 "erAnsatt": true,
                 "navn": "Pizzabakeren",
                 "organisasjonsnummer": "917755736",
                 "sluttetFørSøknadsperiode": null
               }
             ],
             "barnRelasjon": null,
             "barnRelasjonBeskrivelse": null,
             "beredskap": {
               "beredskap": true,
               "tilleggsinformasjon": "I Beredskap"
             },
             "ferieuttakIPerioden": {
               "ferieuttak": [
                 {
                   "fraOgMed": "2020-01-07",
                   "tilOgMed": "2020-01-08"
                 },
                 {
                   "fraOgMed": "2020-01-09",
                   "tilOgMed": "2020-01-10"
                 }
               ],
               "skalTaUtFerieIPerioden": true
             },
             "frilans": {
               "arbeidsforhold": {
                 "arbeidIPeriode": {
                   "arbeiderIPerioden": "SOM_VANLIG",
                   "arbeidsuker": null,
                   "prosentAvNormalt": null,
                   "timerPerUke": null,
                   "type": "ARBEIDER_VANLIG"
                 },
                 "normalarbeidstid": {
                   "timerPerUkeISnitt": "PT37H30M"
                 }
               },
               "harInntektSomFrilanser": true,
               "jobberFortsattSomFrilans": true,
               "sluttdato": null,
               "startdato": "2019-01-01"
             },
             "harBekreftetOpplysninger": true,
             "harForstattRettigheterOgPlikter": true,
             "harMedsøker": true,
             "harVærtEllerErVernepliktig": true,
             "medlemskap": {
               "harBoddIUtlandetSiste12Mnd": true,
               "skalBoIUtlandetNeste12Mnd": false,
               "utenlandsoppholdNeste12Mnd": [],
               "utenlandsoppholdSiste12Mnd": [
                 {
                   "fraOgMed": "2020-01-02",
                   "landkode": "US",
                   "landnavn": "USA",
                   "tilOgMed": "2020-01-03"
                 }
               ]
             },
             "nattevåk": {
               "harNattevåk": true,
               "tilleggsinformasjon": "Har Nattevåk"
             },
             "omsorgstilbud": null,
             "opptjeningIUtlandet": [
               {
                 "fraOgMed": "2020-01-01",
                 "land": {
                   "landkode": "NDL",
                   "landnavn": "Nederland"
                 },
                 "navn": "Yolo AS",
                 "opptjeningType": "ARBEIDSTAKER",
                 "tilOgMed": "2020-10-01"
               }
             ],
             "samtidigHjemme": null,
             "selvstendigNæringsdrivende": {
               "arbeidsforhold": {
                 "arbeidIPeriode": {
                   "arbeiderIPerioden": "SOM_VANLIG",
                   "arbeidsuker": null,
                   "prosentAvNormalt": null,
                   "timerPerUke": null,
                   "type": "ARBEIDER_VANLIG"
                 },
                 "normalarbeidstid": {
                   "timerPerUkeISnitt": "PT37H30M"
                 }
               },
               "harInntektSomSelvstendig": true,
               "virksomhet": {
                 "fiskerErPåBladB": null,
                 "fraOgMed": "2021-01-01",
                 "harFlereAktiveVirksomheter": null,
                 "navnPåVirksomheten": "Kjells Møbelsnekkeri",
                 "næringsinntekt": null,
                 "næringstype": "ANNEN",
                 "organisasjonsnummer": "111111",
                 "registrertINorge": true,
                 "registrertIUtlandet": null,
                 "regnskapsfører": null,
                 "tilOgMed": "2021-01-10",
                 "varigEndring": null,
                 "yrkesaktivSisteTreFerdigliknedeÅrene": {
                   "oppstartsdato": "2021-01-01"
                 }
               }
             },
             "utenlandskNæring": [
               {
                 "fraOgMed": "2020-01-01",
                 "land": {
                   "landkode": "NDL",
                   "landnavn": "Nederland"
                 },
                 "navnPåVirksomheten": "Dagmamma AS",
                 "næringstype": "DAGMAMMA",
                 "organisasjonsnummer": null,
                 "tilOgMed": null
               }
             ],
             "utenlandsoppholdIPerioden": {
               "opphold": [
                 {
                   "erBarnetInnlagt": true,
                   "erUtenforEøs": true,
                   "fraOgMed": "2020-01-01",
                   "landkode": "BAH",
                   "landnavn": "Bahamas",
                   "perioderBarnetErInnlagt": [
                     {
                       "fraOgMed": "2020-01-01",
                       "tilOgMed": "2020-01-01"
                     },
                     {
                       "fraOgMed": "2020-01-03",
                       "tilOgMed": "2020-01-04"
                     }
                   ],
                   "tilOgMed": "2020-01-10",
                   "årsak": "ANNET"
                 },
                 {
                   "erBarnetInnlagt": true,
                   "erUtenforEøs": false,
                   "fraOgMed": "2020-01-01",
                   "landkode": "BHS",
                   "landnavn": "Svergie",
                   "perioderBarnetErInnlagt": [
                     {
                       "fraOgMed": "2020-01-01",
                       "tilOgMed": "2020-01-01"
                     },
                     {
                       "fraOgMed": "2020-01-03",
                       "tilOgMed": "2020-01-04"
                     },
                     {
                       "fraOgMed": "2020-01-05",
                       "tilOgMed": "2020-01-05"
                     }
                   ],
                   "tilOgMed": "2020-01-10",
                   "årsak": "ANNET"
                 }
               ],
               "skalOppholdeSegIUtlandetIPerioden": true
             },
             "dokumentId": [[
               "123456789",
               "987654321"
             ]],
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
                   "samtidigHjemme": true
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
               "journalposter": []
             }
           }
        """.trimIndent()
}
