package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import no.nav.brukerdialog.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.Arbeidsforhold
import no.nav.k9.søknad.felles.opptjening.SelvstendigNæringsdrivende
//TODO fix imports for riktig søknad ytelse
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.LocalDate

data class SelvstendigNæringsdrivende(
    val harInntektSomSelvstendig: Boolean,
    @field:Valid val virksomhet: Virksomhet? = null,
    @field:Valid val arbeidsforhold: Arbeidsforhold? = null,
) {

    @AssertTrue(message = "Må være satt når man har harInntektSomSelvstendig.")
    fun isArbeidsforhold(): Boolean {
        if (harInntektSomSelvstendig) {
            return arbeidsforhold != null
        }
        return true
    }

    @AssertTrue(message = "Må være satt når man har harInntektSomSelvstendig.")
    fun isVirksomhet(): Boolean {
        if (harInntektSomSelvstendig) {
            return virksomhet != null
        }
        return true
    }

    fun tilK9SelvstendigNæringsdrivende(): SelvstendigNæringsdrivende {
        requireNotNull(virksomhet)
        return virksomhet.somK9SelvstendigNæringsdrivende()
    }

    fun k9ArbeidstidInfo(søknadsperiodeStartdato: LocalDate, søknadsperiodeSluttdato: LocalDate): ArbeidstidInfo {
        return when {
            (arbeidsforhold == null) -> Arbeidsforhold.k9ArbeidstidInfoMedNullTimer(
                søknadsperiodeStartdato,
                søknadsperiodeSluttdato
            )

            startetOgSluttetISøknadsperioden(
                søknadsperiodeStartdato,
                søknadsperiodeSluttdato
            ) -> k9ArbeidstidInfoMedStartOgSluttIPerioden(søknadsperiodeSluttdato)

            sluttetISøknadsperioden(søknadsperiodeSluttdato) -> k9ArbeidstidInfoMedSluttIPerioden(
                søknadsperiodeStartdato,
                søknadsperiodeSluttdato
            )

            startetISøknadsperioden(søknadsperiodeStartdato) -> k9ArbeidstidInfoMedStartIPerioden(
                søknadsperiodeSluttdato
            )

            else -> arbeidsforhold.tilK9ArbeidstidInfo(søknadsperiodeStartdato, søknadsperiodeSluttdato)
        }
    }

    private fun sluttetISøknadsperioden(søknadsperiodeSluttdato: LocalDate?): Boolean {
        requireNotNull(virksomhet)
        val virksomhetSluttdato = virksomhet.tilOgMed

        return virksomhetSluttdato != null && virksomhetSluttdato.isBefore(søknadsperiodeSluttdato)
    }

    private fun k9ArbeidstidInfoMedSluttIPerioden(
        søknadperiodeStartdato: LocalDate,
        søknadsperiodeSluttdato: LocalDate,
    ): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        return arbeidsforhold.tilK9ArbeidstidInfo(søknadperiodeStartdato, søknadsperiodeSluttdato)
    }

    private fun startetISøknadsperioden(søknadsperiodeStartdato: LocalDate): Boolean {
        requireNotNull(virksomhet)
        val virksomhetStartdato = virksomhet.fraOgMed

        return virksomhetStartdato.isAfter(søknadsperiodeStartdato)
    }

    private fun k9ArbeidstidInfoMedStartIPerioden(søknadsperiodeSluttdato: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        requireNotNull(virksomhet)
        val virksomhetStartdato = virksomhet.fraOgMed

        return arbeidsforhold.tilK9ArbeidstidInfo(virksomhetStartdato, søknadsperiodeSluttdato)
    }

    private fun startetOgSluttetISøknadsperioden(fraOgMed: LocalDate, tilOgMed: LocalDate?) =
        sluttetISøknadsperioden(tilOgMed) && startetISøknadsperioden(fraOgMed)

    private fun k9ArbeidstidInfoMedStartOgSluttIPerioden(søknadsperiodeSluttdato: LocalDate): ArbeidstidInfo {
        requireNotNull(arbeidsforhold)
        requireNotNull(virksomhet)
        val virksomhetStartdato = virksomhet.fraOgMed
        requireNotNull(virksomhetStartdato)

        return arbeidsforhold.tilK9ArbeidstidInfo(virksomhetStartdato, søknadsperiodeSluttdato)
    }

    private fun slåSammenArbeidstidInfo(vararg arbeidstidInfo: ArbeidstidInfo): ArbeidstidInfo {
        return ArbeidstidInfo().apply {
            arbeidstidInfo.forEach { arbeidstidInfo: ArbeidstidInfo ->
                arbeidstidInfo.perioder.forEach { (periode, arbeidstidPeriodeInfo): Map.Entry<no.nav.k9.søknad.felles.type.Periode, ArbeidstidPeriodeInfo> ->
                    this.leggeTilPeriode(
                        periode,
                        arbeidstidPeriodeInfo
                    )
                }
            }
        }
    }
}
