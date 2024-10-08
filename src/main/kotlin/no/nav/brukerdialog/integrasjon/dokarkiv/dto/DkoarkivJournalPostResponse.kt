package no.nav.brukerdialog.integrasjon.dokarkiv.dto

data class DokarkivJournalpostResponse(
    val journalpostId: String,
    val journalpostFerdigstilt: Boolean,
    val dokumenter: List<DokumentInfo>,
)

data class DokumentInfo(
    val dokumentInfoId: String,
)
