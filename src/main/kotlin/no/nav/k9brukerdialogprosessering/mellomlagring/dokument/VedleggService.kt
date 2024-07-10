package no.nav.k9brukerdialogprosessering.mellomlagring.dokument

import org.springframework.stereotype.Service

@Service
class VedleggService(
    private val k9DokumentMellomlagringService: K9DokumentMellomlagringService,
) {
    suspend fun lagreVedlegg(vedlegg: Vedlegg, personIdent: String): String {
        return k9DokumentMellomlagringService.lagreDokument(vedlegg.somDokument(personIdent)).dokumentId()
    }

    fun hentVedlegg() {
        TODO("Not yet implemented")
    }

    fun slettVedlegg() {
        TODO("Not yet implemented")
    }

    private fun Vedlegg.somDokument(personIdent: String) = Dokument(
        eier = DokumentEier(personIdent),
        content = content,
        contentType = contentType,
        title = title
    )
}
