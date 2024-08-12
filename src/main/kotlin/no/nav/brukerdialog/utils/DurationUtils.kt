package no.nav.brukerdialog.utils

import no.nav.brukerdialog.common.Constants.NORMAL_ARBEIDSDAG
import java.time.Duration

object DurationUtils {
    internal fun Duration.somTekst(avkort: Boolean = true): String {
        if(this.isZero) return "0 timer og 0 minutter"

        val timer = seconds / 3600
        val minutter = (seconds % 3600) / 60
        val timerTeskst = when (timer) {
            0L -> ""
            1L -> "$timer time"
            else -> "$timer timer"
        }
        val minutterTekst = when (minutter) {
            0L -> ""
            1L -> "$minutter minutt"
            else -> "$minutter minutter"
        }

        val mellomTekst = if (timerTeskst.isNotBlank() && minutterTekst.isNotBlank()) " og " else ""
        val avkortetTekst = if (this > NORMAL_ARBEIDSDAG && avkort) " (avkortet til 7 timer og 30 minutter)" else ""

        return "$timerTeskst$mellomTekst$minutterTekst$avkortetTekst"
    }

    fun Duration.tilString(): String = when (this.toMinutesPart()) {
        0 -> "${this.timer()} timer"
        else -> "${this.timer()} timer og ${this.toMinutesPart()} minutter"
    }

    fun Duration.timer() = (this.toDaysPart() * 24) + this.toHoursPart()
}
