package no.nav.brukerdialog.ytelse.opplæringspenger.api.k9Format

import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.ArbeidIPeriode
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.Enkeltdag
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.JobberIPeriodeSvar
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.FULL_ARBEIDSDAG
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.HALV_ARBEIDSDAG
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.INGEN_ARBEIDSDAG
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.enkeltDagerMedFulltFravær
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.enkeltDagerMedJobbSomVanlig
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.enkeltDagerMedRedusertArbeid
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.mandag
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.tirsdag
import no.nav.k9.søknad.felles.type.Periode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
class ArbeidIPeriodeTest {
    @Test
    fun `Gyldig ArbeidIPeriode gir ingen valideringsfeil`() {
        Validator.verifiserIngenValideringsFeil(ArbeidIPeriode(JobberIPeriodeSvar.HELT_FRAVÆR, enkeltDagerMedFulltFravær))

        Validator.verifiserIngenValideringsFeil(ArbeidIPeriode(JobberIPeriodeSvar.SOM_VANLIG, enkeltDagerMedJobbSomVanlig))

        Validator.verifiserIngenValideringsFeil(ArbeidIPeriode(JobberIPeriodeSvar.REDUSERT, enkeltDagerMedRedusertArbeid))
    }

    @Test
    fun `Forvent feil dersom både enkeltdager og enkeltdagerFravær er tomme lister`() {
        val exception = org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            ArbeidIPeriode(JobberIPeriodeSvar.HELT_FRAVÆR, emptyList(), emptyList())
        }
        assertEquals("Enten enkeltdager eller enkeltdagerFravær må være satt", exception.message)
    }

    @Test
    fun `Mapping med kun enkeltdagerFravær trekker fra normaltimerPerDag`() {
        val k9Arbeidstid = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.REDUSERT,
            enkeltdager = emptyList(),
            enkeltdagerFravær = listOf(
                Enkeltdag(mandag, HALV_ARBEIDSDAG), // 3 timer fravær
                Enkeltdag(tirsdag, INGEN_ARBEIDSDAG), // 0 timer fravær
            )
        ).somK9ArbeidstidInfo(FULL_ARBEIDSDAG)

        assertEquals(2, k9Arbeidstid.perioder.size)

        // Mandag: 7.5 timer normalt - 3 timer fravær = 4.5 timer arbeid
        assertEquals(Duration.ofHours(4).plusMinutes(30), k9Arbeidstid.perioder[Periode(mandag, mandag)]!!.faktiskArbeidTimerPerDag)

        // Tirsdag: 7.5 timer normalt - 0 timer fravær = 7.5 timer arbeid
        assertEquals(FULL_ARBEIDSDAG, k9Arbeidstid.perioder[Periode(tirsdag, tirsdag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Mapping med kun enkeltdagerFravær større enn normaltimerPerDag kuttes til 0 timer arbeid`() {
        val merEnnFulltFravær = FULL_ARBEIDSDAG.plusHours(2) // 9.5 timer fravær
        val k9Arbeidstid = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.REDUSERT,
            enkeltdager = emptyList(),
            enkeltdagerFravær = listOf(
                Enkeltdag(mandag, merEnnFulltFravær), // 9.5 timer fravær (mer enn 7.5 timer normalarbeidstid)
            )
        ).somK9ArbeidstidInfo(FULL_ARBEIDSDAG)

        assertEquals(1, k9Arbeidstid.perioder.size)

        // Mandag: 7.5 timer normalt - 9.5 timer fravær = -2 timer -> kuttes til 0 timer arbeid
        assertEquals(INGEN_ARBEIDSDAG, k9Arbeidstid.perioder[Periode(mandag, mandag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Mapping med enkeltdagerFravær lik normaltimerPerDag gir 0 timer arbeid`() {
        val k9Arbeidstid = ArbeidIPeriode(
            jobberIPerioden = JobberIPeriodeSvar.HELT_FRAVÆR,
            enkeltdager = emptyList(),
            enkeltdagerFravær = listOf(
                Enkeltdag(mandag, FULL_ARBEIDSDAG), // 7.5 timer fravær
                Enkeltdag(tirsdag, FULL_ARBEIDSDAG), // 7.5 timer fravær
            )
        ).somK9ArbeidstidInfo(FULL_ARBEIDSDAG)

        assertEquals(2, k9Arbeidstid.perioder.size)
        k9Arbeidstid.perioder.forEach { _, arbeidstidPeriodeInfo ->
            assertEquals(FULL_ARBEIDSDAG, arbeidstidPeriodeInfo.jobberNormaltTimerPerDag)
            assertEquals(INGEN_ARBEIDSDAG, arbeidstidPeriodeInfo.faktiskArbeidTimerPerDag)
        }
    }

    @Test
    fun `Mapping til K9Arbeidstid når man ikke jobber i perioden skal gi null faktiskTimer`() {
        val k9Arbeidstid =
            ArbeidIPeriode(JobberIPeriodeSvar.HELT_FRAVÆR, enkeltDagerMedFulltFravær).somK9ArbeidstidInfo(
                FULL_ARBEIDSDAG
            )

        assertEquals(k9Arbeidstid.perioder.size, enkeltDagerMedFulltFravær.size)
        k9Arbeidstid.perioder.forEach { _, arbeidstidPeriodeInfo ->
            assertEquals(FULL_ARBEIDSDAG, arbeidstidPeriodeInfo.jobberNormaltTimerPerDag)
            assertEquals(INGEN_ARBEIDSDAG, arbeidstidPeriodeInfo.faktiskArbeidTimerPerDag)

        }
    }

    @Test
    fun `Mapping av redusert arbeidstid blir mappet til k9format som forventet`() {
        val k9Arbeidstid = ArbeidIPeriode(
            JobberIPeriodeSvar.REDUSERT,
            listOf(
                Enkeltdag(mandag, HALV_ARBEIDSDAG),
                Enkeltdag(tirsdag, HALV_ARBEIDSDAG),
            )
        ).somK9ArbeidstidInfo(FULL_ARBEIDSDAG)
        assertEquals(k9Arbeidstid.perioder.size, 2)
        listOf(mandag, tirsdag).forEach {
            assertEquals(FULL_ARBEIDSDAG, k9Arbeidstid.perioder[Periode(it, it)]!!.jobberNormaltTimerPerDag)
            assertEquals(HALV_ARBEIDSDAG, k9Arbeidstid.perioder[Periode(it, it)]!!.faktiskArbeidTimerPerDag)
        }
    }

    @Test
    fun `Mapping til K9Arbeidtid når man jobber som vanlig`() {
        val k9ArbeidstidInfo =
            ArbeidIPeriode(JobberIPeriodeSvar.SOM_VANLIG, enkeltDagerMedJobbSomVanlig).somK9ArbeidstidInfo(
                FULL_ARBEIDSDAG
            )
        assertEquals(k9ArbeidstidInfo.perioder.size, enkeltDagerMedJobbSomVanlig.size)

        k9ArbeidstidInfo.perioder.forEach { _, u ->
            assertEquals(FULL_ARBEIDSDAG, u.jobberNormaltTimerPerDag)
            assertEquals(FULL_ARBEIDSDAG, u.faktiskArbeidTimerPerDag)
        }
    }
}

