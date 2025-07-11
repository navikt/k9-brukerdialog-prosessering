package no.nav.brukerdialog.ytelse.opplæringspenger.api.k9Format

import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
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

class ArbeidIPeriodeTest {
    @Test
    fun `Gyldig ArbeidIPeriode gir ingen valideringsfeil`() {
        Validator.verifiserIngenValideringsFeil(ArbeidIPeriode(JobberIPeriodeSvar.HELT_FRAVÆR, enkeltDagerMedFulltFravær))

        Validator.verifiserIngenValideringsFeil(ArbeidIPeriode(JobberIPeriodeSvar.SOM_VANLIG, enkeltDagerMedJobbSomVanlig))

        Validator.verifiserIngenValideringsFeil(ArbeidIPeriode(JobberIPeriodeSvar.REDUSERT, enkeltDagerMedRedusertArbeid))
    }

    @Test
    fun `Forvent feil derom det sendes tom liste med enkeltdager`() {
        Validator.verifiserValideringsFeil(ArbeidIPeriode(JobberIPeriodeSvar.HELT_FRAVÆR, emptyList()), 1, "Kan ikke være tom liste")
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
