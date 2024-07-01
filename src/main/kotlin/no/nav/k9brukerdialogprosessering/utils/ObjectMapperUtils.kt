package no.nav.k9brukerdialogprosessering.utils

import com.fasterxml.jackson.databind.ObjectMapper

object ObjectMapperUtils {
    fun ObjectMapper.somJson(any: Any) = writeValueAsString(this)
}
