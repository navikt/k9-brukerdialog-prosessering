package no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid

import jakarta.validation.Valid
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.brukerdialog.utils.DateUtils.ikkeErHelg
import java.time.LocalDate
import java.util.*
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

data class Arbeidsforhold(
    val normalarbeidstid: NormalArbeidstid,
    @field:Valid val arbeidIPeriode: no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidIPeriode
) {

    companion object {
        internal fun k9ArbeidstidInfoMedNullTimer(fraOgMed: LocalDate, tilOgMed: LocalDate) = ArbeidstidInfo()
            .medPerioder(
                mapOf(
                    Periode(fraOgMed, tilOgMed) to ArbeidstidPeriodeInfo()
                        .medFaktiskArbeidTimerPerDag(NULL_TIMER)
                        .medJobberNormaltTimerPerDag(NULL_TIMER)
                )
            )

    }

    fun tilK9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        return when (arbeidIPeriode.type) {
            ArbeidIPeriodeType.ARBEIDER_VANLIG -> arbeiderVanlig(fraOgMed, tilOgMed)
            ArbeidIPeriodeType.ARBEIDER_IKKE -> arbeiderIkke(fraOgMed, tilOgMed)
            ArbeidIPeriodeType.ARBEIDER_REDUSERT -> arbeiderRedusert(fraOgMed, tilOgMed)
        }
    }

    fun arbeiderRedusert(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidIPeriode.redusertArbeid) { "For å regne ut redusert arbeid må den være satt." }
        return when (arbeidIPeriode.redusertArbeid.type) {
            RedusertArbeidstidType.PROSENT_AV_NORMALT -> arbeiderProsentAvNormalt(fraOgMed, tilOgMed)
            RedusertArbeidstidType.TIMER_I_SNITT_PER_UKE -> arbeiderTimerISnittPerUke(fraOgMed, tilOgMed)
            RedusertArbeidstidType.ULIKE_UKER_TIMER -> arbeidsukerUlikeTimer(fraOgMed, tilOgMed)
        }
    }

    private fun arbeiderVanlig(fraOgMed: LocalDate, tilOgMed: LocalDate) =
        arbeiderVanligMedNormaltimerSomSnitt(fraOgMed, tilOgMed)

    private fun arbeiderVanligMedNormaltimerSomSnitt(
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
    ): ArbeidstidInfo {
        val arbeidstidInfo = ArbeidstidInfo()
        arbeidstidInfo.medPerioder(
            mapOf(
                Periode(fraOgMed, tilOgMed) to ArbeidstidPeriodeInfo()
                    .medJobberNormaltTimerPerDag(normalarbeidstid.timerPerDagFraSnitt())
                    .medFaktiskArbeidTimerPerDag(normalarbeidstid.timerPerDagFraSnitt())
            )
        )
        return arbeidstidInfo
    }

    private fun arbeiderIkke(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        val arbeidstidInfo = ArbeidstidInfo()

        val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
            .medJobberNormaltTimerPerDag(normalarbeidstid.timerPerDagFraSnitt())
            .medFaktiskArbeidTimerPerDag(NULL_TIMER)

        arbeidstidInfo.medPerioder(
            mapOf(Periode(fraOgMed, tilOgMed) to arbeidstidPeriodeInfo)
        )

        return arbeidstidInfo
    }

    private fun arbeiderProsentAvNormalt(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidIPeriode.redusertArbeid) { "For å regne ut redusert arbeid må den være satt." }
        requireNotNull(arbeidIPeriode.redusertArbeid.prosentAvNormalt) { "For å regne ut arbeid fra prosentAvNormalt må den være satt." }
        val normaltTimerPerdag = normalarbeidstid.timerPerDagFraSnitt()
        val faktiskTimerPerDag = normaltTimerPerdag.toKotlinDuration()
            .div(100)
            .times(arbeidIPeriode.redusertArbeid.prosentAvNormalt)
            .toJavaDuration()

        val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
            .medJobberNormaltTimerPerDag(normaltTimerPerdag)
            .medFaktiskArbeidTimerPerDag(faktiskTimerPerDag)

        return ArbeidstidInfo().medPerioder(
            mapOf(Periode(fraOgMed, tilOgMed) to arbeidstidPeriodeInfo)
        )
    }

    private fun arbeiderTimerISnittPerUke(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidIPeriode.redusertArbeid) { "For å regne ut redusert arbeid må den være satt." }
        requireNotNull(arbeidIPeriode.redusertArbeid.timerPerUke) { "For å regne ut arbeid fra timerPerUke må den være satt." }
        val normaltTimerPerDag = normalarbeidstid.timerPerDagFraSnitt()
        val faktiskTimerPerDag = arbeidIPeriode.redusertArbeid.timerPerUke.dividedBy(DAGER_PER_UKE)

        val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
            .medJobberNormaltTimerPerDag(normaltTimerPerDag)
            .medFaktiskArbeidTimerPerDag(faktiskTimerPerDag)

        return ArbeidstidInfo().medPerioder(
            mapOf(Periode(fraOgMed, tilOgMed) to arbeidstidPeriodeInfo)
        )
    }

    internal fun arbeidsukerUlikeTimer(
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
    ): ArbeidstidInfo {
        requireNotNull(arbeidIPeriode.redusertArbeid) { "For å regne ut redusert arbeid må den være satt." }
        requireNotNull(arbeidIPeriode.redusertArbeid.arbeidsuker) { "For å regne ut arbeid fra arbeidsuker må den være satt." }
        val arbeidstidInfo = ArbeidstidInfo()

        arbeidIPeriode.redusertArbeid.arbeidsuker.map { arbeidsuke: no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidsUke ->
            requireNotNull(normalarbeidstid.timerPerUkeISnitt) { "normalarbeidstid.timerPerUkeISnitt må være satt." }
            val periodeUtenHelg: SortedSet<LocalDate> = arbeidsuke.periodeUtenHelg()
            val k9Periode = Periode(periodeUtenHelg.first(), periodeUtenHelg.last())
            val normaltArbeidstimerPerDag = normalarbeidstid.timerPerUkeISnitt.dividedBy(DAGER_PER_UKE)

            val ukedager = periodeUtenHelg.size.toLong()
            val faktiskArbeidstimerPerDag = arbeidsuke.timer.dividedBy(ukedager)

            val arbeidstidPeriodeInfo = ArbeidstidPeriodeInfo()
                .medJobberNormaltTimerPerDag(normaltArbeidstimerPerDag)
                .medFaktiskArbeidTimerPerDag(faktiskArbeidstimerPerDag)

            Pair(k9Periode, arbeidstidPeriodeInfo)
        }
            .forEach { (periode, arbeidstidPeriodeInfo) ->
                //Tar høyde for at enkeltdager kan være utenfor fraOgMed/tilOgMed som kan være justert pga start/slutt innenfor søknadsperioden
                if (periode.fraOgMed.erInnenforPerioden(fraOgMed, tilOgMed)) {
                    arbeidstidInfo.leggeTilPeriode(periode, arbeidstidPeriodeInfo)
                }
            }

        return arbeidstidInfo
    }

    private fun no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidsUke.periodeUtenHelg(): SortedSet<LocalDate> {
        return periode.fraOgMed.datesUntil(periode.tilOgMed.plusDays(1))
            .filter { it.ikkeErHelg() }
            .toList()
            .toSortedSet()
    }

    private fun LocalDate.erInnenforPerioden(fraOgMed: LocalDate, tilOgMed: LocalDate) =
        this.isEqual(fraOgMed) || this.isEqual(tilOgMed) || (this.isAfter(fraOgMed) && this.isBefore(tilOgMed))
}
