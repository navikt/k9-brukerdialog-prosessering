package no.nav.k9brukerdialogprosessering.endringsmelding

import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.felles.Søker
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.personopplysninger.Barn
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.psb.v1.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.k9brukerdialogprosessering.endringsmelding.domene.PSBEndringsmeldingMottatt
import no.nav.k9brukerdialogprosessering.pdf.PDFGenerator
import no.nav.k9brukerdialogprosessering.utils.PathUtils.pdfPath
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

class PleiepengersyktBarnEndringsmeldingPdfGeneratorTest {

    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    //@Ignore
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }

    private companion object {
        const val PDF_PREFIX = "psb-endringsmelding"
        private val generator = PDFGenerator()
        private fun fullGyldigEndringsmelding(søknadsId: String): PSBEndringsmeldingMottatt {
            fun k9FormatEndringsmelding(søknadId: UUID = UUID.randomUUID()) = Søknad(
                SøknadId.of(søknadId.toString()),
                Versjon.of("1.0.0"),
                ZonedDateTime.parse("2020-01-01T10:00:00Z"),
                no.nav.k9.søknad.felles.personopplysninger.Søker(NorskIdentitetsnummer.of("12345678910")),
                PleiepengerSyktBarn()
                    .medSøknadsperiode(Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-10")))
                    .medSøknadInfo(DataBruktTilUtledning(true, true, true, true, "commit-abc-123", true))
                    .medBarn(Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of("10987654321")))
                    .medArbeidstid(
                        Arbeidstid()
                            .medArbeidstaker(
                                listOf(
                                    Arbeidstaker()
                                        .medNorskIdentitetsnummer(NorskIdentitetsnummer.of("12345678910"))
                                        .medOrganisasjonsnummer(Organisasjonsnummer.of("926032925"))
                                        .medArbeidstidInfo(
                                            ArbeidstidInfo()
                                                .medPerioder(
                                                    mapOf(
                                                        Periode(
                                                            LocalDate.parse("2023-01-23"),
                                                            LocalDate.parse("2023-01-27")
                                                        ) to ArbeidstidPeriodeInfo()
                                                            .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                                            .medFaktiskArbeidTimerPerDag(Duration.ofHours(2)),
                                                        Periode(
                                                            LocalDate.parse("2022-12-26"),
                                                            LocalDate.parse("2022-12-30")
                                                        ) to ArbeidstidPeriodeInfo()
                                                            .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                                            .medFaktiskArbeidTimerPerDag(Duration.ofHours(4)),
                                                        Periode(
                                                            LocalDate.parse("2023-01-02"),
                                                            LocalDate.parse("2023-01-06")
                                                        ) to ArbeidstidPeriodeInfo()
                                                            .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                                            .medFaktiskArbeidTimerPerDag(Duration.ofHours(2))
                                                    )
                                                )
                                        )
                                )
                            )
                            .medFrilanserArbeidstid(
                                ArbeidstidInfo()
                                    .medPerioder(
                                        mapOf(
                                            Periode(
                                                LocalDate.parse("2022-12-26"),
                                                LocalDate.parse("2022-12-30")
                                            ) to ArbeidstidPeriodeInfo()
                                                .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                                .medFaktiskArbeidTimerPerDag(Duration.ofHours(4)),
                                            Periode(
                                                LocalDate.parse("2023-01-02"),
                                                LocalDate.parse("2023-01-06")
                                            ) to ArbeidstidPeriodeInfo()
                                                .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                                .medFaktiskArbeidTimerPerDag(Duration.ofHours(2)),
                                            Periode(
                                                LocalDate.parse("2023-01-23"),
                                                LocalDate.parse("2023-01-27")
                                            ) to ArbeidstidPeriodeInfo()
                                                .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                                .medFaktiskArbeidTimerPerDag(Duration.ofHours(2))
                                        )
                                    )
                            )
                            .medSelvstendigNæringsdrivendeArbeidstidInfo(
                                ArbeidstidInfo()
                                    .medPerioder(
                                        mapOf(
                                            Periode(
                                                LocalDate.parse("2022-12-26"),
                                                LocalDate.parse("2022-12-30")
                                            ) to ArbeidstidPeriodeInfo()
                                                .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                                .medFaktiskArbeidTimerPerDag(Duration.ofHours(4)),
                                            Periode(
                                                LocalDate.parse("2023-01-02"),
                                                LocalDate.parse("2023-01-06")
                                            ) to ArbeidstidPeriodeInfo()
                                                .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                                .medFaktiskArbeidTimerPerDag(Duration.ofHours(2)),
                                            Periode(
                                                LocalDate.parse("2023-01-23"),
                                                LocalDate.parse("2023-01-27")
                                            ) to ArbeidstidPeriodeInfo()
                                                .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                                .medFaktiskArbeidTimerPerDag(Duration.ofHours(2))
                                        )
                                    )
                            )
                    )
            )
            return PSBEndringsmeldingMottatt(
                søker = Søker(
                    aktørId = "123456",
                    fornavn = "Ærling",
                    mellomnavn = "ØVERBØ",
                    etternavn = "ÅNSNES",
                    fødselsnummer = "29099012345"
                ),
                pleietrengendeNavn = "Barn Barnesen",
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true,
                k9Format = k9FormatEndringsmelding(søknadId = UUID.fromString(søknadsId))
            )
        }
    }

    private fun genererOppsummeringsPdfer(writeBytes: Boolean) {
        var id = "1-full-endringsmelding"
        var pdf = generator.genererPDF(
            pdfData = fullGyldigEndringsmelding(søknadsId = UUID.randomUUID().toString()).pdfData()
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
    }
}
