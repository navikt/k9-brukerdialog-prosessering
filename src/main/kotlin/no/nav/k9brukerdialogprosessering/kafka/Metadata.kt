package no.nav.k9brukerdialogprosessering.kafka

data class Metadata(
    val version: Int,
    val correlationId: String,
    val soknadDialogCommitSha: String? = null
)
