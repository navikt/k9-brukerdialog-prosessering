package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.FULL_ARBEIDSDAG
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.HALV_ARBEIDSDAG
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.INGEN_ARBEIDSDAG
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.enkeltDagerMedFulltFravær
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.enkeltDagerMedJobbSomVanlig
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.enkeltDagerMedRedusertArbeid
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.mandag
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.PILSTestUtils.tirsdag
import no.nav.k9brukerdialogprosessering.utils.TestUtils.VALIDATOR
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserFeil
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserIngenFeil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ArbeidIPeriodeTest {
    @Test
    fun `Gyldig ArbeidIPeriode gir ingen valideringsfeil`() {
        VALIDATOR.validate(
            ArbeidIPeriode(JobberIPeriodeSvar.HELT_FRAVÆR, enkeltDagerMedFulltFravær)
        ).verifiserIngenFeil()

        VALIDATOR.validate(
            ArbeidIPeriode(JobberIPeriodeSvar.SOM_VANLIG, enkeltDagerMedJobbSomVanlig)
        ).verifiserIngenFeil()

        VALIDATOR.validate(
            ArbeidIPeriode(JobberIPeriodeSvar.REDUSERT, enkeltDagerMedRedusertArbeid)
        ).verifiserIngenFeil()
    }

    @Test
    fun `Forvent feil derom det sendes tom liste med enkeltdager`() {
        VALIDATOR.validate(ArbeidIPeriode(JobberIPeriodeSvar.HELT_FRAVÆR, emptyList()))
            .verifiserFeil(1, "Kan ikke være tom liste")
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
