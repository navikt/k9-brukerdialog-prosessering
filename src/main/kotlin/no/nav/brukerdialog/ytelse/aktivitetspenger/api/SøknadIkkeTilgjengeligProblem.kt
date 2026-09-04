package no.nav.brukerdialog.ytelse.aktivitetspenger.api

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException
import java.net.URI

class SøknadIkkeTilgjengeligProblem(body: ProblemDetail = defaultProblemDetail()) :
    ErrorResponseException(HttpStatus.valueOf(body.status), body, null) {
    companion object {
        fun defaultProblemDetail(): ProblemDetail {
            val problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT)
            problemDetail.type = URI("/problem-details/soknad-ikke-tilgjengelig")
            problemDetail.title = "Søknad ikke tilgjengelig"
            problemDetail.detail = "Bruker kan ikke sende søknad nå."
            problemDetail.instance = URI("")
            problemDetail.status = HttpStatus.CONFLICT.value()
            return problemDetail
        }
    }
}
