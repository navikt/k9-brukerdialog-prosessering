package no.nav.k9brukerdialogprosessering.utils

import no.nav.fpsak.tidsserie.LocalDateInterval
import no.nav.fpsak.tidsserie.LocalDateTimeline
import no.nav.k9.søknad.felles.type.Periode
import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate
import java.time.Month
import java.time.ZonedDateTime
import java.time.temporal.WeekFields
import java.util.*

object DateUtils {

    internal fun antallVirkedager(fraOgMed: LocalDate, tilOgMed: LocalDate): Long =
        fraOgMed.datesUntil(tilOgMed.plusDays(1)).toList()
            .filterNot { it.dayOfWeek == SATURDAY || it.dayOfWeek == SUNDAY }
            .size.toLong()

    fun List<LocalDate>.periodeList() = map { Periode(it, it) }

    internal fun ZonedDateTime.somNorskDag() = dayOfWeek.somNorskDag()

    internal fun DayOfWeek.somNorskDag() = when (this) {
        MONDAY -> "Mandag"
        TUESDAY -> "Tirsdag"
        WEDNESDAY -> "Onsdag"
        THURSDAY -> "Torsdag"
        FRIDAY -> "Fredag"
        SATURDAY -> "Lørdag"
        else -> "Søndag"
    }

    internal fun Month.somNorskMåned() = when (this) {
        Month.JANUARY -> "Januar"
        Month.FEBRUARY -> "Februar"
        Month.MARCH -> "Mars"
        Month.APRIL -> "April"
        Month.MAY -> "Mai"
        Month.JUNE -> "Juni"
        Month.JULY -> "Juli"
        Month.AUGUST -> "August"
        Month.SEPTEMBER -> "September"
        Month.OCTOBER -> "Oktober"
        Month.NOVEMBER -> "November"
        Month.DECEMBER -> "Desember"
    }

    fun List<LocalDate>.grupperMedUker() = groupBy {
        it.ukeNummer()
    }

    fun LocalDate.antallUkerGittÅr(): Int {
        val cal: Calendar = Calendar.getInstance()
        cal.setWeekDate(year, monthValue, dayOfWeek.value)
        return cal.weeksInWeekYear
    }

    fun LocalDate.ukeNummer(): Int {
        val cal: Calendar = Calendar.getInstance()
        cal.setWeekDate(year, monthValue, dayOfWeek.value)
        val uketall = get(WeekFields.of(Locale.getDefault()).weekOfYear())
        val antallUker = antallUkerGittÅr()
        return when {
            antallUker == 53 -> uketall
            antallUker < 53 && uketall > 52 -> 1
            else -> uketall
        }
    }

    fun List<LocalDate>.somLocalDateTimeline(): LocalDateTimeline<Boolean> {
        val perioder = periodeList()
        return no.nav.k9.søknad.TidsserieUtils.toLocalDateTimeline(perioder)
    }

    fun List<LocalDate>.grupperSammenHengendeDatoer(): NavigableSet<LocalDateInterval> =
        somLocalDateTimeline().localDateIntervals
}
