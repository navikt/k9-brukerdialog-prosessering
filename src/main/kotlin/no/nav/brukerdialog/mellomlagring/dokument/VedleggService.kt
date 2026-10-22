package no.nav.brukerdialog.mellomlagring.dokument

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.stereotype.Service
import org.springframework.web.ErrorResponseException

@Service
class VedleggService(
    private val dokumentService: DokumentService,
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(VedleggService::class.java)
    }

    suspend fun lagreVedlegg(vedlegg: Vedlegg, personIdent: String): String {
        return dokumentService.lagreDokument(
            dokument = vedlegg.somDokument(personIdent),
            dokumentEier = DokumentEier(personIdent),
            medHold = false,
            skannForVirus = true
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

    // Vedlegg som eies av en annen person enn personIdent, regnes som ikke funnet.
    suspend fun finnesVedlegg(vedleggIder: List<String>, personIdent: String): List<VedleggEksistens> {
        logger.info("Sjekker om {} vedlegg finnes.", vedleggIder.size)
        val resultat = dokumentService.finnesDokumenter(vedleggIder, DokumentEier(personIdent))
            .map { (vedleggId, finnes) -> VedleggEksistens(vedleggId = vedleggId, finnes = finnes) }

        val antallIkkeFunnet = resultat.count { !it.finnes }
        if (antallIkkeFunnet > 0) logger.info("{} av {} vedlegg ble ikke funnet.", antallIkkeFunnet, resultat.size)

        return resultat
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
