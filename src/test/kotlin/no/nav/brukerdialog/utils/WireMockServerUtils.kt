package no.nav.brukerdialog.utils

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.brukerdialog.integrasjon.clamav.ScanResultat
import org.springframework.http.HttpStatus

object WireMockServerUtils {
    fun WireMockExtension.stubJournalføring(status: Int = 200): WireMockExtension {
        this.stubFor(
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

    fun WireMockExtension.stubFamiliePdf(status: Int = 200): WireMockExtension{
        this.stubFor(
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

    fun WireMockExtension.stubLagreDokument(
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

    fun WireMockExtension.stubSlettDokument(
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

    fun WireMockExtension.stubOpprettMellomlagring(
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

    fun WireMockExtension.stubOpprettMellomlagringLenient(
        urlPathMatching: String,
        responseStatus: HttpStatus,
        responseBodyJson: String,
    ) {
        stubFor(
            WireMock.post(WireMock.urlPathMatching(".*$urlPathMatching"))
                .willReturn(
                    WireMock.aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(responseStatus.value())
                        .withBody(responseBodyJson)
                )
        )
    }

    fun WireMockExtension.stubVirusScan(httpStatus: HttpStatus, scanResultat: ScanResultat) : WireMockExtension {
        this.stubFor(
            WireMock.put(WireMock.urlPathMatching(".*clamav-mock/scan"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(httpStatus.value())
                        .withTransformers("virus-scan")
                        .withBody(
                            //language=json
                            """
                            [
                                {
                                  "Filename": "testfil.pdf",
                                  "Result" : "${scanResultat.name}"
                                }
                            ]""".trimIndent()
                        )
                )
        )
        return this
    }
}
