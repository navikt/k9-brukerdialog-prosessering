package no.nav.k9brukerdialogprosessering.mellomlagring.dokument

import no.nav.k9brukerdialogprosessering.validation.ParameterType
import no.nav.k9brukerdialogprosessering.validation.ValidationErrorResponseException
import no.nav.k9brukerdialogprosessering.validation.ValidationProblemDetails
import no.nav.k9brukerdialogprosessering.validation.Violation
import org.springframework.http.MediaType

data class Vedlegg(
    val content: ByteArray,
    val contentType: String,
    val title: String
) {
    private companion object {
        private const val MAX_VEDLEGG_SIZE_IN_MB = 10
        const val MAX_VEDLEGG_SIZE = MAX_VEDLEGG_SIZE_IN_MB * 1024 * 1024 // Enkeltfil 10 MB

        val supportedContentTypes = listOf(
            MediaType.APPLICATION_PDF_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
        )

        private fun isSupportedContentType(contentType: String): Boolean {
            return supportedContentTypes.any { it.equals(contentType, ignoreCase = true) }
        }
    }

    init {
        valider()
    }

    private fun valider() {
        val violations = mutableListOf<Violation>().apply {
            val contentType = contentType
            if (!isSupportedContentType(contentType)) {
                add(
                    Violation(
                        parameterName = "vedlegg",
                        parameterType = ParameterType.FORM,
                        reason = "Kun ${MediaType.APPLICATION_PDF_VALUE}, ${MediaType.IMAGE_JPEG_VALUE}, og ${MediaType.IMAGE_PNG_VALUE} er tillatt",
                        invalidValue = contentType
                    )
                )
            }

            if (content.isEmpty()) {
                add(
                    Violation(
                        parameterName = "vedlegg",
                        parameterType = ParameterType.FORM,
                        reason = "Vedlegg kan ikke være tom",
                        invalidValue = null
                    )
                )
            }

            if (content.size > MAX_VEDLEGG_SIZE) {
                add(
                    Violation(
                        parameterName = "vedlegg",
                        parameterType = ParameterType.FORM,
                        reason = "Vedlegg kan ikke være større enn ${MAX_VEDLEGG_SIZE_IN_MB} MB",
                        invalidValue = content.size
                    )
                )
            }
        }
        if (violations.isNotEmpty()) {
            throw ValidationErrorResponseException(ValidationProblemDetails(violations = violations.toSet()))
        }
    }
}
