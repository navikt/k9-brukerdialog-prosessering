package no.nav.k9brukerdialogprosessering.utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.k9brukerdialogprosessering.common.Constants.X_CORRELATION_ID
import org.springframework.http.HttpStatus

object WireMockServerUtils {
    fun WireMockServer.stubJournalføring(
        urlPathMatching: String,
        requestBodyJson: String,
        responseStatus: HttpStatus,
        correlationId: String? = null,
        responseBodyJson: String,
    ) {
        val response = WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(responseStatus.value()).withBody(responseBodyJson)

        if (correlationId != null) {
            response.withHeader(X_CORRELATION_ID, correlationId)
        }
        stubFor(
            WireMock.post(WireMock.urlPathMatching(".*$urlPathMatching"))
                .withRequestBody(WireMock.equalToJson(requestBodyJson)).willReturn(
                    response
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
