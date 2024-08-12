package no.nav.brukerdialog.mellomlagring.dokument

import org.springframework.stereotype.Service

@Service
class VedleggService(
    private val k9DokumentMellomlagringService: K9DokumentMellomlagringService,
) {
    suspend fun lagreVedlegg(vedlegg: Vedlegg, personIdent: String): String {
        return k9DokumentMellomlagringService.lagreDokument(vedlegg.somDokument(personIdent)).dokumentId()
    }

    suspend fun hentVedlegg(vedleggId: String, personIdent: String): Vedlegg {
        return k9DokumentMellomlagringService.hentDokument(vedleggId, DokumentEier(personIdent)).somVedlegg()
    }

    suspend fun slettVedlegg(vedleggId: String, personIdent: String) {
        return k9DokumentMellomlagringService.slettDokument(vedleggId, DokumentEier(personIdent))
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
