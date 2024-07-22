package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.JsonUtils
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.INGEN_ARBEIDSDAG
import no.nav.k9brukerdialogprosessering.utils.TestUtils.VALIDATOR
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserFeil
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserIngenFeil
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class ArbeidsgiverTest {

    @Test
    fun `Gyldig arbeidsgiver gir ingen valideringsfeil`() {
        VALIDATOR.validate(
            Arbeidsgiver(
                navn = "Fiskeriet AS",
                organisasjonsnummer = "991346066",
                erAnsatt = true,
                sluttetFørSøknadsperiode = false,
                arbeidsforhold = Arbeidsforhold(
                    37.5,
                    ArbeidIPeriode(JobberIPeriodeSvar.SOM_VANLIG, PILSTestUtils.enkeltDagerMedJobbSomVanlig)
                )
            )
        ).verifiserIngenFeil()
    }

    @Test
    fun `Blankt navn skal gi valideringsfeil`() {
        VALIDATOR.validate(
            Arbeidsgiver(
                navn = " ",
                organisasjonsnummer = "991346066",
                erAnsatt = true,
                sluttetFørSøknadsperiode = false,
                arbeidsforhold = Arbeidsforhold(
                    37.5,
                    ArbeidIPeriode(JobberIPeriodeSvar.HELT_FRAVÆR, PILSTestUtils.enkeltDagerMedFulltFravær)
                )
            )
        ).verifiserFeil(1, "Kan ikke være tomt eller blankt")
    }

    @Test
    fun `Ugyldig arbeidsforhold gi valideringsfeil`() {
        VALIDATOR.validate(
            Arbeidsgiver(
                navn = "Fiskeriet AS",
                organisasjonsnummer = "991346066",
                erAnsatt = true,
                sluttetFørSøknadsperiode = false,
                arbeidsforhold = Arbeidsforhold(37.5, ArbeidIPeriode(JobberIPeriodeSvar.REDUSERT, emptyList()))
            )
        ).verifiserFeil(
            1,
            "Kan ikke være tom liste"
        )
    }

    @Test
    fun `Liste med arbeidsgivere med ugyldig organisasjonsnummer skal gi valideringsfeil`() {
        VALIDATOR.validate(
            Arbeidsgiver(
                navn = "Fiskeriet AS",
                organisasjonsnummer = "1ABC",
                erAnsatt = true,
                sluttetFørSøknadsperiode = false,
                arbeidsforhold = Arbeidsforhold(
                    37.5, ArbeidIPeriode(
                        JobberIPeriodeSvar.HELT_FRAVÆR,
                        PILSTestUtils.enkeltDagerMedFulltFravær
                    )
                )
            )
        ).verifiserFeil(
            1,
            "'1ABC' matcher ikke tillatt pattern '^\\d+$'"
        )
    }

    @Test
    fun `Mapping til K9Arbeidstaker blir som forventet`() {
        val k9Arbeidstaker = Arbeidsgiver(
            navn = "Fiskeriet AS",
            organisasjonsnummer = "991346066",
            erAnsatt = true,
            sluttetFørSøknadsperiode = false,
            arbeidsforhold = Arbeidsforhold(
                37.5, ArbeidIPeriode(
                    JobberIPeriodeSvar.HELT_FRAVÆR, listOf(
                        Enkeltdag(LocalDate.parse("2022-01-01"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-02"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-03"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-04"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-05"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-06"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-07"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-08"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-09"), INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2022-01-10"), INGEN_ARBEIDSDAG),
                    )
                )
            )
        ).somK9Arbeidstaker(LocalDate.parse("2022-01-01"), LocalDate.parse("2022-01-10"))
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
                  "2022-01-05/2022-01-05": {
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
