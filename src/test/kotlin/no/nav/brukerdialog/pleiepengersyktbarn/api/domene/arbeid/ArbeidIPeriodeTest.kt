package no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid

import no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidIPeriodeType
import no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.NormalArbeidstid
import no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.RedusertArbeidstidType
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test
import java.time.Duration

class ArbeidIPeriodeTest {

    companion object {
        private val normalArbeidstid = NormalArbeidstid(timerPerUkeISnitt = Duration.ofHours(40))
    }

    @Test
    fun `Skal gi feil dersom type=PROSENT_AV_NORMALT og prosentAvNormalt er null`() {
        Validator.verifiserValideringsFeil(
            no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                redusertArbeid = no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidsRedusert(
                    type = RedusertArbeidstidType.PROSENT_AV_NORMALT,
                    prosentAvNormalt = null
                )
            ),
            1, "Må være satt dersom type=PROSENT_AV_NORMALT"
        )
    }

    @Test
    fun `Skal gi feil dersom type=TIMER_I_SNITT_PER_UKE og timerPerUke er null`() {
        Validator.verifiserValideringsFeil(
            no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                redusertArbeid = no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidsRedusert(
                    type = RedusertArbeidstidType.TIMER_I_SNITT_PER_UKE,
                    timerPerUke = null
                )
            ),
            1, "Må være satt dersom type=TIMER_I_SNITT_PER_UKE"
        )
    }

    @Test
    fun `Skal gi feil dersom type=ULIKE_UKER_TIMER og arbeidsuker er null eller tom`() {
        Validator.verifiserValideringsFeil(
            no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                redusertArbeid = no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidsRedusert(
                    type = RedusertArbeidstidType.ULIKE_UKER_TIMER,
                    arbeidsuker = null
                )
            ),
            1, "Må være satt dersom type=ULIKE_UKER_TIMER"
        )
    }
}