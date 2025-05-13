package no.nav.brukerdialog.ytelse.opplæringspenger.api.k9Format

import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.*
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.INGEN_ARBEIDSDAG
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.enkeltDagerMedFulltFravær
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.enkeltDagerMedJobbSomVanlig
import no.nav.k9.søknad.JsonUtils
import no.nav.k9.søknad.felles.type.Periode
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class ArbeidsgiverOLPTest {

    @Test
    fun `Gyldig arbeidsgiver gir ingen valideringsfeil`() {
        Validator.verifiserIngenValideringsFeil(
            ArbeidsgiverOLP(
                navn = "Fiskeriet AS",
                organisasjonsnummer = "991346066",
                erAnsatt = true,
                sluttetFørSøknadsperiode = false,
                arbeidsforhold = ArbeidsforholdOLP(
                    37.5,
                    ArbeidIPeriode(JobberIPeriodeSvar.SOM_VANLIG, enkeltDagerMedJobbSomVanlig)
                )
            )
        )
    }

    @Test
    fun `Blankt navn skal gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            ArbeidsgiverOLP(
                navn = " ",
                organisasjonsnummer = "991346066",
                erAnsatt = true,
                sluttetFørSøknadsperiode = false,
                arbeidsforhold = ArbeidsforholdOLP(
                    37.5,
                    ArbeidIPeriode(JobberIPeriodeSvar.HELT_FRAVÆR, enkeltDagerMedFulltFravær)
                )
            ), 1, "navn kan ikke være tomt eller blankt"
        )
    }

    @Test
    fun `Ugyldig arbeidsforhold gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            ArbeidsgiverOLP(
                navn = "Fiskeriet AS",
                organisasjonsnummer = "991346066",
                erAnsatt = true,
                sluttetFørSøknadsperiode = false,
                arbeidsforhold = ArbeidsforholdOLP(37.5, ArbeidIPeriode(JobberIPeriodeSvar.REDUSERT, emptyList()))
            ),
            1,
            "Kan ikke være tom liste"
        )
    }

    @Test
    fun `Liste med arbeidsgivere med ugyldig organisasjonsnummer skal gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            ArbeidsgiverOLP(
                navn = "Fiskeriet AS",
                organisasjonsnummer = "1ABC",
                erAnsatt = true,
                sluttetFørSøknadsperiode = false,
                arbeidsforhold = ArbeidsforholdOLP(
                    37.5, ArbeidIPeriode(
                        JobberIPeriodeSvar.HELT_FRAVÆR,
                        enkeltDagerMedFulltFravær
                    )
                )
            ),
            1,
            "'1ABC' matcher ikke tillatt pattern '^\\d+$'"
        )
    }

    @Test
    fun `Mapping til K9Arbeidstaker blir som forventet`() {
        val k9Arbeidstaker = ArbeidsgiverOLP(
            navn = "Fiskeriet AS",
            organisasjonsnummer = "991346066",
            erAnsatt = true,
            sluttetFørSøknadsperiode = false,
            arbeidsforhold = ArbeidsforholdOLP(
                37.5, ArbeidIPeriode(
                    JobberIPeriodeSvar.HELT_FRAVÆR, listOf(
                        Enkeltdag(LocalDate.parse("2022-01-01"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-02"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-03"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-04"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-06"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-07"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-08"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-09"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-10"), INGEN_ARBEIDSDAG),
                    )
                )
            )
        ).somK9Arbeidstaker(listOf(
            Periode(LocalDate.parse("2022-01-01"), LocalDate.parse("2022-01-04")),
            Periode(LocalDate.parse("2022-01-06"), LocalDate.parse("2022-01-10")))
        )
        val forventet = """
            {
              "norskIdentitetsnummer": null,
              "organisasjonsnummer": "991346066",
              "organisasjonsnavn": "Fiskeriet AS",
              "arbeidstidInfo": {
                "perioder": {
                  "2022-01-01/2022-01-01": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-02/2022-01-02": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-03/2022-01-03": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-04/2022-01-04": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-06/2022-01-06": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-07/2022-01-07": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-08/2022-01-08": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-09/2022-01-09": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  },
                  "2022-01-10/2022-01-10": {
                    "jobberNormaltTimerPerDag": "PT7H30M",
                    "faktiskArbeidTimerPerDag": "PT0S"
                  }
                }
              }
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventet, JsonUtils.toString(k9Arbeidstaker), true)
    }
}
