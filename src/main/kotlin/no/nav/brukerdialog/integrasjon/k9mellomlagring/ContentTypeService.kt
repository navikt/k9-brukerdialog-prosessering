package no.nav.brukerdialog.integrasjon.k9mellomlagring

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service

@Service
class ContentTypeService {
    companion object {
        private val logger = LoggerFactory.getLogger(ContentTypeService::class.java)

        val JSON = MediaType.APPLICATION_JSON
        val PDF = MediaType.APPLICATION_PDF
        val XML = MediaType.APPLICATION_XML
        val PNG = MediaType.IMAGE_PNG
        val JPEG = MediaType.IMAGE_JPEG
    }

    private val supportedApplicationContentTypes = listOf(
        JSON,
        PDF,
        XML
    )
    private val supportedImageContentTypes = listOf(
        PNG,
        JPEG
    )

    fun isSupported(contentType: String): Boolean {
        val parsedContentType = parseOrNull(contentType)
        return supportedImageContentTypes.contains(parsedContentType) || supportedApplicationContentTypes.contains(
            parsedContentType
        )
    }

    fun isSupportedImage(contentType: String): Boolean =
        supportedImageContentTypes.contains(parseOrNull(contentType))

    fun isSupportedApplication(contentType: String): Boolean =
        supportedApplicationContentTypes.contains(parseOrNull(contentType))

    private fun parseOrNull(contentType: String): MediaType? {
        return runCatching {
            logger.trace("Parsing content type: $contentType to MediaType")
            MediaType.parseMediaType(contentType)
        }.getOrNull()
    }
}
