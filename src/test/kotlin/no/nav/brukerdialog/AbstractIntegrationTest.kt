package no.nav.brukerdialog

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import no.nav.brukerdialog.dittnavvarsel.DittnavVarselTopologyConfiguration.Companion.K9_DITTNAV_VARSEL_TOPIC
import no.nav.brukerdialog.integrasjon.dokarkiv.DokarkivService
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.DokarkivJournalpostResponse
import no.nav.brukerdialog.integrasjon.k9mellomlagring.K9DokumentMellomlagringService
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.UngDeltakelseOpplyserService
import no.nav.brukerdialog.mellomlagring.dokument.Dokument
import no.nav.brukerdialog.mellomlagring.dokument.DokumentEier
import no.nav.brukerdialog.oppslag.barn.BarnOppslag
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.oppslag.soker.SøkerService
import no.nav.brukerdialog.utils.KafkaIntegrationTest
import no.nav.brukerdialog.utils.KafkaUtils.opprettKafkaConsumer
import no.nav.brukerdialog.utils.KafkaUtils.opprettKafkaProducer
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.ung.deltakelseopplyser.kontrakt.deltaker.DeltakerDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.OppgaveDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.OppgaveStatus
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.Oppgavetype
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.OppgavetypeDataDTO
import no.nav.ung.deltakelseopplyser.kontrakt.register.DeltakelseOpplysningDTO
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
import java.time.ZonedDateTime
import java.util.*

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

    @MockkBean(relaxed = false)
    protected lateinit var dokarkivService: DokarkivService


    @MockkBean
    protected lateinit var barnService: BarnService

    @MockkBean
    protected lateinit var søkerService: SøkerService

    @MockkBean
    lateinit var ungDeltakelseOpplyserService: UngDeltakelseOpplyserService

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
        coEvery { dokarkivService.journalfør(any()) } returns DokarkivJournalpostResponse(journalpostId, false, listOf())
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
                fødselsdato = LocalDate.parse("2023-11-02"),
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

    protected fun mockHentingAvOppgave(
        oppgavetype: Oppgavetype,
        oppgavetypeData: OppgavetypeDataDTO
    ) {
        every { ungDeltakelseOpplyserService.hentOppgaveForDeltakelse(any()) } returns OppgaveDTO(
            oppgaveReferanse = UUID.randomUUID(),
            oppgavetype = oppgavetype,
            oppgavetypeData = oppgavetypeData,
            status = OppgaveStatus.ULØST,
            bekreftelse = null,
            opprettetDato = ZonedDateTime.now(),
            løstDato = null,
            åpnetDato = null,
            lukketDato = null
        )
    }

    fun mockMarkerDeltakeleSomSøkt() {
        every { ungDeltakelseOpplyserService.markerDeltakelseSomSøkt(any()) } returns DeltakelseOpplysningDTO(
            id = UUID.randomUUID(),
            deltaker = DeltakerDTO(
                id = UUID.randomUUID(),
                deltakerIdent = "12345678901",
            ),
            fraOgMed = LocalDate.now(),
            tilOgMed = null,
            søktTidspunkt = ZonedDateTime.now(),
            oppgaver = emptyList(),
        )
    }
}
