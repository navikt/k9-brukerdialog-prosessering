package no.nav.brukerdialog.mellomlagring.soknad

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.brukerdialog.api.ytelse.Ytelse
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException
import java.time.ZonedDateTime

data class CacheRequest(
    val nøkkelPrefiks: String,
    val verdi: String,
    val ytelse: Ytelse? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val utløpsdato: ZonedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val opprettet: ZonedDateTime? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC") val endret: ZonedDateTime? = null
)

class CacheConflictException(nøkkelPrefiks: String) :
    ErrorResponseException(
        HttpStatus.CONFLICT,
        ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            "Cache med nøkkelPrefiks = $nøkkelPrefiks for person finnes allerede."
        ),
        null
    )

class CacheNotFoundException(nøkkelPrefiks: String) :
    ErrorResponseException(
        HttpStatus.NOT_FOUND,
        ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "Cache med nøkkelPrefiks = $nøkkelPrefiks for person finnes ikke."
        ),
        null
    )
