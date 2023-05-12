package no.nav.k9brukerdialogprosessering.utils

object StringUtils {
    fun String.storForbokstav(): String = split(" ").joinToString(" ") { name: String ->
        name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
