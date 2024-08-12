package no.nav.brukerdialog.api.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.brukerdialog.api.ytelse.fellesdomene.AktivitetFravær
import no.nav.brukerdialog.api.ytelse.fellesdomene.FraværÅrsak
import no.nav.brukerdialog.api.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.brukerdialog.api.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Arbeidsgiver.Companion.somK9Fraværsperiode
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class ArbeidsgiverTest {

    @Test
    fun `Gyldig arbeidsgiver uten feil`() {
        Validator.verifiserIngenValideringsFeil(
            Arbeidsgiver(
                navn = "Kiwi AS",
                organisasjonsnummer = "825905162",
                utbetalingsårsak = Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER,
                konfliktForklaring = "Fordi blablabla",
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = true,
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = LocalDate.now().minusDays(4),
                        tilOgMed = LocalDate.now(),
                        antallTimerBorte = Duration.ofHours(5),
                        antallTimerPlanlagt = Duration.ofHours(7),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                        aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                    )
                )
            )
        )
    }

    @Test
    fun `Arbeidsgiver uten perioder skal gi feil`() {
        Validator.verifiserValideringsFeil(
            Arbeidsgiver(
                navn = "Kiwi AS",
                organisasjonsnummer = "825905162",
                utbetalingsårsak = Utbetalingsårsak.ARBEIDSGIVER_KONKURS,
                perioder = listOf(),
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = true
            ), 1, "Kan ikke være tom"
        )
    }

    @Test
    fun `Arbeidsgiver med blankt navn skal gi feil`() {
        Validator.verifiserValideringsFeil(
            Arbeidsgiver(
                navn = " ",
                organisasjonsnummer = "825905162",
                utbetalingsårsak = Utbetalingsårsak.ARBEIDSGIVER_KONKURS,
                perioder = listOf(
                    Utbetalingsperiode(
                        LocalDate.now(),
                        LocalDate.now().plusDays(4),
                        Duration.ofHours(5),
                        Duration.ofHours(7),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                        aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                    )
                ),
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = true
            ), 1, "Kan ikke være blankt eller tomt"
        )
    }

    @Test
    fun `Arbeidsgiver med blankt organisasjonsnummer skal gi feil`() {
        Validator.verifiserValideringsFeil(
            Arbeidsgiver(
                navn = "Kiwi AS",
                organisasjonsnummer = " ",
                utbetalingsårsak = Utbetalingsårsak.ARBEIDSGIVER_KONKURS,
                perioder = listOf(
                    Utbetalingsperiode(
                        LocalDate.now(),
                        LocalDate.now().plusDays(4),
                        Duration.ofHours(5),
                        Duration.ofHours(7),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                        aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                    )
                ),
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = true
            ), 2, "' ' matcher ikke tillatt pattern '^\\d+$'", "Kan ikke være blankt eller tomt"
        )
    }

    @Test
    fun `Arbeidsgiver med årsak KONFLIKT_MED_ARBEIDSGIVER uten forklaring skal gi feil`() {
        Validator.verifiserValideringsFeil(
            Arbeidsgiver(
                navn = "Kiwi AS",
                organisasjonsnummer = "825905162",
                utbetalingsårsak = Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER,
                konfliktForklaring = null,
                perioder = listOf(
                    Utbetalingsperiode(
                        LocalDate.now(),
                        LocalDate.now().plusDays(4),
                        Duration.ofHours(5),
                        Duration.ofHours(7),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                        aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                    )
                ),
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = true
            ), 1, "konfliktForklaring må være satt dersom Utbetalingsårsak=KONFLIKT_MED_ARBEIDSGIVER"
        )
    }

    @Test
    fun `Arbeidsgiver med årsak NYOPPSTARTET_HOS_ARBEIDSGIVER uten årsakNyoppstartet skal gi feil`() {
        Validator.verifiserValideringsFeil(
            Arbeidsgiver(
                navn = "Kiwi AS",
                organisasjonsnummer = "825905162",
                utbetalingsårsak = Utbetalingsårsak.NYOPPSTARTET_HOS_ARBEIDSGIVER,
                årsakNyoppstartet = null,
                perioder = listOf(
                    Utbetalingsperiode(
                        LocalDate.now(),
                        LocalDate.now().plusDays(4),
                        Duration.ofHours(5),
                        Duration.ofHours(7),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                        aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                    )
                ),
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = true
            ), 1, "årsakNyoppstartet må være satt dersom Utbetalingsårsak=NYOPPSTARTET_HOS_ARBEIDSGIVER"
        )
    }

    @Test
    fun `Genererer FraværPerioder som forventet`() {
        val arbeidsgivere = listOf(
            Arbeidsgiver(
                navn = "Kiwi AS",
                organisasjonsnummer = "825905162",
                utbetalingsårsak = Utbetalingsårsak.ARBEIDSGIVER_KONKURS,
                årsakNyoppstartet = ÅrsakNyoppstartet.ANNET,
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = LocalDate.parse("2022-01-01"),
                        tilOgMed = LocalDate.parse("2022-01-05"),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                        aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                    ),
                    Utbetalingsperiode(
                        fraOgMed = LocalDate.parse("2022-01-10"),
                        tilOgMed = LocalDate.parse("2022-01-15"),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                        aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                    )
                ),
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = true
            ),
            Arbeidsgiver(
                navn = "Rema AS",
                organisasjonsnummer = "883409442",
                utbetalingsårsak = Utbetalingsårsak.ARBEIDSGIVER_KONKURS,
                årsakNyoppstartet = ÅrsakNyoppstartet.ANNET,
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = LocalDate.parse("2022-01-20"),
                        tilOgMed = LocalDate.parse("2022-01-25"),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                        aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                    )
                ),
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = true
            )
        )
        assertTrue(arbeidsgivere.somK9Fraværsperiode().size == 3)
    }
}
