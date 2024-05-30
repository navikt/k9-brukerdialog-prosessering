package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerutbetalingat.utils

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.fravær.AktivitetFravær
import no.nav.k9.søknad.felles.fravær.FraværPeriode
import no.nav.k9.søknad.felles.fravær.SøknadÅrsak
import no.nav.k9.søknad.felles.personopplysninger.Barn
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetaling
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingat.domene.*
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.utils.SøknadUtils.barn
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker

internal object OMPUtbetalingATSøknadUtils {
    private val start = LocalDate.parse("2020-01-01")
    private const val GYLDIG_ORGNR = "917755736"

    internal fun defaultSøknad(søknadId: String, mottatt: ZonedDateTime) = OMPUtbetalingATSoknadMottatt(
        søknadId = søknadId,
        språk = "nb",
        mottatt = mottatt,
        søker = Søker(
            aktørId = "123456",
            fødselsnummer = "02119970078",
            fødselsdato = LocalDate.parse("1999-11-02"),
            etternavn = "Nordmann",
            mellomnavn = null,
            fornavn = "Ola"
        ),
        fosterbarn = listOf(
            Fosterbarn(
                navn = "Fosterbarn 1",
                identitetsnummer = "01011012345"
            ),
            Fosterbarn(
                navn = "Fosterbarn 2",
                identitetsnummer = "01011012234"
            )
        ),
        dineBarn = DineBarn(
            harDeltBosted = true,
            barn = listOf(
                Barn(
                    identitetsnummer = "01010101011",
                    aktørId = "01010101011",
                    fødselsdato = LocalDate.parse("2024-05-31"),
                    navn = "Van Li Barnesen",
                    type = TypeBarn.FRA_OPPSLAG
                ),
                Barn(
                    identitetsnummer = "02020202020",
                    aktørId = "02020202020",
                    fødselsdato = LocalDate.parse("2024-05-30"),
                    navn = "Foster Barnesen",
                    type = TypeBarn.FOSTERBARN
                ),
                Barn(
                    identitetsnummer = "02020202020",
                    aktørId = "02020202020",
                    fødselsdato = LocalDate.parse("2024-05-29"),
                    navn = "Anna Barnesen",
                    type = TypeBarn.ANNET
                ),
            )
        ),
        arbeidsgivere = listOf(
            ArbeidsgiverDetaljer(
                navn = "Arbeidsgiver 1",
                organisasjonsnummer = GYLDIG_ORGNR,
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = start,
                        tilOgMed = start.plusDays(10),
                        antallTimerPlanlagt = Duration.ofHours(8),
                        antallTimerBorte = Duration.ofHours(8),
                        årsak = FraværÅrsak.SMITTEVERNHENSYN
                    ),
                    Utbetalingsperiode(
                        fraOgMed = start.plusDays(10),
                        tilOgMed = start.plusDays(20),
                        antallTimerPlanlagt = Duration.ofHours(8),
                        antallTimerBorte = Duration.ofHours(8),
                        årsak = null
                    )
                ),
                utbetalingsårsak = Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER,
                konfliktForklaring = "Har en konflikt med arbeidsgiver fordi ...."
            ),
            ArbeidsgiverDetaljer(
                navn = "Arbeidsgiver 2",
                organisasjonsnummer = GYLDIG_ORGNR,
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                utbetalingsårsak = Utbetalingsårsak.NYOPPSTARTET_HOS_ARBEIDSGIVER,
                årsakNyoppstartet = ÅrsakNyoppstartet.UTØVDE_VERNEPLIKT,
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = start.plusDays(20),
                        tilOgMed = start.plusDays(20),
                        antallTimerPlanlagt = Duration.ofHours(8),
                        antallTimerBorte = Duration.ofHours(8),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR
                    )
                )
            ),
            ArbeidsgiverDetaljer(
                navn = "Arbeidsgiver 3",
                organisasjonsnummer = GYLDIG_ORGNR,
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                utbetalingsårsak = Utbetalingsårsak.NYOPPSTARTET_HOS_ARBEIDSGIVER,
                årsakNyoppstartet = ÅrsakNyoppstartet.SØKTE_ANDRE_UTBETALINGER,
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = start.plusMonths(1),
                        tilOgMed = start.plusMonths(1).plusDays(5),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR
                    )
                )
            )
        ),
        bosteder = listOf(
            Bosted(
                fraOgMed = start.minusDays(20),
                tilOgMed = start.minusDays(10),
                landkode = "GB",
                landnavn = "Great Britain",
                erEØSLand = JaNei.Ja
            ),
            Bosted(
                fraOgMed = start.minusDays(20),
                tilOgMed = start.minusDays(10),
                landkode = "US",
                landnavn = "USA",
                erEØSLand = JaNei.Nei
            )
        ),
        opphold = listOf(
            Opphold(
                fraOgMed = start.minusDays(20),
                tilOgMed = start.minusDays(10),
                landkode = "GB",
                landnavn = "Great Britain",
                erEØSLand = JaNei.Ja
            )
        ),
        bekreftelser = Bekreftelser(
            harForståttRettigheterOgPlikter = JaNei.Ja,
            harBekreftetOpplysninger = JaNei.Ja
        ),
        titler = listOf(
            "vedlegg1"
        ),
        vedleggId = listOf("1234", "5678"),
        hjemmePgaSmittevernhensyn = true,
        hjemmePgaStengtBhgSkole = true,
        k9Format = Søknad(
            SøknadId(søknadId),
            Versjon("1.0.0"),
            mottatt,
            K9Søker(NorskIdentitetsnummer.of("02119970078")),
            OmsorgspengerUtbetaling(
                listOf(
                    Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of("26128027024"))
                ),
                null,
                listOf(
                    FraværPeriode(
                        Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-10")),
                        Duration.ofHours(7).plusMinutes(30),
                        null,
                        no.nav.k9.søknad.felles.fravær.FraværÅrsak.STENGT_SKOLE_ELLER_BARNEHAGE,
                        SøknadÅrsak.KONFLIKT_MED_ARBEIDSGIVER,
                        listOf(AktivitetFravær.ARBEIDSTAKER),
                        Organisasjonsnummer.of(GYLDIG_ORGNR),
                        null
                    )
                ),
                null,
                Bosteder().medPerioder(
                    mapOf(
                        Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-10")) to
                                Bosteder.BostedPeriodeInfo().medLand((Landkode.NORGE))
                    )
                ),
                Utenlandsopphold().medPerioder(
                    mapOf(
                        Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-10")) to
                                Utenlandsopphold.UtenlandsoppholdPeriodeInfo()
                                    .medLand(Landkode.SPANIA)
                                    .medÅrsak(Utenlandsopphold.UtenlandsoppholdÅrsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD)
                    )
                )
            )
        )
    )
}
