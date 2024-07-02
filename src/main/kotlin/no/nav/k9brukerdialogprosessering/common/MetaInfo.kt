package no.nav.k9brukerdialogprosessering.common

data class MetaInfo(
    val version: Int = 1,
    val correlationId: String,
    val soknadDialogCommitSha: String? = null,
)
