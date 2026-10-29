package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.integrasjon.k9sakinnsynapi.Barn
import no.nav.brukerdialog.integrasjon.k9sakinnsynapi.K9SakInnsynApiService
import no.nav.brukerdialog.integrasjon.k9sakinnsynapi.K9SakInnsynSøknad
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.ArbeidsgivereOppslagsService
import no.nav.brukerdialog.oppslag.arbeidsgiver.ArbeidsgivereOppslagDto
import no.nav.brukerdialog.oppslag.arbeidsgiver.ArbeidsgivereOppslagResponsDto
import no.nav.brukerdialog.oppslag.arbeidsgiver.OrganisasjonOppslagDto
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang.IngenTilgangÅrsak.HAR_FLERE_ANSETTELSESFORHOLD_HOS_UKJENT_ARBEIDSGIVER
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang.IngenTilgangÅrsak.HAR_INGEN_SAK
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.utils.TilgangTestUtils.periode
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.utils.SøknadUtils.Companion.defaultK9FormatPSB
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Clock
import java.time.LocalDate
import java.time.ZonedDateTime

class EndringsmeldingTilgangServiceTest {

    private val k9SakInnsynApiService = mockk<K9SakInnsynApiService>()
    private val arbeidsgivereOppslagsService = mockk<ArbeidsgivereOppslagsService>()

    // I dag = 2024-03-15 gir endringsperiode 2023-12-01/2025-03-31.
    private val clock = Clock.fixed(
        ZonedDateTime.of(2024, 3, 15, 12, 0, 0, 0, OSLO_ZONE_ID).toInstant(),
        OSLO_ZONE_ID
    )
    private val endringsperiode = periode("2023-12-01/2025-03-31")

    private val service = EndringsmeldingTilgangService(
        k9SakInnsynApiService, arbeidsgivereOppslagsService, clock
    )

    @BeforeEach
    fun setUp() {
        every { arbeidsgivereOppslagsService.hentArbeidsgivere(any(), any(), any(), any(), any()) } returns
            arbeidsgivere()
    }

    @Test
    fun `avslag på sakene henter ikke arbeidsgivere`() {
        every { k9SakInnsynApiService.hentSøknadsopplysninger() } returns emptyList()

        assertEquals(TilgangResultat.ingenTilgang(listOf(HAR_INGEN_SAK), endringsperiode), service.vurderTilgang())
        verify(exactly = 0) { arbeidsgivereOppslagsService.hentArbeidsgivere(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `tilgang henter arbeidsgivere for oppslagsperioden uten private arbeidsgivere og frilansoppdrag`() {
        every { k9SakInnsynApiService.hentSøknadsopplysninger() } returns
            listOf(innsynSøknad("2023-06-01/2024-01-31"))

        assertEquals(TilgangResultat.tilgang(endringsperiode), service.vurderTilgang())
        verify {
            arbeidsgivereOppslagsService.hentArbeidsgivere(
                fraOgMed = LocalDate.parse("2023-12-01"),
                tilOgMed = LocalDate.parse("2024-01-31"),
                inkluderAlleAnsettelsesperioder = true,
                skalHentePrivateArbeidsgivere = false,
                skalHenteFrilansoppdrag = false,
            )
        }
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
            TilgangResultat.ingenTilgang(listOf(HAR_FLERE_ANSETTELSESFORHOLD_HOS_UKJENT_ARBEIDSGIVER), endringsperiode),
            service.vurderTilgang()
        )
    }

    @Test
    fun `feil fra AA-reg gir exception og ikke tilgang`() {
        every { k9SakInnsynApiService.hentSøknadsopplysninger() } returns
            listOf(innsynSøknad("2024-01-01/2024-01-31"))
        every { arbeidsgivereOppslagsService.hentArbeidsgivere(any(), any(), any(), any(), any()) } throws
            IllegalStateException("Feil ved henting av arbeidsgivere")

        assertThrows<IllegalStateException> { service.vurderTilgang() }
    }

    private fun innsynSøknad(søknadsperiode: String) = K9SakInnsynSøknad(
        barn = Barn(
            fødselsdato = LocalDate.parse("2020-01-01"),
            fornavn = "BARN",
            etternavn = "BARNESEN",
            aktørId = "1000000000001",
            identitetsnummer = "02119970079",
        ),
        søknad = defaultK9FormatPSB(søknadsPeriode = listOf(Periode(søknadsperiode)), arbeidstid = Arbeidstid()),
    )

    private fun arbeidsgivere(vararg organisasjoner: OrganisasjonOppslagDto) = ArbeidsgivereOppslagResponsDto(
        ArbeidsgivereOppslagDto(organisasjoner.toList(), null, null)
    )
}
