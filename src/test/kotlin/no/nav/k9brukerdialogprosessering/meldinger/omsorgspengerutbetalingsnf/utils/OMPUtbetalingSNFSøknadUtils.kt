package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerutbetalingsnf.utils

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.fravær.FraværPeriode
import no.nav.k9.søknad.felles.opptjening.Frilanser
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.personopplysninger.Barn
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.felles.type.VirksomhetType
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetaling
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.AktivitetFravær
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.Bekreftelser
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.Bosted
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.FraværÅrsak
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.Frilans
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.JaNei
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.Næringstyper
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.OMPUtbetalingSNFSoknadMottatt
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.SelvstendigNæringsdrivende
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.SpørsmålOgSvar
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.TypeBarn
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.Utbetalingsperiode
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.VarigEndring
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

internal object OMPUtbetalingSNFSøknadUtils {
    private val start = LocalDate.parse("2020-01-01")
    private const val GYLDIG_ORGNR = "917755736"

    internal fun defaultSøknad(søknadId: String, mottatt: ZonedDateTime) = OMPUtbetalingSNFSoknadMottatt(
        søknadId = søknadId,
        språk = "nb",
        mottatt = mottatt,
        søker = Søker(
            aktørId = "123456",
            fødselsnummer = "02119970078",
            fødselsdato = LocalDate.parse("2020-08-02"),
            etternavn = "Nordmann",
            mellomnavn = "Mellomnavn",
            fornavn = "Ola"
        ),
        barn = listOf(
            no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.Barn(
                navn = "Barn 1 Barnesen",
                fødselsdato = LocalDate.parse("2020-01-01"),
                identitetsnummer = "1234",
                type = TypeBarn.FOSTERBARN,
                aktørId = null
            ),
            no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.Barn(
                navn = "Barn 2 Barnesen",
                fødselsdato = LocalDate.parse("2019-01-01"),
                identitetsnummer = "5677",
                type = TypeBarn.FRA_OPPSLAG,
                aktørId = "123456789"
            ),
            no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.Barn(
                navn = "Barn 3 Barnesen",
                fødselsdato = LocalDate.parse("2018-01-01"),
                identitetsnummer = "8888",
                type = TypeBarn.ANNET,
                aktørId = null
            )
        ),
        harDekketTiFørsteDagerSelv = true,
        harSyktBarn = true,
        harAleneomsorg = true,
        bosteder = listOf(
            Bosted(
                fraOgMed = start,
                tilOgMed = start.plusDays(5),
                landnavn = "Sverige",
                landkode = "SWE",
                erEØSLand = JaNei.Ja
            ),
            Bosted(
                fraOgMed = start.plusDays(10),
                tilOgMed = start.plusDays(10),
                landnavn = "Norge",
                landkode = "NOR",
                erEØSLand = JaNei.Ja
            )
        ),
        opphold = listOf(
            Bosted(
                fraOgMed = start.plusDays(15),
                tilOgMed = start.plusDays(20),
                landnavn = "England",
                landkode = "Eng",
                erEØSLand = JaNei.Ja
            ),
            Bosted(
                fraOgMed = start.minusDays(10),
                tilOgMed = start.minusDays(5),
                landnavn = "Kroatia",
                landkode = "CRO",
                erEØSLand = JaNei.Ja
            )
        ),
        spørsmål = listOf(
            SpørsmålOgSvar(
                spørsmål = "Har du vært hjemme?",
                svar = JaNei.Ja
            ),
            SpørsmålOgSvar(
                spørsmål = "Skal du være hjemme?",
                svar = JaNei.Nei
            )
        ),
        utbetalingsperioder = listOf(
            Utbetalingsperiode(
                fraOgMed = start,
                tilOgMed = start.plusDays(10),
                aktivitetFravær = listOf(AktivitetFravær.FRILANSER),
                årsak = FraværÅrsak.ORDINÆRT_FRAVÆR
            ),
            Utbetalingsperiode(
                fraOgMed = start.plusDays(20),
                tilOgMed = start.plusDays(20),
                aktivitetFravær = listOf(AktivitetFravær.SELVSTENDIG_VIRKSOMHET),
                årsak = FraværÅrsak.SMITTEVERNHENSYN
            ),
            Utbetalingsperiode(
                fraOgMed = start.plusDays(30),
                tilOgMed = start.plusDays(35),
                aktivitetFravær = listOf(AktivitetFravær.FRILANSER, AktivitetFravær.SELVSTENDIG_VIRKSOMHET),
                årsak = FraværÅrsak.STENGT_SKOLE_ELLER_BARNEHAGE
            )
        ),
        vedleggId = listOf(
            "123",
            "456",
            "789"
        ),
        titler = listOf(
            "legeerklæring",
            "vedlegg 1",
            "vedlegg 2"
        ),
        bekreftelser = Bekreftelser(
            harBekreftetOpplysninger = JaNei.Ja,
            harForståttRettigheterOgPlikter = JaNei.Ja
        ),
        frilans = Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            sluttdato = LocalDate.parse("2022-10-01"),
            jobberFortsattSomFrilans = false
        ),
        selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            næringstype = Næringstyper.FISKE,
            fiskerErPåBladB = JaNei.Nei,
            fraOgMed = LocalDate.parse("2023-04-29"),
            erNyoppstartet = true,
            tilOgMed = LocalDate.parse("2023-05-09"),
            navnPåVirksomheten = "Kjells Møbelsnekkeri",
            registrertINorge = JaNei.Ja,
            næringsinntekt = 123456789,
            organisasjonsnummer = "111111",
            varigEndring = VarigEndring(
                dato = LocalDate.parse("2023-04-09"),
                inntektEtterEndring = 234543,
                forklaring = "Forklaring som handler om varig endring"
            ),
            harFlereAktiveVirksomheter = true

        ),
        erArbeidstakerOgså = true,
        k9FormatSøknad = defaultK9FormatOmsorgspengerutbetaling(søknadId, mottatt),
    )

    fun defaultK9FormatOmsorgspengerutbetaling(søknadId: String, mottatt: ZonedDateTime) = Søknad(
        SøknadId.of(søknadId),
        Versjon.of("1.1"),
        mottatt,
        no.nav.k9.søknad.felles.personopplysninger.Søker(NorskIdentitetsnummer.of("12345678910")),
        OmsorgspengerUtbetaling()
            .medFosterbarn(
                listOf(Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of("10987654321")))
            )
            .medFraværsperioder(
                listOf(
                    FraværPeriode(
                        Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-05")),
                        Duration.ofHours(7),
                        null,
                        no.nav.k9.søknad.felles.fravær.FraværÅrsak.STENGT_SKOLE_ELLER_BARNEHAGE,
                        null,
                        listOf(no.nav.k9.søknad.felles.fravær.AktivitetFravær.FRILANSER),
                        null,
                        null
                    ),
                    FraværPeriode(
                        Periode(LocalDate.parse("2020-01-06"), LocalDate.parse("2020-01-10")),
                        Duration.ofHours(4),
                        null,
                        no.nav.k9.søknad.felles.fravær.FraværÅrsak.SMITTEVERNHENSYN,
                        null,
                        listOf(no.nav.k9.søknad.felles.fravær.AktivitetFravær.SELVSTENDIG_VIRKSOMHET),
                        null,
                        null
                    )
                )
            )
            .medBosteder(
                Bosteder().medPerioder(
                    mapOf(
                        Periode(
                            LocalDate.parse("2020-01-01"),
                            LocalDate.parse("2020-01-05")
                        ) to Bosteder.BostedPeriodeInfo().medLand(Landkode.SPANIA),
                        Periode(
                            LocalDate.parse("2020-01-06"),
                            LocalDate.parse("2020-01-10")
                        ) to Bosteder.BostedPeriodeInfo().medLand(Landkode.NORGE)
                    )
                )
            )
            .medUtenlandsopphold(
                Utenlandsopphold().medPerioder(
                    mapOf(
                        Periode(
                            LocalDate.parse("2020-01-01"),
                            LocalDate.parse("2020-01-05")
                        ) to Utenlandsopphold.UtenlandsoppholdPeriodeInfo()
                            .medLand(Landkode.CANADA)
                            .medÅrsak(Utenlandsopphold.UtenlandsoppholdÅrsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD),
                        Periode(
                            LocalDate.parse("2020-01-06"),
                            LocalDate.parse("2020-01-10")
                        ) to Utenlandsopphold.UtenlandsoppholdPeriodeInfo()
                            .medLand(Landkode.SVERIGE)
                            .medÅrsak(Utenlandsopphold.UtenlandsoppholdÅrsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING)
                    )
                )
            )
            .medAktivitet(
                OpptjeningAktivitet()
                    .medFrilanser(
                        Frilanser().medStartdato(LocalDate.parse("2020-01-01"))
                    )
                    .medSelvstendigNæringsdrivende(
                        no.nav.k9.søknad.felles.opptjening.SelvstendigNæringsdrivende()
                            .medVirksomhetNavn("Mamsen Bamsen AS")
                            .medOrganisasjonsnummer(Organisasjonsnummer.of("12345678910112233444455667"))
                            .medPerioder(
                                mapOf(
                                    Periode(
                                        LocalDate.parse("2018-01-01"),
                                        LocalDate.parse("2020-01-01")
                                    ) to no.nav.k9.søknad.felles.opptjening.SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo()
                                        .medErNyoppstartet(true)
                                        .medRegistrertIUtlandet(false)
                                        .medBruttoInntekt(BigDecimal(5_000_000L))
                                        .medErVarigEndring(true)
                                        .medEndringDato(LocalDate.parse("2020-01-01"))
                                        .medEndringBegrunnelse("Grunnet Covid-19")
                                        .medLandkode(Landkode.NORGE)
                                        .medRegnskapsførerNavn("Regnskapsfører Svensen")
                                        .medRegnskapsførerTlf("+4799887766")
                                        .medVirksomhetstyper(listOf(VirksomhetType.DAGMAMMA))
                                        .medErNyIArbeidslivet(true)
                                )
                            )
                    )
            ),
    ).medKildesystem(Kildesystem.SØKNADSDIALOG)
}
