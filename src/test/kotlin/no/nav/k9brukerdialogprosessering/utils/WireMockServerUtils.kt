package no.nav.k9brukerdialogprosessering.utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus

object WireMockServerUtils {
    fun WireMockServer.stubJournalføring(
        urlPathMatching: String,
        requestBodyJson: String,
        responseStatus: HttpStatus,
        responseBodyJson: String,
    ) {
        stubFor(
            WireMock.post(WireMock.urlPathMatching(".*$urlPathMatching"))
                .withRequestBody(WireMock.equalToJson(requestBodyJson)).willReturn(
                    WireMock.aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(responseStatus.value()).withBody(responseBodyJson)
                )
        )
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
}
