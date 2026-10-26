package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang

import no.nav.brukerdialog.integrasjon.k9sakinnsynapi.Barn
import no.nav.brukerdialog.integrasjon.k9sakinnsynapi.K9SakInnsynSøknad
import no.nav.brukerdialog.oppslag.arbeidsgiver.OrganisasjonOppslagDto
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.utils.SøknadUtils.Companion.defaultK9FormatPSB
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Duration
import java.time.LocalDate

class SakForTilgangTest {

    @Test
    fun `mapper søknadsperioder og orgnr fra k9-format`() {
        val sak = innsynSøknad(defaultK9FormatPSB()).tilSakForTilgang()

        assertEquals(
            SakForTilgang(
                søknadsperioder = listOf(Datoperiode(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-01-01"))),
                arbeidsgiverOrgnr = setOf("926032925"),
                harSelvstendigNæringsdrivendePerioder = false,
            ),
            sak
        )
    }

    @Test
    fun `arbeidstaker uten orgnr utelates`() {
        val arbeidstid = Arbeidstid().medArbeidstaker(
            listOf(
                Arbeidstaker()
                    .medNorskIdentitetsnummer(NorskIdentitetsnummer.of("12345678910"))
                    .medArbeidstidInfo(arbeidstidInfo(Duration.ofHours(8)))
            )
        )

        val sak = innsynSøknad(defaultK9FormatPSB(arbeidstid = arbeidstid)).tilSakForTilgang()

        assertEquals(emptySet<String>(), sak.arbeidsgiverOrgnr)
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["PT7H30M", "PT0S"])
    fun `SN med perioder gir SN-perioder uansett timetall`(jobberNormaltTimerPerDag: String) {
        val arbeidstid = Arbeidstid().medSelvstendigNæringsdrivendeArbeidstidInfo(
            arbeidstidInfo(Duration.parse(jobberNormaltTimerPerDag))
        )

        val sak = innsynSøknad(defaultK9FormatPSB(arbeidstid = arbeidstid)).tilSakForTilgang()

        assertTrue(sak.harSelvstendigNæringsdrivendePerioder)
    }

    @Test
    fun `frilans gir ikke SN-perioder`() {
        val arbeidstid = Arbeidstid().medFrilanserArbeidstid(arbeidstidInfo(Duration.ofMinutes(450)))

        val sak = innsynSøknad(defaultK9FormatPSB(arbeidstid = arbeidstid)).tilSakForTilgang()

        assertFalse(sak.harSelvstendigNæringsdrivendePerioder)
    }

    @Test
    fun `grupperer ansettelsesperioder per orgnr`() {
        val gruppert = listOf(
            OrganisasjonOppslagDto("111", null, LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-02")),
            OrganisasjonOppslagDto("222", null, null, null),
            OrganisasjonOppslagDto("111", null, LocalDate.parse("2024-01-04"), null),
        ).grupperPerArbeidsgiver()

        assertEquals(
            listOf(
                ArbeidsgiverForTilgang(
                    "111",
                    listOf(
                        Ansettelsesperiode(LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-02")),
                        Ansettelsesperiode(LocalDate.parse("2024-01-04"), null),
                    )
                ),
                ArbeidsgiverForTilgang("222", listOf(Ansettelsesperiode(null, null))),
            ),
            gruppert
        )
    }

    private fun innsynSøknad(søknad: no.nav.k9.søknad.Søknad) = K9SakInnsynSøknad(
        barn = Barn(
            fødselsdato = LocalDate.parse("2020-01-01"),
            fornavn = "BARN",
            etternavn = "BARNESEN",
            aktørId = "1000000000001",
        ),
        søknad = søknad,
    )

    private fun arbeidstidInfo(jobberNormaltTimerPerDag: Duration) = ArbeidstidInfo().medPerioder(
        mapOf(
            Periode(LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-31")) to
                ArbeidstidPeriodeInfo()
                    .medJobberNormaltTimerPerDag(jobberNormaltTimerPerDag)
                    .medFaktiskArbeidTimerPerDag(Duration.ZERO)
        )
    )
}
