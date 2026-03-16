package no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import java.time.Duration
import java.time.LocalDate

data class Arbeidsgiver(
    val navn: String? = null,
    val organisasjonsnummer: String,
    val erAnsatt: Boolean,
    val arbeidsforhold: Arbeidsforhold? = null
)

data class Arbeidsforhold(
    val jobberNormaltTimer: Double,
    val arbeidIPeriode: ArbeidIPeriode
)

@JsonInclude(Include.NON_EMPTY)
data class ArbeidIPeriode(
    val jobberIPerioden: JobberIPeriodeSvar,
    val enkeltdager: List<Enkeltdag> = emptyList(),
    val enkeltdagerFravær: List<Enkeltdag> = emptyList()
)

enum class JobberIPeriodeSvar(val pdfTekst: String) {
    SOM_VANLIG("Jeg er ikke borte fra jobb på grunn av opplæring"),
    REDUSERT("Jeg er delvis borte fra jobb fordi jeg er på opplæring"),
    HELT_FRAVÆR("Jeg er helt borte fra jobb fordi jeg er på opplæring");

    fun tilBoolean(): Boolean{
        return when(this){
            SOM_VANLIG, REDUSERT -> true
            HELT_FRAVÆR -> false
        }
    }
}

data class Enkeltdag(
    val dato: LocalDate,
    val tid: Duration
)
