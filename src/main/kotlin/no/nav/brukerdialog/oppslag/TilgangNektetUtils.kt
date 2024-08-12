package no.nav.brukerdialog.oppslag

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
            problemDetail.title = "Tilgang til personopplysninger nektet fra k9-selvbetjening-oppslag"
            problemDetail.detail = melding

            problemDetail.type = URI("/problem-details/tilgang-nektet")

            return problemDetail
        }
    }
}
