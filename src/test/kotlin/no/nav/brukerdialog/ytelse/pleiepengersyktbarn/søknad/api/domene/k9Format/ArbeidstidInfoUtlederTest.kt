package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.k9Format

import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Frilans
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.FrilansType
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.ArbeidIPeriode
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.ArbeidIPeriodeType
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.ArbeidsRedusert
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.Arbeidsforhold
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.NormalArbeidstid
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.RedusertArbeidstidType
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.omsorgsstønad.OmsorgsstønadMottarDelerAvPerioden
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.omsorgsstønad.OmsorgsstønadType
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class ArbeidstidInfoUtlederTest {

    private companion object {
        val MANDAG = LocalDate.parse("2025-04-07")
        val TIRSDAG = LocalDate.parse("2025-04-08")
        val ONSDAG = LocalDate.parse("2025-04-09")
        val TORSDAG = LocalDate.parse("2025-04-10")
        val FREDAG = LocalDate.parse("2025-04-11")
    }

    @Test
    fun `Utleding av kun en arbeidstidInfo, returnerer selve arbeidstidInfoen`() {
        val søknadsperiode = Periode(MANDAG, FREDAG)

        val frilansArbeidstidInfo = byggFrilansArbeidstidInfo(
            søknadsperiode = søknadsperiode,
            startdato = MANDAG,
            sluttdato = TIRSDAG,
            jobberNormaltTimerPerDag = Duration.ofHours(40),
            prosentAvNormalt = 50.0
        )

        val utledetArbeidstidInfo = ArbeidstidInfoUtleder(
            førsteArbeidstidInfo = frilansArbeidstidInfo,
            andreArbeidstidInfo = null,
            totalPeriode = søknadsperiode
        ).utled()

        assertThat(frilansArbeidstidInfo).isEqualTo(utledetArbeidstidInfo)
    }

    @Test
    fun `Separate perioder med frilans aktivitet og omsorgsstønad`() {
        val søknadsperiode = Periode(MANDAG, FREDAG)

        val frilansArbeidstidInfo = byggFrilansArbeidstidInfo(
            søknadsperiode = søknadsperiode,
            startdato = søknadsperiode.fraOgMed,
            sluttdato = TIRSDAG,
            jobberNormaltTimerPerDag = Duration.ofHours(40),
            prosentAvNormalt = 50.0
        )

        val omsorgsstønadArbeidstidInfo = OmsorgsstønadMottarDelerAvPerioden(
            type = OmsorgsstønadType.MOTTAR_I_DELER_AV_PERIODEN,
            mottarOmsorgsstønad = true,
            startdato = TORSDAG,
            sluttdato = søknadsperiode.tilOgMed,
            antallTimerIUken = Duration.ofHours(40)
        ).k9ArbeidstidInfo(søknadsperiode)

        val sammenslåttArbeidstidInfo = ArbeidstidInfoUtleder(
            førsteArbeidstidInfo = frilansArbeidstidInfo,
            andreArbeidstidInfo = omsorgsstønadArbeidstidInfo,
            totalPeriode = søknadsperiode
        ).utled()

        val arbeidstidInfoPerioder = sammenslåttArbeidstidInfo.perioder
        assertThat(arbeidstidInfoPerioder.size).isEqualTo(3)

        val perioder = arbeidstidInfoPerioder.iterator()
        val førstePeriode = perioder.next()
        assertThat(førstePeriode.key).isEqualTo(Periode(søknadsperiode.fraOgMed, TIRSDAG))
        assertThat(førstePeriode.value.faktiskArbeidTimerPerDag).isEqualTo(Duration.ofHours(4))
        assertThat(førstePeriode.value.jobberNormaltTimerPerDag).isEqualTo(Duration.ofHours(8))

        val andrePeriode = perioder.next()
        assertThat(andrePeriode.key).isEqualTo(Periode(ONSDAG, ONSDAG))
        assertThat(andrePeriode.value.faktiskArbeidTimerPerDag).isEqualTo(Duration.ofHours(0))
        assertThat(andrePeriode.value.jobberNormaltTimerPerDag).isEqualTo(Duration.ofHours(0))

        val tredjePeriode = perioder.next()
        assertThat(tredjePeriode.key).isEqualTo(Periode(TORSDAG, søknadsperiode.tilOgMed))
        assertThat(tredjePeriode.value.faktiskArbeidTimerPerDag).isEqualTo(Duration.ofHours(8))
        assertThat(tredjePeriode.value.jobberNormaltTimerPerDag).isEqualTo(Duration.ofHours(8))
    }

    @Test
    fun `Overlappende perioder med frilans aktivitet og omsorgsstønad`() {
        val søknadsperiode = Periode(MANDAG, FREDAG)

        val frilansArbeidstidInfo = byggFrilansArbeidstidInfo(
            søknadsperiode = søknadsperiode,
            startdato = søknadsperiode.fraOgMed,
            sluttdato = ONSDAG,
            prosentAvNormalt = 50.0,
            jobberNormaltTimerPerDag = Duration.ofHours(40)
        )

        val omsorgsstønadArbeidstidInfo = byggOmsorgsstønadDelerAvPeriodenArbeidstidInfo(
            søknadsperiode = søknadsperiode,
            startdato = TIRSDAG,
            sluttdato = søknadsperiode.tilOgMed,
            antallTimerIUken = Duration.ofHours(40)
        )

        val sammenslåttArbeidstidInfo = ArbeidstidInfoUtleder(
            førsteArbeidstidInfo = frilansArbeidstidInfo,
            andreArbeidstidInfo = omsorgsstønadArbeidstidInfo,
            totalPeriode = søknadsperiode
        ).utled()

        val arbeidstidInfoPerioder = sammenslåttArbeidstidInfo.perioder
        assertThat(arbeidstidInfoPerioder.size).isEqualTo(3)

        val perioder = arbeidstidInfoPerioder.iterator()
        val førstePeriode = perioder.next()
        assertThat(førstePeriode.key).isEqualTo(Periode(søknadsperiode.fraOgMed, søknadsperiode.fraOgMed))
        assertThat(førstePeriode.value.faktiskArbeidTimerPerDag).isEqualTo(Duration.ofHours(4))
        assertThat(førstePeriode.value.jobberNormaltTimerPerDag).isEqualTo(Duration.ofHours(8))

        val andrePeriode = perioder.next()
        assertThat(andrePeriode.key).isEqualTo(Periode(TIRSDAG, ONSDAG))
        assertThat(andrePeriode.value.faktiskArbeidTimerPerDag).isEqualTo(Duration.ofHours(12))
        assertThat(andrePeriode.value.jobberNormaltTimerPerDag).isEqualTo(Duration.ofHours(16))

        val tredjePeriode = perioder.next()
        assertThat(tredjePeriode.key).isEqualTo(Periode(TORSDAG, søknadsperiode.tilOgMed))
        assertThat(tredjePeriode.value.faktiskArbeidTimerPerDag).isEqualTo(Duration.ofHours(8))
        assertThat(tredjePeriode.value.jobberNormaltTimerPerDag).isEqualTo(Duration.ofHours(8))
    }

    @Test
    fun `Overlappende perioder med frilans aktivitet og omsorgsstønad som overskrider 24t per dag skal feile`() {
        val søknadsperiode = Periode(MANDAG, FREDAG)

        val frilansArbeidstidInfo = byggFrilansArbeidstidInfo(
            søknadsperiode = søknadsperiode,
            startdato = søknadsperiode.fraOgMed,
            sluttdato = søknadsperiode.tilOgMed,
            prosentAvNormalt = 50.0,
            jobberNormaltTimerPerDag = Duration.ofHours(100)
        )

        val omsorgsstønadArbeidstidInfo = byggOmsorgsstønadDelerAvPeriodenArbeidstidInfo(
            søknadsperiode = søknadsperiode,
            startdato = søknadsperiode.fraOgMed,
            sluttdato = søknadsperiode.tilOgMed,
            antallTimerIUken = Duration.ofHours(40)
        )

        val sammenslåttArbeidstidInfo = ArbeidstidInfoUtleder(
            førsteArbeidstidInfo = frilansArbeidstidInfo,
            andreArbeidstidInfo = omsorgsstønadArbeidstidInfo,
            totalPeriode = søknadsperiode
        ).utled()

        val arbeidstidInfoPerioder = sammenslåttArbeidstidInfo.perioder
        assertThat(arbeidstidInfoPerioder.size).isEqualTo(1)

        val arbeidstidPeriodeInfo = arbeidstidInfoPerioder.values.first()
        Validator.verifiserValideringsFeil(arbeidstidPeriodeInfo, 1, "[ugyldigVerdi] Må være lavere eller lik 24 timer.")
    }

    private fun byggFrilansArbeidstidInfo(
        søknadsperiode: Periode,
        startdato: LocalDate,
        sluttdato: LocalDate,
        prosentAvNormalt: Double,
        jobberNormaltTimerPerDag: Duration,
    ): ArbeidstidInfo = Frilans(
        harInntektSomFrilanser = true,
        type = FrilansType.FRILANS,
        startdato = startdato,
        sluttdato = sluttdato,
        startetFørSisteTreHeleMåneder = false,
        misterHonorar = false,
        jobberFortsattSomFrilans = false,
        arbeidsforhold = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(jobberNormaltTimerPerDag),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                redusertArbeid = ArbeidsRedusert(
                    type = RedusertArbeidstidType.PROSENT_AV_NORMALT,
                    prosentAvNormalt = prosentAvNormalt
                )
            )
        )
    ).k9ArbeidstidInfo(søknadsperiode)

    private fun byggOmsorgsstønadDelerAvPeriodenArbeidstidInfo(
        søknadsperiode: Periode,
        startdato: LocalDate,
        sluttdato: LocalDate,
        antallTimerIUken: Duration,
    ): ArbeidstidInfo = OmsorgsstønadMottarDelerAvPerioden(
        type = OmsorgsstønadType.MOTTAR_I_DELER_AV_PERIODEN,
        mottarOmsorgsstønad = true,
        startdato = startdato,
        sluttdato = sluttdato,
        antallTimerIUken = antallTimerIUken
    ).k9ArbeidstidInfo(søknadsperiode)
}
