package no.nav.k9brukerdialogprosessering.common

import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Constants {
    const val OFFSET = "offset"
    const val PARTITION = "partition"
    const val TOPIC = "topic"
    const val YTELSE = "ytelse"
    const val SOKNAD_ID_KEY = "soknad_id"
    const val CORRELATION_ID_KEY = "correlation_id"

    val NORMAL_ARBEIDSDAG = Duration.ofHours(7).plusMinutes(30)
    val ZONE_ID = ZoneId.of("Europe/Oslo")
    val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZONE_ID)
    val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZONE_ID)

}
