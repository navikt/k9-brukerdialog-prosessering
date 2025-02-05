package no.nav.brukerdialog.utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus

object WireMockServerUtils {
    fun WireMockServer.stubJournalføring(status: Int = 200): WireMockServer{
        WireMock.stubFor(
            WireMock.post(
                WireMock.urlMatching(".*/dokarkiv-mock/rest/journalpostapi/v1/journalpost"))
                .withRequestBody(WireMock.matchingJsonPath("$.tilleggsopplysninger[0].nokkel", WireMock.equalTo("k9.kilde")))
                .withRequestBody(WireMock.matchingJsonPath("$.tilleggsopplysninger[0].verdi", WireMock.equalTo("DIGITAL")))
                .withRequestBody(WireMock.matchingJsonPath("$.tilleggsopplysninger[1].nokkel", WireMock.equalTo("k9.type")))
                .withRequestBody(WireMock.matchingJsonPath("$.tilleggsopplysninger[1].verdi", WireMock.matching("SØKNAD|MELDING|ETTERSENDELSE|ENDRING")))
                .withRequestBody(WireMock.matchingJsonPath("$.eksternReferanseId", WireMock.equalTo("123156")))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withTransformers("dokarkiv")
                )
        )
        return this
    }

    fun WireMockServer.stubFamiliePdf(status: Int = 200): WireMockServer{
        WireMock.stubFor(
            WireMock.post(
                WireMock.urlMatching(".*/familie-pdf-mock/api/v1/pdf/opprett-pdf"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/pdf")
                        .withBody("mocked-pdf-innhold".toByteArray())
                )
        )
        return this
    }

    fun WireMockServer.stubLagreDokument(
        urlPathMatching: String,
        requestBodyJson: String,
        responseStatus: HttpStatus,
        responseLocationHeaderPath: String,
    ) {
        stubFor(
            WireMock.post(WireMock.urlPathMatching(".*$urlPathMatching"))
                .withRequestBody(WireMock.equalToJson(requestBodyJson)).willReturn(
                    WireMock.aResponse().withHeader("Content-Type", "application/json")
                        .withHeader("Location", "${this.baseUrl()}$responseLocationHeaderPath")
                        .withStatus(responseStatus.value())
                )
        )
    }

    fun WireMockServer.stubSlettDokument(
        urlPathMatching: String,
        requestBodyJson: String,
        responseStatus: HttpStatus,
    ) {
        stubFor(
            WireMock.delete(WireMock.urlPathMatching(".*$urlPathMatching"))
                .withRequestBody(WireMock.equalToJson(requestBodyJson)).willReturn(
                    WireMock.aResponse().withStatus(responseStatus.value())
                )
        )
    }

    fun WireMockServer.stubOpprettMellomlagring(
        urlPathMatching: String,
        requestBodyJson: String,
        responseStatus: HttpStatus,
        responseBodyJson: String,
    ) {
        stubFor(
            WireMock.post(WireMock.urlPathMatching(".*$urlPathMatching"))
                .withRequestBody(WireMock.equalToJson(requestBodyJson)).willReturn(
                    WireMock.aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(responseStatus.value())
                        .withBody(responseBodyJson)
                )
        )
    }
}
