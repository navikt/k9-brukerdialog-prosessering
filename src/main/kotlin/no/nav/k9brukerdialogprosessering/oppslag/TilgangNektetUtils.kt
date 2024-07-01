package no.nav.k9brukerdialogprosessering.oppslag

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException
import java.net.URI

class TilgangNektetException(
    melding: String,
    httpStatus: HttpStatus
) : ErrorResponseException(httpStatus, asProblemDetail(melding, httpStatus), null) {
    private companion object {
        private fun asProblemDetail(
            melding: String,
            httpStatus: HttpStatus,
        ): ProblemDetail {
            val problemDetail = ProblemDetail.forStatus(httpStatus)
            problemDetail.title = "Feil ved oppslag av søker."
            problemDetail.detail = melding

            problemDetail.type = URI("/problem-details/oppslag-soker")

            return problemDetail
        }
    }
}
