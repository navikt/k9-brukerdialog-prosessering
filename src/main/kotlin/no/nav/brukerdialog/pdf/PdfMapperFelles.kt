package no.nav.brukerdialog.pdf

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.ArbeidIPeriodeType
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsforhold
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.RedusertArbeidstidType
import no.nav.brukerdialog.utils.DateUtils.somNorskMåned
import no.nav.brukerdialog.utils.DateUtils.ukeNummer
import no.nav.brukerdialog.utils.DurationUtils.somTekst
import no.nav.brukerdialog.utils.DurationUtils.tilString
import no.nav.brukerdialog.utils.StringUtils.storForbokstav
import no.nav.helse.felles.Enkeltdag
import java.time.Duration
import java.time.LocalDate

data class MånedData(
    val år: Int,
    val navnPåMåned: String,
    val uker: List<UkeData>,
)

data class UkeData(
    val uke: Int,
    val dager: List<DagData>,
)

data class DagData(
    val dato: String,
    val tid: String,
)

data class SpørsmålOgSvar(
    val spørsmål: String,
    val svar: String?,
)

fun tilSpørsmålOgSvar(
    spørsmål: String?,
    svar: Any?,
): SpørsmålOgSvar? {
    var svarSomStreng: String? = null

    if (spørsmål == null) return null

    svarSomStreng =
        when (svar) {
            is String -> svar
            is Enum<*> -> svar.toString()
            is Boolean -> konverterBooleanTilSvar(svar)
            is Duration -> svar.tilString()
            is LocalDate -> DATE_FORMATTER.format(svar)
            is Int -> svar.toString()
            is Arbeidsforhold -> arbeidIPerioden(svar)
            else -> null
        }

    return svarSomStreng?.let { SpørsmålOgSvar(spørsmål, it) }
}

fun lagVerdiElement(spørsmålOgSvar: SpørsmålOgSvar?): VerdilisteElement? =

    if (spørsmålOgSvar == null) {
        null
    } else {
        VerdilisteElement(label = spørsmålOgSvar.spørsmål, verdi = spørsmålOgSvar.svar)
    }

fun arbeidIPerioden(arbeidsforhold: Arbeidsforhold?): String {
    val arbeidIPeriode = arbeidsforhold?.arbeidIPeriode ?: return "Ingen arbeidsforhold registrert"

    return when (arbeidIPeriode.type) {
        ArbeidIPeriodeType.IKKE_BESVART -> "Ikke besvart"
        ArbeidIPeriodeType.ARBEIDER_IKKE -> "Jobber ikke i perioden."
        ArbeidIPeriodeType.ARBEIDER_VANLIG -> "Jobber som normalt i perioden. Har ikke fravær fra jobb"
        ArbeidIPeriodeType.ARBEIDER_REDUSERT -> {
            val redusertArbeid = arbeidIPeriode.redusertArbeid ?: return "Jobber kombinerer pleiepeneger med arbeid i perioden."

            val redusertInfo =
                when (redusertArbeid.type) {
                    RedusertArbeidstidType.PROSENT_AV_NORMALT -> "Jobber ${redusertArbeid.prosentAvNormalt} av normalt i perioden"
                    RedusertArbeidstidType.TIMER_I_SNITT_PER_UKE -> "Jobber ${redusertArbeid.timerPerUke?.toHours()} timer per uke"
                    RedusertArbeidstidType.ULIKE_UKER_TIMER ->
                        redusertArbeid.arbeidsuker?.joinToString(
                            "\n",
                        ) { uke -> "Periode: ${uke.periode}, Timer: ${uke.timer?.toHours()} timer" }
                            ?: "Ingen detaljer for ulike uker"
                }
            "Jobber kombinerer pleiepenger med arbeid i perioden.\n$redusertInfo"
        }
    }
}

fun normalArbeidstid(timePerUkeISnitt: Duration?): String? =
    timePerUkeISnitt?.let {
        "Jobber normalt ${timePerUkeISnitt.tilString()} i uke i snitt."
    }

fun konverterBooleanTilSvar(svar: Boolean) =
    if (svar) {
        "Ja"
    } else {
        "Nei"
    }

fun List<Enkeltdag>.somMapPerMnd(): List<MånedData> =
    groupBy { it.dato.month }.map { (måned, dager) ->
        MånedData(
            år = dager.first().dato.year,
            navnPåMåned = måned.somNorskMåned().storForbokstav(),
            uker = dager.somMapPerUke(),
        )
    }

private fun List<Enkeltdag>.somMapPerUke(): List<UkeData> =
    groupBy { it.dato.ukeNummer() }.map { (uke, dager) ->
        UkeData(
            uke = uke,
            dager = dager.somMapEnkeltdag(),
        )
    }

private fun List<Enkeltdag>.somMapEnkeltdag(): List<DagData> =
    map { dag ->
        DagData(
            dato = DATE_FORMATTER.format(dag.dato),
            tid = dag.tid.somTekst(avkort = false),
        )
    }
