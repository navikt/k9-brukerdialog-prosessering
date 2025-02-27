package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Frilans
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.FrilansType
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.lagVerdiElement3
import no.nav.brukerdialog.pdf.normalArbeidstid
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar

data class FrilansSpørsmålOgSvar(
    val jobberSomFrilanserEllerMottarHonorar: String,
    val startetFørSisteTreHeleMåneder: SpørsmålOgSvar? = null,
    val nårStartet: SpørsmålOgSvar? = null,
    val fremdelesFrilansEllerHonorar: SpørsmålOgSvar? = null,
    val nårSluttet: SpørsmålOgSvar? = null,
    val misterHonorar: SpørsmålOgSvar? = null,
    val arbeidIPerioden: SpørsmålOgSvar? = null,
)

fun strukturerFrilansSeksjon(søknadSvarFrilans: Frilans): VerdilisteElement? {
    val frilans = mapFrilansTilSpørsmålOgSvar(søknadSvarFrilans)

    return VerdilisteElement(
        label = "Frilans",
        verdiliste =
            listOfNotNull(
                lagVerdiElement(
                    "Jobber du som frilanser eller mottar du honorarer?",
                    frilans.jobberSomFrilanserEllerMottarHonorar,
                ),
                VerdilisteElement(
                    label = frilans.jobberSomFrilanserEllerMottarHonorar,
                    verdiliste =
                        listOfNotNull(
                            lagVerdiElement3(frilans.startetFørSisteTreHeleMåneder),
                            lagVerdiElement3(frilans.nårStartet),
                            lagVerdiElement3(frilans.fremdelesFrilansEllerHonorar),
                            lagVerdiElement3(frilans.nårSluttet),
                        ),
                ),
                lagVerdiElement3(frilans.arbeidIPerioden),
            ),
    )
}

fun mapFrilansTilSpørsmålOgSvar(frilans: Frilans): FrilansSpørsmålOgSvar {
    if (!frilans.harInntektSomFrilanser) {
        return FrilansSpørsmålOgSvar(
            jobberSomFrilanserEllerMottarHonorar = "Har ikke vært frilanser eller mottatt honorar i perioden det søkes om.",
        )
    }

    val sisteTreMånederFørSøknadsperiodeStart = DATE_FORMATTER.format(frilans.startdato?.minusMonths(3))

    return when (frilans.type) {
        FrilansType.FRILANS -> {
            FrilansSpørsmålOgSvar(
                jobberSomFrilanserEllerMottarHonorar = "Jobber som frilanser",
                startetFørSisteTreHeleMåneder =
                    frilans.startetFørSisteTreHeleMåneder?.takeIf { it }?.let {
                        tilSpørsmålOgSvar(
                            spørsmål = "Startet du som frilanser før $sisteTreMånederFørSøknadsperiodeStart?",
                            svar = frilans.startetFørSisteTreHeleMåneder,
                        )
                    },
                nårStartet =
                    frilans.startetFørSisteTreHeleMåneder?.let { null }
                        ?: tilSpørsmålOgSvar("Når startet du som frilanser?", frilans.startdato),
                fremdelesFrilansEllerHonorar =
                    frilans.jobberFortsattSomFrilans?.let {
                        tilSpørsmålOgSvar("Jobber du fremdeles som frilanser?", frilans.jobberFortsattSomFrilans)
                    },
                nårSluttet =
                    frilans.jobberFortsattSomFrilans?.takeIf { !it }?.let {
                        tilSpørsmålOgSvar(
                            "Når sluttet du som frilanser?",
                            frilans.sluttdato,
                        )
                    },
                arbeidIPerioden =
                    frilans.arbeidsforhold?.let {
                        tilSpørsmålOgSvar("Arbeid i perioden", normalArbeidstid(frilans.arbeidsforhold.normalarbeidstid.timerPerUkeISnitt))
                    },
            )
        }

        FrilansType.FRILANS_HONORAR -> {
            FrilansSpørsmålOgSvar(
                jobberSomFrilanserEllerMottarHonorar = "Jobber som frilanser og mottar honorar",
                startetFørSisteTreHeleMåneder =
                    frilans.startetFørSisteTreHeleMåneder?.takeIf { it }?.let {
                        tilSpørsmålOgSvar(
                            "Startet du som frilanser/startet å motta honorar før $sisteTreMånederFørSøknadsperiodeStart?",
                            frilans.startetFørSisteTreHeleMåneder,
                        )
                    },
                nårStartet =
                    frilans.startetFørSisteTreHeleMåneder?.let { null }
                        ?: tilSpørsmålOgSvar("Når begynte du å jobbe som frilanser/startet å motta honorar?", frilans.startdato),
                fremdelesFrilansEllerHonorar =
                    frilans.jobberFortsattSomFrilans?.let {
                        tilSpørsmålOgSvar("Jobber du fremdeles som frilanser/mottar honorar?", frilans.jobberFortsattSomFrilans)
                    },
                nårSluttet =
                    frilans.jobberFortsattSomFrilans?.takeIf { !it }?.let {
                        tilSpørsmålOgSvar(
                            "Når sluttet du som frilanser/sluttet å motta honorar?",
                            frilans.sluttdato,
                        )
                    },
                arbeidIPerioden =
                    frilans.arbeidsforhold?.let {
                        tilSpørsmålOgSvar("Arbeid i perioden", normalArbeidstid(frilans.arbeidsforhold.normalarbeidstid.timerPerUkeISnitt))
                    },
            )
        }
        FrilansType.HONORAR -> {
            FrilansSpørsmålOgSvar(
                jobberSomFrilanserEllerMottarHonorar = "Mottar honorar",
                startetFørSisteTreHeleMåneder =
                    frilans.startetFørSisteTreHeleMåneder?.takeIf { it }?.let {
                        tilSpørsmålOgSvar(
                            "Startet du å motta honorar før $sisteTreMånederFørSøknadsperiodeStart?",
                            frilans.startetFørSisteTreHeleMåneder,
                        )
                    },
                nårStartet =
                    frilans.startetFørSisteTreHeleMåneder?.let { null }
                        ?: tilSpørsmålOgSvar("Når begynte du å motta honorar?", frilans.startdato),
                fremdelesFrilansEllerHonorar =
                    frilans.jobberFortsattSomFrilans?.let {
                        tilSpørsmålOgSvar("Mottar du fortsatt honorar?", frilans.jobberFortsattSomFrilans)
                    },
                nårSluttet =
                    frilans.jobberFortsattSomFrilans?.takeIf { !it }?.let {
                        tilSpørsmålOgSvar(
                            "Når sluttet du å motta honorar?",
                            frilans.sluttdato,
                        )
                    },
                misterHonorar =
                    frilans.misterHonorar?.let {
                        tilSpørsmålOgSvar(
                            "Mister du honorar i søknadsperioden?",
                            if (it)"Jeg mister honorar i søknadsperioden" else "Jeg mister ikke honorar i søknadsperioden",
                        )
                    },
                arbeidIPerioden =
                    frilans.arbeidsforhold?.let {
                        tilSpørsmålOgSvar("Arbeid i perioden", normalArbeidstid(frilans.arbeidsforhold.normalarbeidstid.timerPerUkeISnitt))
                    },
            )
        }
        null ->
            FrilansSpørsmålOgSvar(
                jobberSomFrilanserEllerMottarHonorar = "ikkeFrilansEllerHonorar",
            )
    }
}
