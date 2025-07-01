package no.nav.brukerdialog.mellomlagring.dokument

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.stereotype.Service
import org.springframework.web.ErrorResponseException

@Service
class VedleggService(
    private val dokumentService: DokumentService,
) {

    suspend fun lagreVedlegg(vedlegg: Vedlegg, personIdent: String): String {
        return dokumentService.lagreDokument(
            dokument = vedlegg.somDokument(personIdent),
            dokumentEier = DokumentEier(personIdent),
            medHold = false,
            skannForVirus = false // TODO: Skru på virusskanning før prodsetting.
        )
    }

    suspend fun hentVedlegg(vedleggId: String, personIdent: String): Vedlegg {
        val dokument = dokumentService.hentDokument(vedleggId, DokumentEier(personIdent))
            ?: throw ErrorResponseException(
                HttpStatus.NOT_FOUND,
                ProblemDetail.forStatusAndDetail(
                    HttpStatus.NOT_FOUND,
                    "Vedlegg med ID $vedleggId ble ikke funnet."
                ),
                null
            )

        return dokument.somVedlegg()
    }

    suspend fun slettVedlegg(vedleggId: String, personIdent: String) {
        dokumentService.slettDokument(vedleggId, DokumentEier(personIdent))
    }

    private fun Vedlegg.somDokument(personIdent: String) = Dokument(
        eier = DokumentEier(personIdent),
        content = content,
        contentType = contentType,
        title = title
    )

    private fun Dokument.somVedlegg() = Vedlegg(
        content = content,
        contentType = contentType,
        title = title
    )
}
