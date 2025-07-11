package no.nav.brukerdialog.mellomlagring.dokument

import no.nav.brukerdialog.utils.FilUtils.hentFil
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import java.nio.file.Files

class ContentTypeServiceTest {

    private val service = ContentTypeService()

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Bilde_3_MB.jpg",
            "test.pdf",
            "nav-logo.png"
        ]
    )
    fun `Støttet filtype returner true`(filnavn: String) {
        val fil = hentFil(filnavn)
        val contentType = Files.probeContentType(fil.toPath())
        assertTrue(service.isSupportedContentType(contentType))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.TEXT_PLAIN_VALUE,
        ]
    )
    fun `Ustøttet contentType returnerer false`(contentType: String) {
        assertFalse(service.isSupportedContentType(contentType))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Bilde_3_MB.jpg",
            "test.pdf",
            "nav-logo.png"
        ]
    )
    fun `Dersom filen er gyldig returner true`(filnavn: String) {
        val fil = hentFil(filnavn)
        val contentType = Files.probeContentType(fil.toPath())
        val resultat = service.isWhatItSeems(fil.readBytes(), contentType)
        assertTrue(resultat)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "jpeg-fil.png",
            "png-fil.jpg",
        ]
    )
    fun `Dersom filen er noe annet enn dens type returner false`(filnavn: String) {
        val fil = hentFil(filnavn)
        val contentType = Files.probeContentType(fil.toPath())
        val resultat = service.isWhatItSeems(fil.readBytes(), contentType)
        assertFalse(resultat)
    }
}
