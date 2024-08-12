package no.nav.brukerdialog.common

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
    const val NAV_CALL_ID = "Nav-Callid"
    const val X_CORRELATION_ID = "X-Correlation-ID"

    val NORMAL_ARBEIDSDAG = Duration.ofHours(7).plusMinutes(30)
    val OSLO_ZONE_ID = ZoneId.of("Europe/Oslo")
    val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(OSLO_ZONE_ID)
    val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(OSLO_ZONE_ID)

}
