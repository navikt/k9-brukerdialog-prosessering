package no.nav.k9brukerdialogprosessering.meldinger.ettersendelse

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.k9brukerdialogprosessering.common.MetaInfo
import no.nav.k9brukerdialogprosessering.journalforing.JournalføringsResponse
import no.nav.k9brukerdialogprosessering.journalforing.K9JoarkService
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.utils.EttersendingUtils
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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.test.EmbeddedKafkaBroker
import java.net.URI
import java.time.ZonedDateTime
import java.util.*

@KafkaIntegrationTest
class EttersendelseKonsumentTest {

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker // Broker som brukes til å konfigurere opp en kafka producer.

    @MockkBean(relaxed = true)
    private lateinit var k9DokumentMellomlagringService: K9DokumentMellomlagringService

    @MockkBean(relaxed = true)
    private lateinit var k9JoarkService: K9JoarkService

    lateinit var producer: Producer<String, Any> // Kafka producer som brukes til å legge på kafka meldinger. Mer spesifikk, Hendelser om ettersendelse
    lateinit var consumer: Consumer<String, String> // Kafka producer som brukes til å legge på kafka meldinger. Mer spesifikk, Hendelser om ettersendelse

    @BeforeAll
    fun setUp() {
        producer = embeddedKafkaBroker.opprettKafkaProducer()
        consumer = embeddedKafkaBroker.opprettKafkaConsumer(
            groupPrefix = "ettersendelse", topics = listOf(
                ETTERSENDELSE_MOTTATT_TOPIC, ETTERSENDELSE_PREPROSESSERT_TOPIC, ETTERSENDELSE_CLEANUP_TOPIC
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
        val ettersendelseMottatt = EttersendingUtils.defaultEttersendelse(søknadId = søknadId, mottatt = mottatt)
        val correlationId = UUID.randomUUID().toString()
        val metadata = MetaInfo(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, ettersendelseMottatt)
        val topicEntryJson = mapper.writeValueAsString(topicEntry)

        val forventetDokmentIderForSletting = listOf("123456789", "987654321")
        coEvery { k9DokumentMellomlagringService.lagreDokument(any()) }.returnsMany(forventetDokmentIderForSletting.map { URI("http://localhost:8080/dokument/$it") })
        coEvery { k9JoarkService.journalfør(any()) } returns JournalføringsResponse("123456789")

        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = ETTERSENDELSE_MOTTATT_TOPIC)
        verify(exactly = 1, timeout = 120 * 1000) {
            runBlocking {
                k9DokumentMellomlagringService.slettDokumenter(any(), any())
            }
        }
    }

    @Test
    fun `Forvent at melding bli prosessert på 5 forsøk etter 4 feil`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattString = "2020-01-01T10:30:15.000Z"
        val mottatt = ZonedDateTime.parse(mottattString)
        val ettersendelseMottatt = EttersendingUtils.defaultEttersendelse(søknadId = søknadId, mottatt = mottatt)
        val correlationId = UUID.randomUUID().toString()
        val metadata = MetaInfo(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, ettersendelseMottatt)
        val topicEntryJson = mapper.writeValueAsString(topicEntry)

        coEvery { k9DokumentMellomlagringService.lagreDokument(any()) }
            .throws(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenMany(listOf("123456789", "987654321").map { URI("http://localhost:8080/dokument/$it") })

        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = ETTERSENDELSE_MOTTATT_TOPIC)
        val lesMelding = consumer.lesMelding(key = søknadId, topic = ETTERSENDELSE_PREPROSESSERT_TOPIC, maxWaitInSeconds = 40).value()

        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
        JSONAssert.assertEquals(preprosessertEttersendelseSomJson(søknadId, mottattString), preprosessertSøknadJson, true)
    }

    @Language("JSON")
    private fun preprosessertEttersendelseSomJson(søknadId: String, mottatt: String)   = """
        {
          "harForstattRettigheterOgPlikter": true,
          "k9Format": {
            "mottattDato": "$mottatt",
            "søknadId": "$søknadId",
            "søker": {
              "norskIdentitetsnummer": "29099012345"
            },
            "ytelse": "PLEIEPENGER_SYKT_BARN",
            "versjon": "0.0.1",
            "type": "LEGEERKLÆRING",
            "pleietrengende": {
              "norskIdentitetsnummer": "29099012345"
            }
          },
          "soknadId": "$søknadId",
          "mottatt": "$mottatt",
          "søker": {
            "etternavn": "Ånsnes",
            "mellomnavn": "Øverbø",
            "aktørId": "123456",
            "fødselsdato": "2003-03-21",
            "fornavn": "Ærling",
            "fødselsnummer": "29099012345"
          },
         
          "vedleggId": [
            [
              "123456789",
              "987654321"
            ],
            [
              "vedlegg1"
            ],
            [
              "vedlegg2"
            ],
            [
              "vedlegg3"
            ]
          ],
          "sprak": "nb",
          "titler": [
            "Vedlegg 1",
            "Vedlegg 2",
            "Vedlegg 3"
          ],
          "søknadstype": "PLEIEPENGER_SYKT_BARN",
          "ettersendelsesType": "LEGEERKLÆRING",
          "pleietrengende": {
            "fødselsdato": "2003-03-21",
            "navn": "Ola Nordmann",
            "norskIdentitetsnummer": "29099012345"
          },
          "harBekreftetOpplysninger": true,
          "beskrivelse": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed accumsan erat cursus enim aliquet, ac auctor orci consequat. Etiam nec tellus sapien. Nam gravida massa id sagittis ultrices."
        }
        """.trimIndent()
}
