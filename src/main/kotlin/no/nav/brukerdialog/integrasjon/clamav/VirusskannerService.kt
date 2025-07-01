package no.nav.brukerdialog.integrasjon.clamav

import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.ErrorResponseException
import org.springframework.web.client.RestTemplate

@Service
class VirusskannerService(
    private val clamAvRestTemplate: RestTemplate,
) : VirusSkann {
    private companion object {
        private val logger = LoggerFactory.getLogger(VirusskannerService::class.java)
    }

    override fun skann(fil: ByteArray) {
        kotlin.runCatching {
            clamAvRestTemplate.exchange(
                "/scan",
                HttpMethod.PUT,
                HttpEntity(fil),
                object : ParameterizedTypeReference<List<ClamAVScanResponse>>() {}
            )
        }.fold(
            onSuccess = { response: ResponseEntity<List<ClamAVScanResponse>> -> forsikreIkkeInfisert(response) },
            onFailure = { error: Throwable ->
                logger.error("Feil ved virusscan. ${error.message}", error)
                throw ErrorResponseException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Feil ved virusscan: ${error.message}"
                    ),
                    error
                )
            }
        )
    }

    private fun forsikreIkkeInfisert(response: ResponseEntity<List<ClamAVScanResponse>>) {
        val clamAVScanResponse = response.body!!.first()
        if (clamAVScanResponse.result != ScanResultat.OK) {
            throw ErrorResponseException(
                HttpStatus.BAD_REQUEST,
                ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST,
                    "Virusskann av fil med navn ${clamAVScanResponse.fileName} avdekket at den var infisert."
                ),
                null
            )
        }
    }
}
