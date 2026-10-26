package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.integrasjon.k9sakinnsynapi.Barn
import no.nav.brukerdialog.integrasjon.k9sakinnsynapi.K9SakInnsynApiService
import no.nav.brukerdialog.integrasjon.k9sakinnsynapi.K9SakInnsynSøknad
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.ArbeidsgivereOppslagsService
import no.nav.brukerdialog.metrikk.MetrikkService
import no.nav.brukerdialog.oppslag.arbeidsgiver.ArbeidsgivereOppslagDto
import no.nav.brukerdialog.oppslag.arbeidsgiver.ArbeidsgivereOppslagResponsDto
import no.nav.brukerdialog.oppslag.arbeidsgiver.OrganisasjonOppslagDto
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang.IngenTilgangÅrsak.HAR_ARBEIDSTID_SOM_SELVSTENDIG_NÆRINGSDRIVENDE
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang.IngenTilgangÅrsak.HAR_FLERE_ANSETTELSESFORHOLD_HOS_UKJENT_ARBEIDSGIVER
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang.IngenTilgangÅrsak.HAR_INGEN_SAK
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.utils.SøknadUtils.Companion.defaultK9FormatPSB
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

class EndringsmeldingTilgangServiceTest {

    private val k9SakInnsynApiService = mockk<K9SakInnsynApiService>()
    private val arbeidsgivereOppslagsService = mockk<ArbeidsgivereOppslagsService>()
    private val metrikkService = mockk<MetrikkService>(relaxed = true)

    // I dag = 2024-03-15 gir endringsperiode 2023-12-01/2025-03-31.
    private val clock = Clock.fixed(
        ZonedDateTime.of(2024, 3, 15, 12, 0, 0, 0, OSLO_ZONE_ID).toInstant(),
        OSLO_ZONE_ID
    )

    private val service = EndringsmeldingTilgangService(
        k9SakInnsynApiService, arbeidsgivereOppslagsService, metrikkService, clock
    )

    @BeforeEach
    fun setUp() {
        every { arbeidsgivereOppslagsService.hentArbeidsgivere(any(), any(), any(), any(), any()) } returns
            arbeidsgivere()
    }

    @Test
    fun `avslag på sakene henter ikke arbeidsgivere`() {
        every { k9SakInnsynApiService.hentSøknadsopplysninger() } returns emptyList()

        assertEquals(TilgangResultat.ingenTilgang(listOf(HAR_INGEN_SAK)), service.vurderTilgang())
        verify(exactly = 0) { arbeidsgivereOppslagsService.hentArbeidsgivere(any(), any(), any(), any(), any()) }
        verify { metrikkService.registrerEndringsmeldingTilgangVurdering(HAR_INGEN_SAK) }
    }

    @Test
    fun `tilgang henter arbeidsgivere for oppslagsperioden uten private arbeidsgivere og frilansoppdrag`() {
        every { k9SakInnsynApiService.hentSøknadsopplysninger() } returns
            listOf(innsynSøknad("2023-06-01/2024-01-31"))

        assertEquals(TilgangResultat.tilgang(), service.vurderTilgang())
        verify {
            arbeidsgivereOppslagsService.hentArbeidsgivere(
                fraOgMed = LocalDate.parse("2023-12-01"),
                tilOgMed = LocalDate.parse("2024-01-31"),
                inkluderAlleAnsettelsesperioder = true,
                skalHentePrivateArbeidsgivere = false,
                skalHenteFrilansoppdrag = false,
            )
        }
        verify { metrikkService.registrerEndringsmeldingTilgangVurdering(null) }
    }

    @Test
    fun `avslag på arbeidsforhold bruker arbeidsgiverne fra AA-reg gruppert per orgnr`() {
        every { k9SakInnsynApiService.hentSøknadsopplysninger() } returns
            listOf(innsynSøknad("2024-01-01/2024-01-31"))
        every { arbeidsgivereOppslagsService.hentArbeidsgivere(any(), any(), any(), any(), any()) } returns
            arbeidsgivere(
                OrganisasjonOppslagDto("999", null, LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-10")),
                OrganisasjonOppslagDto("999", null, LocalDate.parse("2024-01-15"), LocalDate.parse("2024-01-20")),
            )

        assertEquals(
            TilgangResultat.ingenTilgang(listOf(HAR_FLERE_ANSETTELSESFORHOLD_HOS_UKJENT_ARBEIDSGIVER)),
            service.vurderTilgang()
        )
    }

    @Test
    fun `arbeidstaker uten normalarbeidstid regnes som ukjent arbeidsgiver, som i frontend`() {
        val arbeidstid = Arbeidstid().medArbeidstaker(
            listOf(
                Arbeidstaker()
                    .medOrganisasjonsnummer(Organisasjonsnummer.of("999"))
                    .medArbeidstidInfo(arbeidstidInfo(Duration.ZERO))
            )
        )
        every { k9SakInnsynApiService.hentSøknadsopplysninger() } returns
            listOf(innsynSøknad("2024-01-01/2024-01-31", arbeidstid))
        every { arbeidsgivereOppslagsService.hentArbeidsgivere(any(), any(), any(), any(), any()) } returns
            arbeidsgivere(
                OrganisasjonOppslagDto("999", null, LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-10")),
                OrganisasjonOppslagDto("999", null, LocalDate.parse("2024-01-15"), LocalDate.parse("2024-01-20")),
            )

        assertEquals(
            TilgangResultat.ingenTilgang(listOf(HAR_FLERE_ANSETTELSESFORHOLD_HOS_UKJENT_ARBEIDSGIVER)),
            service.vurderTilgang()
        )
    }

    @Test
    fun `barn uten identitetsnummer i aktuell sak gir exception før arbeidsgivere hentes`() {
        every { k9SakInnsynApiService.hentSøknadsopplysninger() } returns
            listOf(innsynSøknad("2024-01-01/2024-01-31", identitetsnummer = null))

        assertThrows<IllegalStateException> { service.vurderTilgang() }
        verify(exactly = 0) { arbeidsgivereOppslagsService.hentArbeidsgivere(any(), any(), any(), any(), any()) }
        verify(exactly = 0) { metrikkService.registrerEndringsmeldingTilgangVurdering(any()) }
    }

    @Test
    fun `selvstendig næringsdrivende får ikke tilgang`() {
        val arbeidstid = Arbeidstid().medSelvstendigNæringsdrivendeArbeidstidInfo(
            arbeidstidInfo(Duration.ofMinutes(450))
        )
        every { k9SakInnsynApiService.hentSøknadsopplysninger() } returns
            listOf(innsynSøknad("2024-01-01/2024-01-31", arbeidstid))

        assertEquals(
            TilgangResultat.ingenTilgang(listOf(HAR_ARBEIDSTID_SOM_SELVSTENDIG_NÆRINGSDRIVENDE)),
            service.vurderTilgang()
        )
        verify { metrikkService.registrerEndringsmeldingTilgangVurdering(HAR_ARBEIDSTID_SOM_SELVSTENDIG_NÆRINGSDRIVENDE) }
    }

    @Test
    fun `feil fra AA-reg gir exception og ikke tilgang`() {
        every { k9SakInnsynApiService.hentSøknadsopplysninger() } returns
            listOf(innsynSøknad("2024-01-01/2024-01-31"))
        every { arbeidsgivereOppslagsService.hentArbeidsgivere(any(), any(), any(), any(), any()) } throws
            IllegalStateException("Feil ved henting av arbeidsgivere")

        assertThrows<IllegalStateException> { service.vurderTilgang() }
        verify(exactly = 0) { metrikkService.registrerEndringsmeldingTilgangVurdering(any()) }
    }

    private fun innsynSøknad(
        søknadsperiode: String,
        arbeidstid: Arbeidstid = Arbeidstid(),
        identitetsnummer: String? = "02119970079",
    ) = K9SakInnsynSøknad(
        barn = Barn(
            fødselsdato = LocalDate.parse("2020-01-01"),
            fornavn = "BARN",
            etternavn = "BARNESEN",
            aktørId = "1000000000001",
            identitetsnummer = identitetsnummer,
        ),
        søknad = defaultK9FormatPSB(søknadsPeriode = listOf(Periode(søknadsperiode)), arbeidstid = arbeidstid),
    )

    private fun arbeidstidInfo(jobberNormaltTimerPerDag: Duration) = ArbeidstidInfo().medPerioder(
        mapOf(
            Periode("2024-01-01/2024-01-31") to ArbeidstidPeriodeInfo()
                .medJobberNormaltTimerPerDag(jobberNormaltTimerPerDag)
                .medFaktiskArbeidTimerPerDag(Duration.ZERO)
        )
    )

    private fun arbeidsgivere(vararg organisasjoner: OrganisasjonOppslagDto) = ArbeidsgivereOppslagResponsDto(
        ArbeidsgivereOppslagDto(organisasjoner.toList(), null, null)
    )
}
