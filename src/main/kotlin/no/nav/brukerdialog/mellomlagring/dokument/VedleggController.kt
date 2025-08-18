package no.nav.brukerdialog.mellomlagring.dokument

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.NotBlank
import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.config.Issuers
import no.nav.brukerdialog.utils.TokenUtils.personIdent
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.util.StringUtils
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/vedlegg")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class VedleggController(
    private val vedleggService: VedleggService,
    private val tokenValidationContextHolder: SpringTokenValidationContextHolder,
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun lagreVedlegg(
        request: HttpServletRequest,
        @RequestParam("vedlegg") vedleggFile: MultipartFile,
    ): ResponseEntity<Unit> = runBlocking {
        val personIdent = tokenValidationContextHolder.personIdent()
        val vedlegg = vedleggFile.somVedlegg()
        val dokumentId: String = vedleggService.lagreVedlegg(vedlegg, personIdent)

        ResponseEntity.created(
            ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(dokumentId)
                .toUri()
        )
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION)
            .build()
    }

    @GetMapping("/{vedleggId}")
    fun hentVedlegg(@NotBlank @PathVariable vedleggId: String): ResponseEntity<ByteArray> = runBlocking {
        val personIdent = tokenValidationContextHolder.personIdent()
        kotlin.runCatching {
            vedleggService.hentVedlegg(vedleggId, personIdent).let {
                val content = it.content
                ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(it.contentType))
                    .contentLength(content.size.toLong())
                    .body(content)
            }
        }.fold(
            onSuccess = { it },
            onFailure = { cause ->
                if (cause is HttpClientErrorException.NotFound)
                    throw ErrorResponseException(
                        HttpStatus.NOT_FOUND,
                        ProblemDetail.forStatusAndDetail(
                            HttpStatus.NOT_FOUND,
                            "Vedlegg med id $vedleggId ble ikke funnet"
                        ),
                        cause
                    )

                throw ErrorResponseException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Feil ved henting av vedlegg"
                    ),
                    cause
                )
            }
        )
    }

    @DeleteMapping("/{vedleggId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun slettVedlegg(@NotBlank @PathVariable vedleggId: String): Unit = runBlocking {
        val personIdent = tokenValidationContextHolder.personIdent()
        kotlin.runCatching { vedleggService.slettVedlegg(vedleggId, personIdent) }
            .fold(
                onSuccess = {},
                onFailure = { cause ->
                    throw ErrorResponseException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ProblemDetail.forStatusAndDetail(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Feil ved sletting av vedlegg"
                        ),
                        cause
                    )
                }
            )
    }

    private fun MultipartFile.somVedlegg(): Vedlegg {
        // Konverter multipartfile til Dokument
        val contentType = contentType!!
        val filnavn = StringUtils.cleanPath(originalFilename ?: "")

        return Vedlegg(
            content = bytes,
            contentType = contentType,
            title = filnavn
        )
    }
}
