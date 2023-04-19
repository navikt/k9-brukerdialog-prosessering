package no.nav.k9brukerdialogprosessering.common

data class Navn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String
) {
    override fun toString(): String {
        return "$fornavn ${mellomnavn ?: ""} $etternavn"
    }
}
