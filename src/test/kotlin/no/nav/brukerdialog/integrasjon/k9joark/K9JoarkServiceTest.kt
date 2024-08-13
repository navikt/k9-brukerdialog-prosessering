package no.nav.brukerdialog.integrasjon.k9joark

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.integrasjon.k9joark.JournalføringsRequest
import no.nav.brukerdialog.integrasjon.k9joark.JournalføringsResponse
import no.nav.brukerdialog.integrasjon.k9joark.K9JoarkService
import no.nav.brukerdialog.integrasjon.k9joark.toUri
import no.nav.brukerdialog.meldinger.ettersendelse.domene.Søknadstype
import no.nav.brukerdialog.meldinger.felles.domene.Navn
import no.nav.brukerdialog.utils.WireMockServerUtils.stubJournalføring
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.time.ZonedDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWireMock
@EnableMockOAuth2Server
@ActiveProfiles("test")
class K9JoarkServiceTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var k9JoarkService: K9JoarkService

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Test
    fun `Gitt Journalføring av PSB, forvent riktig response`(): Unit = runBlocking {
        val journalføringsRequest = JournalføringsRequest(
            ytelse = Ytelse.PLEIEPENGER_SYKT_BARN,
            norskIdent = "12345678910",
            sokerNavn = Navn("John", "Doe", "Hansen"),
            mottatt = ZonedDateTime.now(),
            correlationId = "123456789",
            dokumentId = listOf(listOf("123", "456"))
        )
        val journalføringsResponse = JournalføringsResponse("9876543210")

        wireMockServer.stubJournalføring(
            urlPathMatching = "/v1/pleiepenge/journalforing",
            requestBodyJson = objectMapper.writeValueAsString(journalføringsRequest),
            responseStatus = HttpStatus.OK,
            correlationId = "123456789",
            responseBodyJson = objectMapper.writeValueAsString(journalføringsResponse)
        )

        val response = k9JoarkService.journalfør(journalføringsRequest)
        assertThat(response).isEqualTo(journalføringsResponse)
    }

    @Test
    fun `Gitt Journalføring av PSB Endringsmelding, forvent riktig response`(): Unit = runBlocking {
        val journalføringsRequest = JournalføringsRequest(
            ytelse = Ytelse.PLEIEPENGER_SYKT_BARN_ENDRINGSMELDING,
            norskIdent = "12345678910",
            sokerNavn = Navn("John", "Doe", "Hansen"),
            mottatt = ZonedDateTime.now(),
            dokumentId = listOf(listOf("123", "456"))
        )
        val journalføringsResponse = JournalføringsResponse("9876543210")

        wireMockServer.stubJournalføring(
            urlPathMatching = "/v1/pleiepenge/endringsmelding/journalforing",
            requestBodyJson = objectMapper.writeValueAsString(journalføringsRequest),
            responseStatus = HttpStatus.OK,
            responseBodyJson = objectMapper.writeValueAsString(journalføringsResponse)
        )

        val response = k9JoarkService.journalfør(journalføringsRequest)
        assertThat(response).isEqualTo(journalføringsResponse)
    }

    /**
     * Test for å sjekke at alle søknadstyper blir journalført på riktig url.
     */
    @ParameterizedTest
    @EnumSource(Søknadstype::class) // Alle søknadstyper
    fun `Gitt Journalføring av ettersendelse, forvent riktig response`(søknadstype: Søknadstype): Unit = runBlocking {
        val journalføringsRequest = JournalføringsRequest(
            ytelse = Ytelse.ETTERSENDELSE,
            søknadstype = søknadstype,
            norskIdent = "12345678910",
            sokerNavn = Navn("John", "Doe", "Hansen"),
            mottatt = ZonedDateTime.now(),
            dokumentId = listOf(listOf("123", "456"))
        )
        val journalføringsResponse = JournalføringsResponse("9876543210")

        wireMockServer.stubJournalføring(
            urlPathMatching = søknadstype.toUri().path,
            requestBodyJson = objectMapper.writeValueAsString(journalføringsRequest),
            responseStatus = HttpStatus.OK,
            responseBodyJson = objectMapper.writeValueAsString(journalføringsResponse)
        )

        val response = k9JoarkService.journalfør(journalføringsRequest)
        assertThat(response).isEqualTo(journalføringsResponse)
    }
}
