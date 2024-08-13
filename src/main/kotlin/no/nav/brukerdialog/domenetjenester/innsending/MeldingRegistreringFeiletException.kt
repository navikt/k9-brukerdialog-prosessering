package no.nav.brukerdialog.domenetjenester.innsending

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException

class MeldingRegistreringFeiletException(melding: String) : ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, asProblemDetail(melding), null) {
    private companion object {
        private fun asProblemDetail(
            melding: String,
        ): ProblemDetail {
            val problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
            problemDetail.title = "Feil ved registrering av melding"
            problemDetail.detail = melding

            return problemDetail
        }
    }
}
