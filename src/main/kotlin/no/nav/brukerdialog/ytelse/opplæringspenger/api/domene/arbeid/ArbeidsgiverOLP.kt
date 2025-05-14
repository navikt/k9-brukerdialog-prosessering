package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.ArbeidsforholdOLP.Companion.somK9ArbeidstidInfo
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker

data class ArbeidsgiverOLP(
    @field:Size(max = 20)
    @field:Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private val organisasjonsnummer: String,

    @field:NotBlank(message = "navn kan ikke være tomt eller blankt")
    private val navn: String,
    private val erAnsatt: Boolean,
    private val sluttetFørSøknadsperiode: Boolean? = null,

    @field:Valid
    private val arbeidsforhold: ArbeidsforholdOLP? = null,
) {

    companion object {

        internal fun List<ArbeidsgiverOLP>.somK9Arbeidstaker(perioder: List<Periode>) =
            map { it.somK9Arbeidstaker(perioder) }
    }

    fun somK9Arbeidstaker(perioder: List<Periode>) = Arbeidstaker().apply {
        medOrganisasjonsnummer(Organisasjonsnummer.of(this@ArbeidsgiverOLP.organisasjonsnummer))
        medOrganisasjonsnavn(navn)
        medArbeidstidInfo(arbeidsforhold.somK9ArbeidstidInfo(perioder))
    }

}
