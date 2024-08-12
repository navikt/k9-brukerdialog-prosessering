package no.nav.k9brukerdialogprosessering.utils

import org.springframework.stereotype.Component
import java.util.*

@Component
class CallIdGenerator {
    fun create(): String {
        return UUID.randomUUID().toString()
    }
}
