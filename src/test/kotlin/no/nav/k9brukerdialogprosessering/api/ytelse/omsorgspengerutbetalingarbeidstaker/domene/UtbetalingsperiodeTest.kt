package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.k9.søknad.JsonUtils
import no.nav.k9.søknad.felles.fravær.SøknadÅrsak
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.AktivitetFravær
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.AktivitetFravær.SELVSTENDIG_VIRKSOMHET
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.FraværÅrsak.ORDINÆRT_FRAVÆR
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.FraværÅrsak.SMITTEVERNHENSYN
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Utbetalingsperiode.Companion.somK9FraværPeriode
import no.nav.k9brukerdialogprosessering.utils.TestUtils.Validator
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.Duration
import java.time.LocalDate

class UtbetalingsperiodeTest {

    @Test
    fun `Gyldig utbetalingsperiode gir ingen feil`() {
        Validator.verifiserIngenValideringsFeil(
            Utbetalingsperiode(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(4),
                antallTimerBorte = Duration.ofHours(5),
                antallTimerPlanlagt = Duration.ofHours(7),
                årsak = ORDINÆRT_FRAVÆR,
                aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
            )
        )
    }

    @Test
    fun `Utbetalingsperiode med tom liste for aktivitetFravær gir feil`() {
        Validator.verifiserValideringsFeil(
            Utbetalingsperiode(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(4),
                antallTimerBorte = Duration.ofHours(5),
                antallTimerPlanlagt = Duration.ofHours(7),
                årsak = ORDINÆRT_FRAVÆR,
                aktivitetFravær = listOf()
            ), 1, "Kan ikke være tom"
        )
    }

    @Test
    fun `Utbetalingsperiode hvor fraOgMed er etter tilOgMed gir feil`() {
        Validator.verifiserValideringsFeil(
            Utbetalingsperiode(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().minusDays(1),
                antallTimerBorte = Duration.ofHours(5),
                antallTimerPlanlagt = Duration.ofHours(7),
                årsak = ORDINÆRT_FRAVÆR,
                aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
            ), 1, "'tilOgMed' må være lik eller etter 'fraOgMed'"
        )
    }

    @Test
    fun `Utbetalingsperiode hvor antallTimerBorte er mer enn antallTimerPlanlagt gir feil`() {
        Validator.verifiserValideringsFeil(
            Utbetalingsperiode(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(4),
                antallTimerBorte = Duration.ofHours(7),
                antallTimerPlanlagt = Duration.ofHours(5),
                årsak = ORDINÆRT_FRAVÆR,
                aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
            ),
            1,
            "antallTimerBorte kan ikke være større enn antallTimerPlanlagt"
        )
    }

    @Test
    fun `Utbetalingsperiode hvor antallTimerPlanlagt er satt men antallTimerBorte er null gir feil`() {
        Validator.verifiserValideringsFeil(
            Utbetalingsperiode(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(4),
                antallTimerPlanlagt = Duration.ofHours(5),
                antallTimerBorte = null,
                årsak = ORDINÆRT_FRAVÆR,
                aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
            ),
            1,
            "Dersom antallTimerPlanlagt er satt må antallTimerBorte være satt"
        )
    }

    @Test
    fun `Utbetalingsperiode hvor antallTimerBorte er satt men antallTimerPlanlagt er null gir feil`() {
        Validator.verifiserValideringsFeil(
            Utbetalingsperiode(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().plusDays(4),
                antallTimerBorte = Duration.ofHours(5),
                antallTimerPlanlagt = null,
                årsak = ORDINÆRT_FRAVÆR,
                aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
            ),
            1,
            "Dersom antallTimerBorte er satt må antallTimerPlanlagt være satt"
        )
    }

    @Test
    fun `Genererer forventet FraværPeriode for arbeidstaker`() {
        val utbetalingsperiode = Utbetalingsperiode(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-10"),
            antallTimerBorte = Duration.ofHours(5),
            antallTimerPlanlagt = Duration.ofHours(7),
            årsak = ORDINÆRT_FRAVÆR,
            aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
        )
        val faktiskFraværPeriode = utbetalingsperiode.somK9FraværPeriode(
            SøknadÅrsak.ARBEIDSGIVER_KONKURS,
            "825905162"
        )

        val forventetFraværPeriode = """
            {
              "periode": "2022-01-01/2022-01-10",
              "duration": "PT5H30M",
              "årsak": "ORDINÆRT_FRAVÆR",
              "søknadÅrsak": "ARBEIDSGIVER_KONKURS",
              "aktivitetFravær": [
                "ARBEIDSTAKER"
              ],
              "arbeidsforholdId": null,
              "arbeidsgiverOrgNr": "825905162",
                "delvisFravær": {
                  "normalarbeidstid":"PT7H",
                  "fravær":"PT5H"
                }
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventetFraværPeriode, JsonUtils.toString(faktiskFraværPeriode), true)
    }


    @Test
    fun `Genererer forventet FraværPeriode for selvstendig næringsdrivende og frilans fra liste`() {
        val k9Fraværsperioder = listOf(
            Utbetalingsperiode(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-10"),
                antallTimerBorte = Duration.ofHours(5),
                antallTimerPlanlagt = Duration.ofHours(7),
                årsak = ORDINÆRT_FRAVÆR,
                aktivitetFravær = listOf(AktivitetFravær.FRILANSER, SELVSTENDIG_VIRKSOMHET)
            ),
            Utbetalingsperiode(
                fraOgMed = LocalDate.parse("2022-01-11"),
                tilOgMed = LocalDate.parse("2022-01-15"),
                årsak = SMITTEVERNHENSYN,
                aktivitetFravær = listOf(AktivitetFravær.FRILANSER, SELVSTENDIG_VIRKSOMHET)
            )
        ).somK9FraværPeriode()

        val forventetFraværPeriode = """
            [{
              "periode": "2022-01-01/2022-01-10",
              "duration": "PT5H30M",
              "delvisFravær": {
                 "normalarbeidstid":"PT7H",
                 "fravær":"PT5H"
             },
              "årsak": "ORDINÆRT_FRAVÆR",
              "søknadÅrsak": null,
              "aktivitetFravær": [
                "FRILANSER",
                "SELVSTENDIG_VIRKSOMHET"
              ],
              "arbeidsforholdId": null,
              "arbeidsgiverOrgNr": null
            },
            {
              "periode": "2022-01-11/2022-01-15",
              "duration": null,
              "delvisFravær": null,
              "årsak": "SMITTEVERNHENSYN",
              "søknadÅrsak": null,
              "aktivitetFravær": [
                "FRILANSER",
                "SELVSTENDIG_VIRKSOMHET"
              ],
              "arbeidsforholdId": null,
              "arbeidsgiverOrgNr": null
            }
           ]
        """.trimIndent()
        JSONAssert.assertEquals(forventetFraværPeriode, JsonUtils.toString(k9Fraværsperioder), true)
    }

}
