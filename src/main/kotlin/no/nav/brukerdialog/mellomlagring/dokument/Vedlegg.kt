package no.nav.brukerdialog.mellomlagring.dokument

import no.nav.brukerdialog.mellomlagring.dokument.ContentTypeService.Companion.SUPPORTED_CONTENT_TYPES
import no.nav.brukerdialog.mellomlagring.dokument.ContentTypeService.Companion.detectOrNull
import no.nav.brukerdialog.validation.ParameterType
import no.nav.brukerdialog.validation.ValidationErrorResponseException
import no.nav.brukerdialog.validation.ValidationProblemDetails
import no.nav.brukerdialog.validation.Violation
import org.apache.tika.Tika

data class Vedlegg(
    val content: ByteArray,
    val contentType: String,
    val title: String,
) {
    private companion object {
        private const val MAX_VEDLEGG_SIZE_IN_MB = 10
        const val MAX_VEDLEGG_SIZE = MAX_VEDLEGG_SIZE_IN_MB * 1024 * 1024 // Enkeltfil 10 MB

        private val contentTypeService = ContentTypeService()
    }

    init {
        valider()
    }

    private fun valider() {
        val violations = mutableListOf<Violation>().apply {
            val contentType = contentType
            if (!erStøttetInnholdstype()) {
                add(
                    Violation(
                        parameterName = "vedlegg",
                        parameterType = ParameterType.FORM,
                        reason = "Kun $SUPPORTED_CONTENT_TYPES er tillatt",
                        invalidValue = contentType
                    )
                )
            }

            if (!erHvaDetSierÅVære()) {
                add(
                    Violation(
                        parameterName = "vedlegg",
                        parameterType = ParameterType.FORM,
                        reason = "Vedlegg av typen ${Tika().detectOrNull(content)} påstås ikke for å være $contentType",
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

    private fun erStøttetInnholdstype(): Boolean {
        return contentTypeService.isSupportedContentType(contentType)
    }

    private fun erHvaDetSierÅVære(): Boolean {
        return contentTypeService.isWhatItSeems(content, contentType)
    }
}
