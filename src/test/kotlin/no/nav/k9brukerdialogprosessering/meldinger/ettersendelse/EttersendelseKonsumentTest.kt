package no.nav.k9brukerdialogprosessering.meldinger.ettersendelse

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.k9brukerdialogprosessering.K9brukerdialogprosesseringApplication
import no.nav.k9brukerdialogprosessering.journalforing.JournalføringsResponse
import no.nav.k9brukerdialogprosessering.journalforing.K9JoarkService
import no.nav.k9brukerdialogprosessering.kafka.types.Metadata
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_MOTTATT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.utils.EttersendingUtils
import no.nav.k9brukerdialogprosessering.mellomlagring.K9MellomlagringService
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
    topics = [ETTERSENDELSE_MOTTATT_TOPIC, ETTERSENDELSE_PREPROSESSERT_TOPIC, ETTERSENDELSE_CLEANUP_TOPIC]
)
@DirtiesContext // Sørger for at context blir re-instantiert mellom hver test.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@EnableMockOAuth2Server // Tilgjengliggjør en oicd-provider for test. Se application-test.yml -> no.nav.security.jwt.issuer.selvbetjening for konfigurasjon
@ActiveProfiles("test")
@SpringBootTest(
    classes = [K9brukerdialogprosesseringApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
) // Integrasjonstest - Kjører opp hele Spring Context med alle konfigurerte beans.
class EttersendelseKonsumentTest {

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker // Broker som brukes til å konfigurere opp en kafka producer.

    @MockkBean(relaxed = true)
    private lateinit var k9MellomlagringService: K9MellomlagringService

    @MockkBean(relaxed = true)
    private lateinit var k9JoarkService: K9JoarkService

    lateinit var producer: Producer<String, Any> // Kafka producer som brukes til å legge på kafka meldinger. Mer spesifikk, Hendelser om pp-sykt-barn
    lateinit var consumer: Consumer<String, String> // Kafka producer som brukes til å legge på kafka meldinger. Mer spesifikk, Hendelser om pp-sykt-barn

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
        val metadata = Metadata(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, ettersendelseMottatt)
        val topicEntryJson = mapper.writeValueAsString(topicEntry)

        val forventetDokmentIderForSletting = listOf("123456789", "987654321")
        coEvery { k9MellomlagringService.lagreDokument(any()) }.returnsMany(forventetDokmentIderForSletting.map { URI("http://localhost:8080/dokument/$it") })
        coEvery { k9JoarkService.journalfør(any()) } returns JournalføringsResponse("123456789")

        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = ETTERSENDELSE_MOTTATT_TOPIC)
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
        val ettersendelseMottatt = EttersendingUtils.defaultEttersendelse(søknadId = søknadId, mottatt = mottatt)
        val correlationId = UUID.randomUUID().toString()
        val metadata = Metadata(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, ettersendelseMottatt)
        val topicEntryJson = mapper.writeValueAsString(topicEntry)

        coEvery { k9MellomlagringService.lagreDokument(any()) }
            .throws(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenMany(listOf("123456789", "987654321").map { URI("http://localhost:8080/dokument/$it") })

        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = ETTERSENDELSE_MOTTATT_TOPIC)
        val lesMelding = consumer.lesMelding(key = søknadId, topic = ETTERSENDELSE_PREPROSESSERT_TOPIC, maxWaitInSeconds = 40).value()

        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
        println("---> " + preprosessertSøknadJson)
        JSONAssert.assertEquals(preprosessertEttersendelseSomJson(søknadId, mottattString), preprosessertSøknadJson, true)
    }

    @Language("JSON")
    private fun preprosessertEttersendelseSomJson(søknadId: String, mottatt: String) = """
        {
          "harForstattRettigheterOgPlikter": true,
          "k9Format": {
            "mottattDato": "$mottatt",
            "søknadId": "$søknadId",
            "søker": {
              "norskIdentitetsnummer": "29099012345"
            },
            "ytelse": "PLEIEPENGER_SYKT_BARN",
            "versjon": "0.0.1"
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
          "harBekreftetOpplysninger": true,
          "beskrivelse": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed accumsan erat cursus enim aliquet, ac auctor orci consequat. Etiam nec tellus sapien. Nam gravida massa id sagittis ultrices."
        }
        """.trimIndent()
}
