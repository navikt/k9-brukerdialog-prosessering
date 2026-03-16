package no.nav.brukerdialog

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import no.nav.brukerdialog.dittnavvarsel.DittnavVarselTopologyConfiguration.Companion.K9_DITTNAV_VARSEL_TOPIC
import no.nav.brukerdialog.integrasjon.dokarkiv.DokarkivService
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.DokarkivJournalpostResponse
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.integrasjon.ungbrukerdialogapi.UngBrukerdialogApiService
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.UngDeltakelseOpplyserService
import no.nav.brukerdialog.mellomlagring.dokument.Dokument
import no.nav.brukerdialog.mellomlagring.dokument.DokumentEier
import no.nav.brukerdialog.mellomlagring.dokument.DokumentService
import no.nav.brukerdialog.oppslag.barn.BarnOppslag
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.oppslag.soker.SøkerService
import no.nav.brukerdialog.utils.KafkaIntegrationTest
import no.nav.brukerdialog.utils.KafkaUtils.opprettKafkaConsumer
import no.nav.brukerdialog.utils.KafkaUtils.opprettKafkaProducer
import no.nav.brukerdialog.utils.TestContainers
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.ung.brukerdialog.kontrakt.oppgaver.BrukerdialogOppgaveDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretstartdato.EndretStartdatoDataDto
import no.nav.ung.deltakelseopplyser.kontrakt.deltaker.DeltakerDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.*
import no.nav.ung.deltakelseopplyser.kontrakt.register.DeltakelseDTO
import no.nav.ung.deltakelseopplyser.kontrakt.register.DeltakelseKomposittDTO
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.producer.Producer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

@KafkaIntegrationTest
@AutoConfigureMockMvc
@Import(GcsStorageTestConfiguration::class)
abstract class AbstractIntegrationTest {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @MockkBean(relaxed = true)
    protected lateinit var dokumentService: DokumentService

    @MockkBean(relaxed = false)
    protected lateinit var dokarkivService: DokarkivService


    @MockkBean
    protected lateinit var barnService: BarnService

    @MockkBean
    protected lateinit var søkerService: SøkerService

    @MockkBean
    lateinit var ungDeltakelseOpplyserService: UngDeltakelseOpplyserService

    @MockkBean
    lateinit var ungBrukerdialogApiService: UngBrukerdialogApiService

    @Autowired
    protected lateinit var mockOAuth2Server: MockOAuth2Server

    protected lateinit var producer: Producer<String, Any> // Kafka producer som brukes til å legge på kafka meldinger. Mer spesifikk, Hendelser om pp-sykt-barn
    protected lateinit var consumer: Consumer<String, String> // Kafka producer som brukes til å legge på kafka meldinger. Mer spesifikk, Hendelser om pp-sykt-barn
    protected lateinit var k9DittnavVarselConsumer: Consumer<String, String>

    protected abstract val consumerGroupPrefix: String
    protected abstract val consumerGroupTopics: List<String>


    @BeforeEach
    fun resetMocks() {
        clearMocks(
            dokumentService,
            dokarkivService,
            barnService,
            søkerService,
            ungDeltakelseOpplyserService,
            ungBrukerdialogApiService
        )
    }

    @BeforeAll
    fun setUp() {
        val bootstrapServers = TestContainers.KAFKA_CONTAINER.bootstrapServers
        producer = opprettKafkaProducer(bootstrapServers, consumerGroupPrefix)
        consumer = opprettKafkaConsumer(
            bootstrapServers = bootstrapServers,
            groupPrefix = consumerGroupPrefix,
            topics = consumerGroupTopics
        )

        k9DittnavVarselConsumer = opprettKafkaConsumer(
            bootstrapServers = bootstrapServers,
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
        coEvery { dokarkivService.journalfør(any()) } returns DokarkivJournalpostResponse(
            journalpostId,
            false,
            listOf()
        )
    }

    protected fun mockLagreDokument(forventedeDokumenterForSletting: List<String> = listOf("123456789", "987654321")) {
        val forventetDokmentIderForSletting = forventedeDokumenterForSletting
        coEvery { dokumentService.lagreDokument(any(), any(), any(), any()) }.returnsMany(
            forventetDokmentIderForSletting
        )
    }

    protected fun mockHentDokumenter() {
        coEvery { dokumentService.hentDokumenter(any(), any()) } returns listOf(
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
        oppgavetype: OppgaveType,
        oppgavetypeData: OppgavetypeDataDto
    ) {
        every { ungBrukerdialogApiService.hentOppgave(any()) } returns BrukerdialogOppgaveDto(
            UUID.randomUUID(),
            oppgavetype,
            oppgavetypeData,
            null,
            no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus.ULØST,
            ZonedDateTime.now(),

            ZonedDateTime.now(),
            null
        )

    }

    protected fun mockMarkerOppgaveSomLøst() {
        every { ungBrukerdialogApiService.markerOppgaveSomLøst(any(), any()) } returns BrukerdialogOppgaveDto(
            UUID.randomUUID(),
            OppgaveType.BEKREFT_ENDRET_STARTDATO,
            EndretStartdatoDataDto(
                LocalDate.now(),
                LocalDate.now().minusDays(30)
            ),
            null,
            no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus.LØST,
            ZonedDateTime.now(),

            ZonedDateTime.now(),
            null
        )
    }

    fun mockMarkerDeltakeleSomSøkt() {
        every { ungDeltakelseOpplyserService.markerDeltakelseSomSøkt(any()) } returns DeltakelseKomposittDTO(
            deltakelse = DeltakelseDTO(
                id = UUID.randomUUID(),
                deltaker = DeltakerDTO(
                    id = UUID.randomUUID(),
                    deltakerIdent = "12345678901",
                ),
                fraOgMed = LocalDate.now(),
                tilOgMed = null,
                søktTidspunkt = ZonedDateTime.now(),
            ),
            oppgaver = emptyList(),
        )
    }
}
