package no.nav.brukerdialog.integrasjon.familiepdf

import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.integrasjon.familiepdf.dto.FamiliePdfPostRequest
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pdf")
@Unprotected
@Profile("dev-gcp")
class PdfController(
    private val familiePdfService: FamiliePdfService,
) {
    @PostMapping
    fun lagPdf(
        @RequestBody familiePdfRequestBody: FamiliePdfPostRequest,
    ): ResponseEntity<ByteArrayResource> =
        runBlocking {
            val pdfBytes = familiePdfService.lagPdfKvittering(familiePdfRequestBody)
            val resource = ByteArrayResource(pdfBytes)
            val filnavn = familiePdfRequestBody.label
            ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=$filnavn.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(resource.byteArray.size.toLong())
                .body(resource)
        }
}
