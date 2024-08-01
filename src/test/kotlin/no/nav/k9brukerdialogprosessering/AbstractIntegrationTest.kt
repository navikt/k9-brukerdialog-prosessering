package no.nav.k9brukerdialogprosessering

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import no.nav.k9brukerdialogprosessering.dittnavvarsel.DittnavVarselTopologyConfiguration.Companion.K9_DITTNAV_VARSEL_TOPIC
import no.nav.k9brukerdialogprosessering.journalforing.JournalføringsResponse
import no.nav.k9brukerdialogprosessering.journalforing.K9JoarkService
import no.nav.k9brukerdialogprosessering.mellomlagring.dokument.Dokument
import no.nav.k9brukerdialogprosessering.mellomlagring.dokument.DokumentEier
import no.nav.k9brukerdialogprosessering.mellomlagring.dokument.K9DokumentMellomlagringService
import no.nav.k9brukerdialogprosessering.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogprosessering.oppslag.barn.BarnService
import no.nav.k9brukerdialogprosessering.oppslag.soker.Søker
import no.nav.k9brukerdialogprosessering.oppslag.soker.SøkerService
import no.nav.k9brukerdialogprosessering.utils.KafkaIntegrationTest
import no.nav.k9brukerdialogprosessering.utils.KafkaUtils.opprettKafkaConsumer
import no.nav.k9brukerdialogprosessering.utils.KafkaUtils.opprettKafkaProducer
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Producer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.test.web.servlet.MockMvc
import java.net.URI
import java.time.LocalDate

@KafkaIntegrationTest
@AutoConfigureMockMvc
abstract class AbstractIntegrationTest {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker // Broker som brukes til å konfigurere opp en kafka producer.

    @MockkBean(relaxed = true)
    protected lateinit var k9DokumentMellomlagringService: K9DokumentMellomlagringService

    @MockkBean(relaxed = true)
    protected lateinit var k9JoarkService: K9JoarkService

    @MockkBean
    protected lateinit var barnService: BarnService

    @MockkBean
    protected lateinit var søkerService: SøkerService

    @Autowired
    protected lateinit var mockOAuth2Server: MockOAuth2Server

    protected lateinit var producer: Producer<String, Any> // Kafka producer som brukes til å legge på kafka meldinger. Mer spesifikk, Hendelser om pp-sykt-barn
    protected lateinit var consumer: Consumer<String, String> // Kafka producer som brukes til å legge på kafka meldinger. Mer spesifikk, Hendelser om pp-sykt-barn
    protected lateinit var k9DittnavVarselConsumer: Consumer<String, String>

    protected abstract val consumerGroupPrefix: String
    protected abstract val consumerGroupTopics: List<String>


    @BeforeAll
    fun setUp() {
        producer = embeddedKafkaBroker.opprettKafkaProducer(consumerGroupPrefix)
        consumer = embeddedKafkaBroker.opprettKafkaConsumer(
            groupPrefix = consumerGroupPrefix,
            topics = consumerGroupTopics
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

    protected fun mockJournalføring(journalpostId: String = "123456789") {
        coEvery { k9JoarkService.journalfør(any()) } returns JournalføringsResponse(journalpostId)
    }

    protected fun mockLagreDokument(forventedeDokumenterForSletting: List<String> = listOf("123456789", "987654321")) {
        val forventetDokmentIderForSletting = forventedeDokumenterForSletting
        coEvery { k9DokumentMellomlagringService.lagreDokument(any()) }.returnsMany(forventetDokmentIderForSletting.map {
            URI(
                "http://localhost:8080/dokument/$it"
            )
        })
    }

    protected fun mockHentDokumenter() {
        coEvery { k9DokumentMellomlagringService.hentDokumenter(any(), any()) } returns listOf(
            Dokument(
                eier = DokumentEier("123"),
                content = "some value".toByteArray(),
                contentType = MediaType.APPLICATION_PDF_VALUE,
                title = "PDF fil"
            )
        )
    }

    protected fun mockBarn(aktørId: String = "11111111111", fnr: String = "05119970078") {
        coEvery { barnService.hentBarn() } returns listOf(
            BarnOppslag(
                aktørId = aktørId,
                fødselsdato = LocalDate.parse("1999-11-02"),
                identitetsnummer = fnr,
                fornavn = "Ole",
                mellomnavn = null,
                etternavn = "Dole"
            )
        )
    }

    protected fun mockSøker(aktørId: String = "1234", fnr: String = "02119970078"): Søker {
        val søker = Søker(
            aktørId = aktørId,
            fødselsdato = LocalDate.parse("1999-11-02"),
            fødselsnummer = fnr,
            fornavn = "Ola",
            mellomnavn = "Mellomnavn",
            etternavn = "Nordmann"
        )
        coEvery { søkerService.hentSøker() } returns søker
        return søker
    }
}
