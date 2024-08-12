package no.nav.brukerdialog.mellomlagring.dokument

import no.nav.brukerdialog.validation.ParameterType
import no.nav.brukerdialog.validation.ValidationErrorResponseException
import no.nav.brukerdialog.validation.ValidationProblemDetails
import no.nav.brukerdialog.validation.Violation
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException
import java.net.URL

const val MAX_TOTAL_VEDLEGG_SIZE_IN_MB = 24
const val MAX_TOTAL_VEDLEGG_SIZE = MAX_TOTAL_VEDLEGG_SIZE_IN_MB * 1024 * 1024 // 3 vedlegg på 25 MB

const val MAX_VEDLEGG_SIZE_IN_MB = 10
const val MAX_VEDLEGG_SIZE = MAX_VEDLEGG_SIZE_IN_MB * 1024 * 1024 // Enkeltfil 10 MB

internal fun List<Dokument>.valider(path: String, vedleggUrler: List<URL>) {
    validerTotalStørresle()
    if (size != vedleggUrler.size) {
        throw ValidationErrorResponseException(
            ValidationProblemDetails(
                violations = setOf(
                    Violation(
                        parameterName = path,
                        parameterType = ParameterType.ENTITY,
                        reason = "Mottok referanse til ${vedleggUrler.size} vedlegg, men fant kun $size vedlegg.",
                        invalidValue = vedleggUrler
                    )
                )
            )
        )
    }
}

fun List<Dokument>.validerTotalStørresle() {
    val totalSize = sumOf { it.content.size }
    if (totalSize > MAX_TOTAL_VEDLEGG_SIZE) {
        throw ErrorResponseException(
            HttpStatus.BAD_REQUEST,
            vedleggTooLargeProblemDetails(this, MAX_TOTAL_VEDLEGG_SIZE_IN_MB),
            null
        )
    }
}

fun vedleggTooLargeProblemDetails(vedlegg: List<Dokument>, tillattStørrelseIMB: Int): ProblemDetail {
    val antallVedlegg = vedlegg.size
    val faktiskStørrelseIMB = vedlegg.sumOf { it.content.size }.div(1024).div(1024).toDouble()

    return ProblemDetail.forStatus(413).apply {
        title = "attachment-too-large"
        detail =
            "De $antallVedlegg vedleggene overstiger grensen på $tillattStørrelseIMB MB (faktisk størrelse: $faktiskStørrelseIMB MB)"
    }
}
