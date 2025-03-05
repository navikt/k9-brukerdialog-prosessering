package no.nav.brukerdialog.pdf.seksjoner.psbs

import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.ArbeidIPeriode
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.ArbeidIPeriodeType
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.ArbeidsRedusert
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsforhold
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsgiver
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.NormalArbeidstid
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.RedusertArbeidstidType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class ArbeidsgivereKtTest {
    @Test
    fun `Arbeidsgiver gir riktig verdi dersom arbeidsløs`() {
        val arbeidsgivere = emptyList<Arbeidsgiver>()
        val fraOgMed = LocalDate.now()

        val resultat = strukturerArbeidsgivereSeksjon(arbeidsgivere, fraOgMed)

        Assertions.assertEquals("Arbeidsforhold", resultat.verdiliste?.get(0)?.label)
        Assertions.assertEquals("Ingen arbeidsforhold registrert i AA-registeret.", resultat.verdiliste?.get(0)?.verdi)
    }

    @Test
    fun `Flere arbeidsgivere gir riktig struktur`() {
        val fraOgMed = LocalDate.now().minusYears(3)
        val arbeidsgivere =
            listOf(
                Arbeidsgiver(
                    navn = "Peppes",
                    organisasjonsnummer = "917755736",
                    erAnsatt = true,
                    arbeidsforhold =
                        Arbeidsforhold(
                            normalarbeidstid =
                                NormalArbeidstid(
                                    timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30),
                                ),
                            arbeidIPeriode =
                                ArbeidIPeriode(
                                    type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                                ),
                        ),
                ),
                Arbeidsgiver(
                    navn = "Pizzabakeren",
                    organisasjonsnummer = "917755736",
                    erAnsatt = true,
                    arbeidsforhold =
                        Arbeidsforhold(
                            normalarbeidstid =
                                NormalArbeidstid(
                                    timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30),
                                ),
                            arbeidIPeriode =
                                ArbeidIPeriode(
                                    type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                                    redusertArbeid =
                                        ArbeidsRedusert(
                                            type = RedusertArbeidstidType.TIMER_I_SNITT_PER_UKE,
                                            timerPerUke = Duration.ofHours(37).plusMinutes(30),
                                        ),
                                ),
                        ),
                ),
                Arbeidsgiver(
                    navn = "Sluttaaaa",
                    organisasjonsnummer = "917755736",
                    erAnsatt = false,
                    arbeidsforhold = null,
                    sluttetFørSøknadsperiode = true,
                ),
            )

        val resultat = strukturerArbeidsgivereSeksjon(arbeidsgivere, fraOgMed)

        Assertions.assertEquals(3, resultat.verdiliste?.size)
    }

    @Test
    fun `Nåværende rbeidsgiver resulterer i ritig spørsmål og svar`() {
        val fraOgMed = LocalDate.now().minusYears(3)
        val arbeidsgivere =
            listOf(
                Arbeidsgiver(
                    navn = "Peppes",
                    organisasjonsnummer = "917755736",
                    erAnsatt = true,
                    arbeidsforhold =
                        Arbeidsforhold(
                            normalarbeidstid =
                                NormalArbeidstid(
                                    timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30),
                                ),
                            arbeidIPeriode =
                                ArbeidIPeriode(
                                    type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                                ),
                        ),
                ),
            )

        val resultat = strukturerArbeidsgivereSeksjon(arbeidsgivere, fraOgMed)
        val bedrift = resultat.verdiliste?.get(0)?.verdiliste

        Assertions.assertEquals("Peppes (orgnr: 917755736)", resultat.verdiliste?.get(0)?.label)
        Assertions.assertEquals("Jobber du her nå?", bedrift?.get(0)?.label)
        Assertions.assertEquals("Ja", bedrift?.get(0)?.verdi)
        Assertions.assertEquals("Hvor mange timer jobber du normalt per uke?", bedrift?.get(1)?.label)
        Assertions.assertEquals("Jobber normalt 37 timer og 30 minutter i uke i snitt.", bedrift?.get(1)?.verdi)
    }

    @Test
    fun `Riktig struktur og verdier ved null-verdier`() {
        val fraOgMed = LocalDate.now().minusYears(3)
        val arbeidsgivere =
            listOf(
                Arbeidsgiver(
                    organisasjonsnummer = "917755736",
                    erAnsatt = true,
                ),
            )

        val resultat = strukturerArbeidsgivereSeksjon(arbeidsgivere, fraOgMed)

        Assertions.assertEquals("Orgnr: 917755736", resultat.verdiliste?.get(0)?.label)
        Assertions.assertEquals(1, resultat.verdiliste?.size)
    }
}
