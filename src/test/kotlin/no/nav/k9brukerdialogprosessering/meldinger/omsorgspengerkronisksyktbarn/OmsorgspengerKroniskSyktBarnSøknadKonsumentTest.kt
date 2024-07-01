package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.k9brukerdialogprosessering.dittnavvarsel.DittnavVarselTopologyConfiguration.Companion.K9_DITTNAV_VARSEL_TOPIC
import no.nav.k9brukerdialogprosessering.dittnavvarsel.K9Beskjed
import no.nav.k9brukerdialogprosessering.journalforing.JournalføringsResponse
import no.nav.k9brukerdialogprosessering.journalforing.K9JoarkService
import no.nav.k9brukerdialogprosessering.kafka.types.Metadata
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.OMPKSTopologyConfiguration.Companion.OMP_UTV_KS_SØKNAD_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.OMPKSTopologyConfiguration.Companion.OMP_UTV_KS_SØKNAD_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.OMPKSTopologyConfiguration.Companion.OMP_UTV_KS_SØKNAD_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.utils.SøknadUtils
import no.nav.k9brukerdialogprosessering.mellomlagring.dokument.K9DokumentMellomlagringService
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.test.EmbeddedKafkaBroker
import java.net.URI
import java.time.ZonedDateTime
import java.util.*

@KafkaIntegrationTest
class OmsorgspengerKroniskSyktBarnSøknadKonsumentTest {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker // Broker som brukes til å konfigurere opp en kafka producer.

    @MockkBean(relaxed = true)
    private lateinit var k9DokumentMellomlagringService: K9DokumentMellomlagringService

    @MockkBean(relaxed = true)
    private lateinit var k9JoarkService: K9JoarkService

    lateinit var producer: Producer<String, Any> // Kafka producer som brukes til å legge på kafka meldinger. Mer spesifikk, Hendelser om pp-sykt-barn
    lateinit var consumer: Consumer<String, String> // Kafka producer som brukes til å legge på kafka meldinger. Mer spesifikk, Hendelser om pp-sykt-barn
    lateinit var k9DittnavVarselConsumer: Consumer<String, String> // Kafka producer som brukes til å legge på kafka meldinger. Mer spesifikk, Hendelser om pp-sykt-barn

    @BeforeAll
    fun setUp() {
        producer = embeddedKafkaBroker.opprettKafkaProducer()
        consumer = embeddedKafkaBroker.opprettKafkaConsumer(
            groupPrefix = "omsorgspenger-kronisk-sykt-barn", topics = listOf(
                OMP_UTV_KS_SØKNAD_MOTTATT_TOPIC, OMP_UTV_KS_SØKNAD_PREPROSESSERT_TOPIC, OMP_UTV_KS_SØKNAD_CLEANUP_TOPIC
            )
        )
        k9DittnavVarselConsumer = embeddedKafkaBroker.opprettKafkaConsumer(
            groupPrefix = "k9-dittnav-varsel",
            topics = listOf(K9_DITTNAV_VARSEL_TOPIC)
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
        val søknadMottatt = SøknadUtils.defaultSøknad(søknadId = søknadId, mottatt = mottatt)
        val correlationId = UUID.randomUUID().toString()
        val metadata = Metadata(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, søknadMottatt)
        val topicEntryJson = objectMapper.writeValueAsString(topicEntry)

        val forventetDokmentIderForSletting = listOf("123456789", "987654321")
        coEvery { k9DokumentMellomlagringService.lagreDokument(any()) }.returnsMany(forventetDokmentIderForSletting.map { URI("http://localhost:8080/dokument/$it") })
        coEvery { k9JoarkService.journalfør(any()) } returns JournalføringsResponse("123456789")

        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = OMP_UTV_KS_SØKNAD_MOTTATT_TOPIC)
        verify(exactly = 1, timeout = 120 * 1000) {
            runBlocking {
                k9DokumentMellomlagringService.slettDokumenter(any(), any())
            }
        }

        k9DittnavVarselConsumer.lesMelding(
            key = søknadId,
            topic = K9_DITTNAV_VARSEL_TOPIC,
            maxWaitInSeconds = 40
        ).value().assertDittnavVarsel(
            K9Beskjed(
                metadata = metadata,
                grupperingsId = søknadId,
                tekst = "Vi har mottatt søknad fra deg om ekstra omsorgsdager ved kronisk sykt eller funksjonshemmet barn.",
                link = null,
                dagerSynlig = 7,
                søkerFødselsnummer = søknadMottatt.søkerFødselsnummer(),
                eventId = "testes ikke",
                ytelse = "OMSORGSPENGER_UTV_KS",
            )
        )
    }

    @Test
    fun `Forvent at melding bli prosessert på 5 forsøk etter 4 feil`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15.000Z"
        val mottatt = ZonedDateTime.parse(mottattString)
        val søknadMottatt = SøknadUtils.defaultSøknad(søknadId = søknadId, mottatt = mottatt)
        val correlationId = UUID.randomUUID().toString()
        val metadata = Metadata(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, søknadMottatt)
        val topicEntryJson = objectMapper.writeValueAsString(topicEntry)

        coEvery { k9DokumentMellomlagringService.lagreDokument(any()) }
            .throws(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenMany(listOf("123456789", "987654321").map { URI("http://localhost:8080/dokument/$it") })

        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = OMP_UTV_KS_SØKNAD_MOTTATT_TOPIC)
        val lesMelding =
            consumer.lesMelding(key = søknadId, topic = OMP_UTV_KS_SØKNAD_PREPROSESSERT_TOPIC, maxWaitInSeconds = 40)
                .value()

        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
        JSONAssert.assertEquals(preprosessertSøknadSomJson(søknadId, mottattString), preprosessertSøknadJson, true)
    }

    @Language("JSON")
    private fun preprosessertSøknadSomJson(søknadId: String, mottatt: String) = """
         {
          "soknadId": "$søknadId",
          "mottatt": "$mottatt",
          "språk": "nb",
          "barn": {
            "aktørId": "123456",
            "fødselsdato": "2020-01-01",
            "navn": "Ole Dole Doffen",
            "norskIdentifikator": "02119970078"
          },
          "kroniskEllerFunksjonshemming": false,
          "søker": {
            "etternavn": "Kjeller",
            "mellomnavn": null,
            "aktørId": "12345",
            "fødselsdato": "2000-01-01",
            "fornavn": "Kjell",
            "fødselsnummer": "26104500284"
          },
          "harForståttRettigheterOgPlikter": true,
          "dokumentId": [
            [
              "123456789",
              "987654321"
            ],
            [
              "5678"
            ],
            [
              "1234"
            ]
          ],
          "relasjonTilBarnet": "FAR",
          "sammeAdresse": "JA",
          "høyereRisikoForFravær": true,
           "høyereRisikoForFraværBeskrivelse": "Beskrivelse av høyere risiko for fravær",
          "harBekreftetOpplysninger": true,
          "k9FormatSøknad": {
            "språk": "nb",
            "kildesystem": null,
            "mottattDato": "$mottatt",
            "søknadId": "$søknadId",
            "søker": {
              "norskIdentitetsnummer": "26104500284"
            },
            "ytelse": {
              "barn": {
                "fødselsdato": null,
                "norskIdentitetsnummer": "02119970078"
              },
              "kroniskEllerFunksjonshemming": true,
              "type": "OMP_UTV_KS",
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
