package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.Arbeidsforhold
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.Arbeidsforhold.Companion.k9ArbeidstidInfoMedNullTimer
import no.nav.k9brukerdialogprosessering.utils.krever
import java.time.LocalDate

data class Arbeidsgiver(
    @Size(max = 20)
    @Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    val organisasjonsnummer: String,
    val navn: String? = null,
    val erAnsatt: Boolean,
    val sluttetFørSøknadsperiode: Boolean? = null,
    val arbeidsforhold: Arbeidsforhold? = null,
) {
    fun k9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        return arbeidsforhold?.tilK9ArbeidstidInfo(fraOgMed, tilOgMed)
            ?: k9ArbeidstidInfoMedNullTimer(fraOgMed, tilOgMed)
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        if (arbeidsforhold != null) addAll(arbeidsforhold.valider("$felt.arbeidsforhold"))
        krever(!navn.isNullOrBlank(), "$felt.navn kan ikke være tomt eller kun whitespace")
    }
}

internal fun List<Arbeidsgiver>.valider() = mutableSetOf<String>().apply {
    this@valider.mapIndexed { index, arbeidsgiver ->
        addAll(arbeidsgiver.valider("arbeidsgiver[$index]"))
    }
}
