package no.nav.brukerdialog.ytelse.pleiepengerilivetssluttfase.api.domene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.brukerdialog.ytelse.pleiepengerilivetssluttfase.api.domene.PILSTestUtils.FULL_ARBEIDSDAG
import no.nav.brukerdialog.ytelse.pleiepengerilivetssluttfase.api.domene.PILSTestUtils.INGEN_ARBEIDSDAG
import no.nav.brukerdialog.ytelse.pleiepengerilivetssluttfase.api.domene.PILSTestUtils.fredag
import no.nav.brukerdialog.ytelse.pleiepengerilivetssluttfase.api.domene.PILSTestUtils.mandag
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.ArbeidIPeriode
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.Arbeidsforhold
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.Frilans
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.JobberIPeriodeSvar
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class FrilansTest {

    @Test
    fun `Gyldig frilans gir ingen valideringsfeil`() {
        Validator.verifiserIngenValideringsFeil(
            Frilans(
                startdato = LocalDate.parse("2022-01-01"),
                sluttdato = null,
                jobberFortsattSomFrilans = true,
                harHattInntektSomFrilanser = true
            )
        )
    }

    @Test
    fun `harHattInntektSomFrilans er null skal gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Frilans(
                startdato = LocalDate.parse("2022-01-01"),
                jobberFortsattSomFrilans = true,
                harHattInntektSomFrilanser = null
            ), 1, "Kan ikke være null"
        )
    }


    @Test
    fun `sluttdato før startdato skal gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Frilans(
                startdato = LocalDate.parse("2022-01-02"),
                sluttdato = LocalDate.parse("2022-01-01"),
                jobberFortsattSomFrilans = false,
                harHattInntektSomFrilanser = true
            ),
            1,
            "'Sluttdato' må være lik eller etter 'startdato'"
        )
    }

    @Test
    fun `Dersom jobberFortsattSomFrilans er true og sluttdato er satt skal det gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Frilans(
                startdato = LocalDate.parse("2022-01-01"),
                sluttdato = LocalDate.parse("2022-01-02"),
                jobberFortsattSomFrilans = true,
                harHattInntektSomFrilanser = true
            ), 1, "Dersom 'jobberFortsattSomFrilans' er true, kan ikke 'sluttdato' være satt"
        )
    }

    @Test
    fun `Dersom jobberFortsattSomFrilans er false og sluttdato er null skal det gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Frilans(
                startdato = LocalDate.parse("2022-01-01"),
                sluttdato = null,
                jobberFortsattSomFrilans = false,
                harHattInntektSomFrilanser = true
            ), 1, "Dersom 'jobberFortsattSomFrilans' er false, må 'sluttdato' være satt"
        )
    }

    @Test
    fun `Feil i arbeidsforhold skal gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Frilans(
                startdato = LocalDate.parse("2022-01-01"),
                jobberFortsattSomFrilans = true,
                harHattInntektSomFrilanser = true,
                arbeidsforhold = Arbeidsforhold(20.0, ArbeidIPeriode(JobberIPeriodeSvar.REDUSERT, emptyList())),
            ),
            1,
            "Kan ikke være tom liste"
        )
    }

    @Test
    fun `Mapping til K9Frilanser blir som forventet`() {
        val startdato = LocalDate.parse("2022-01-01")
        val sluttdato = LocalDate.parse("2022-01-10")
        Frilans(startdato, sluttdato, false, null, true)
            .somK9Frilanser().also {
                assertEquals(startdato, it.startdato)
                assertEquals(sluttdato, it.sluttdato)
            }
    }

    @Test
    fun `Mapping til K9Arbeidstid blir som forventet`() {
        val fraOgMed = mandag
        val tilOgMed = fredag
        Frilans(
            startdato = LocalDate.parse("2022-01-02"),
            sluttdato = LocalDate.parse("2022-01-01"),
            jobberFortsattSomFrilans = false,
            harHattInntektSomFrilanser = true,
            arbeidsforhold = Arbeidsforhold(
                37.5,
                ArbeidIPeriode(JobberIPeriodeSvar.HELT_FRAVÆR, PILSTestUtils.enkeltDagerMedFulltFravær)
            )
        ).somK9Arbeidstid(fraOgMed, tilOgMed).also {
            assertEquals(it.perioder.size, 5)
            it.perioder.forEach { _: Periode, arbeidstidPeriodeInfo: ArbeidstidPeriodeInfo ->
                assertEquals(FULL_ARBEIDSDAG, arbeidstidPeriodeInfo.jobberNormaltTimerPerDag)
                assertEquals(INGEN_ARBEIDSDAG, arbeidstidPeriodeInfo.faktiskArbeidTimerPerDag)
            }
        }
    }
}
