package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.utils

import no.nav.brukerdialog.ytelse.fellesdomene.Land
import no.nav.brukerdialog.ytelse.fellesdomene.Næringstype
import no.nav.brukerdialog.ytelse.fellesdomene.Regnskapsfører
import no.nav.brukerdialog.ytelse.fellesdomene.VarigEndring
import no.nav.brukerdialog.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.ytelse.fellesdomene.YrkesaktivSisteTreFerdigliknedeArene
import no.nav.brukerdialog.integrasjon.k9sakinnsynapi.Barn
import no.nav.brukerdialog.integrasjon.k9sakinnsynapi.K9SakInnsynSøknad
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Arbeidsgiver
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.BarnDetaljer
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.BarnRelasjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Beredskap
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Bosted
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Enkeltdag
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Ferieuttak
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.FerieuttakIPerioden
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Frilans
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.FrilansType
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Medlemskap
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Nattevåk
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Omsorgstilbud
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.OmsorgstilbudSvarFortid
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.OpptjeningIUtlandet
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.OpptjeningType
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Periode
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.PleiepengerSyktBarnSøknad
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.SelvstendigNæringsdrivende
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Språk
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Utenlandsopphold
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.UtenlandsoppholdIPerioden
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.ArbeidIPeriode
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.ArbeidIPeriodeType
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.Arbeidsforhold
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.NormalArbeidstid
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.fosterhjemgodtgjørelse.FosterhjemgodtgjørelseType
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.fosterhjemgodtgjørelse.FosterhjemsgodtgjørelseFrikjøpt
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.omsorgsstønad.OmsorgsstønadMottarDelerAvPerioden
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.omsorgsstønad.OmsorgsstønadType
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Årsak
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.psb.v1.ArbeiderIPeriodenSvar
import no.nav.k9.søknad.ytelse.psb.v1.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9.søknad.ytelse.psb.v1.UkjentArbeidsforhold
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*


class SøknadUtils {
    companion object {
        val søker = Søker(
            aktørId = "12345",
            fødselsdato = LocalDate.parse("2000-01-01"),
            fornavn = "Kjell",
            fødselsnummer = "25037139184"
        )

        fun defaultSøknad(søknadId: String = UUID.randomUUID().toString()) = PleiepengerSyktBarnSøknad(
            newVersion = null,
            apiDataVersjon = "1.0.0",
            søknadId = søknadId,
            mottatt = ZonedDateTime.of(2021, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")),
            språk = Språk.nb,
            barn = BarnDetaljer(
                fødselsnummer = "03028104560",
                fødselsdato = LocalDate.parse("2018-01-01"),
                navn = "Barn Barnesen",
                aktørId = null
            ),
            barnRelasjon = BarnRelasjon.ANNET,
            barnRelasjonBeskrivelse = "Gudfar til barnet",
            arbeidsgivere = listOf(
                Arbeidsgiver(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    erAnsatt = true,
                    arbeidsforhold = Arbeidsforhold(
                        normalarbeidstid = NormalArbeidstid(
                            timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                        ),
                        arbeidIPeriode = ArbeidIPeriode(
                            type = ArbeidIPeriodeType.ARBEIDER_VANLIG
                        )
                    )
                ),
                Arbeidsgiver(
                    navn = "JobberIkkeHerLenger",
                    organisasjonsnummer = "977155436",
                    erAnsatt = false,
                    sluttetFørSøknadsperiode = false
                )
            ),
            vedlegg = listOf(URI.create("http://localhost:8080/vedlegg/1").toURL()),
            fødselsattestVedleggUrls = listOf(URI.create("http://localhost:8080/vedlegg/2").toURL()),
            fraOgMed = LocalDate.parse("2021-01-01"),
            tilOgMed = LocalDate.parse("2021-10-01"),
            nattevåk = Nattevåk(
                harNattevåk = true,
                tilleggsinformasjon = "Æ har nattevåk"
            ),
            selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                harInntektSomSelvstendig = true,
                virksomhet = Virksomhet(
                    næringstype = Næringstype.ANNEN,
                    fiskerErPåBladB = false,
                    fraOgMed = LocalDate.parse("2021-01-01"),
                    næringsinntekt = 1111,
                    navnPåVirksomheten = "TullOgTøys",
                    registrertINorge = false,
                    registrertIUtlandet = Land(
                        landnavn = "Tyskland",
                        landkode = "DEU"
                    ),
                    varigEndring = VarigEndring(
                        inntektEtterEndring = 9999,
                        dato = LocalDate.parse("2020-01-01"),
                        forklaring = "Korona"
                    ),
                    regnskapsfører = Regnskapsfører(
                        "Kjell Regnskap",
                        "123456789"
                    ),
                    yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.parse("2018-01-01")),
                    harFlereAktiveVirksomheter = true,
                    erNyoppstartet = false
                ),
                arbeidsforhold = Arbeidsforhold(
                    normalarbeidstid = NormalArbeidstid(
                        timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                    ),
                    arbeidIPeriode = ArbeidIPeriode(
                        type = ArbeidIPeriodeType.ARBEIDER_VANLIG
                    )
                )
            ),
            omsorgstilbud = Omsorgstilbud(
                svarFortid = OmsorgstilbudSvarFortid.JA,
                erLiktHverUke = false,
                enkeltdager = listOf(
                    Enkeltdag(
                        LocalDate.parse("2021-01-01"),
                        Duration.ofHours(4)
                    ),
                    Enkeltdag(
                        LocalDate.parse("2021-01-02"),
                        Duration.ofHours(4)
                    ),
                    Enkeltdag(
                        LocalDate.parse("2021-01-03"),
                        Duration.ofHours(4)
                    ),
                    Enkeltdag(
                        LocalDate.parse("2021-01-04"),
                        Duration.ofHours(4)
                    )
                )
            ),
            medlemskap = Medlemskap(
                harBoddIUtlandetSiste12Mnd = true,
                skalBoIUtlandetNeste12Mnd = true,
                utenlandsoppholdNeste12Mnd = listOf(
                    Bosted(
                        fraOgMed = LocalDate.parse("2018-01-01"),
                        tilOgMed = LocalDate.parse("2018-01-10"),
                        landnavn = "Tyskland",
                        landkode = "DEU"
                    )
                ),
                utenlandsoppholdSiste12Mnd = listOf(
                    Bosted(
                        fraOgMed = LocalDate.parse("2017-01-01"),
                        tilOgMed = LocalDate.parse("2017-01-10"),
                        landnavn = "Tyskland",
                        landkode = "DEU"
                    )
                )
            ),
            beredskap = Beredskap(
                beredskap = true,
                tilleggsinformasjon = "Ikke beredskap"
            ),
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true,
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
                skalOppholdeSegIUtlandetIPerioden = true, opphold = listOf(
                    Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2019-10-10"),
                        tilOgMed = LocalDate.parse("2019-11-10"),
                        landkode = "SE",
                        landnavn = "Sverige",
                        erBarnetInnlagt = true,
                        perioderBarnetErInnlagt = listOf(
                            Periode(
                                fraOgMed = LocalDate.parse("2019-10-15"),
                                tilOgMed = LocalDate.parse("2019-10-20")
                            )
                        ),
                        erUtenforEøs = false,
                        erSammenMedBarnet = false,
                        årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD
                    ),
                    Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2020-11-10"),
                        tilOgMed = LocalDate.parse("2020-11-15"),
                        landkode = "SE",
                        landnavn = "Sverige",
                        erBarnetInnlagt = true,
                        perioderBarnetErInnlagt = listOf(
                            Periode(
                                fraOgMed = LocalDate.parse("2020-11-10"),
                                tilOgMed = LocalDate.parse("2020-11-12")
                            )
                        ),
                        erUtenforEøs = false,
                        erSammenMedBarnet = false,
                        årsak = Årsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_FOR_NORSK_OFFENTLIG_REGNING
                    ),
                    Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2022-12-10"),
                        tilOgMed = LocalDate.parse("2022-12-20"),
                        landkode = "SE",
                        landnavn = "Sverige",
                        erBarnetInnlagt = false,
                        erUtenforEøs = false,
                        erSammenMedBarnet = false,
                        årsak = null
                    )
                )
            ),
            ferieuttakIPerioden = FerieuttakIPerioden(
                skalTaUtFerieIPerioden = true, ferieuttak = listOf(
                    Ferieuttak(
                        fraOgMed = LocalDate.parse("2021-01-01"),
                        tilOgMed = LocalDate.parse("2021-01-10")
                    )
                )
            ),
            frilans = Frilans(
                jobberFortsattSomFrilans = true,
                harInntektSomFrilanser = true,
                startetFørSisteTreHeleMåneder = false,
                startdato = LocalDate.parse("2018-01-01"),
                misterHonorar = true,
                type = FrilansType.FRILANS,
                arbeidsforhold = Arbeidsforhold(
                    normalarbeidstid = NormalArbeidstid(
                        timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                    ),
                    arbeidIPeriode = ArbeidIPeriode(
                        type = ArbeidIPeriodeType.ARBEIDER_VANLIG
                    )
                )
            ),
            fosterhjemgodtgjørelse = FosterhjemsgodtgjørelseFrikjøpt(
                type = FosterhjemgodtgjørelseType.MOTTAR_FRIKJØPT,
                mottarFosterhjemsgodtgjørelse = true,
                erFrikjøptFraJobb = true,
                frikjøptBeskrivelse = "Frikjøpt fra jobb"
            ),
            omsorgsstønad = OmsorgsstønadMottarDelerAvPerioden(
                type = OmsorgsstønadType.MOTTAR_I_DELER_AV_PERIODEN,
                mottarOmsorgsstønad = true,
                startdato = LocalDate.parse("2021-01-02"),
                sluttdato = LocalDate.parse("2021-01-04"),
                antallTimerIUken = Duration.ofHours(25)
            ),
            opptjeningIUtlandet = listOf(
                OpptjeningIUtlandet(
                    navn = "Kiwi AS",
                    opptjeningType = OpptjeningType.ARBEIDSTAKER,
                    land = Land(
                        landkode = "BEL",
                        landnavn = "Belgia",
                    ),
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10")
                )
            ),
            utenlandskNæring = listOf(),
            harVærtEllerErVernepliktig = true,
            dataBruktTilUtledningAnnetData = "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
        )

        fun defaultK9FormatPSB(
            søknadId: UUID = UUID.randomUUID(),
            søknadsPeriode: List<no.nav.k9.søknad.felles.type.Periode> = listOf(
                no.nav.k9.søknad.felles.type.Periode(
                    LocalDate.parse("2021-01-01"),
                    LocalDate.parse("2021-01-01")
                )
            ),
            arbeidstid: Arbeidstid = Arbeidstid().medArbeidstaker(
                listOf(
                    Arbeidstaker()
                        .medNorskIdentitetsnummer(NorskIdentitetsnummer.of("12345678910"))
                        .medOrganisasjonsnummer(Organisasjonsnummer.of("926032925"))
                        .medArbeidstidInfo(
                            ArbeidstidInfo().medPerioder(
                                mapOf(
                                    no.nav.k9.søknad.felles.type.Periode(
                                        LocalDate.parse("2018-01-01"),
                                        LocalDate.parse("2020-01-05")
                                    ) to ArbeidstidPeriodeInfo()
                                        .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                        .medFaktiskArbeidTimerPerDag(Duration.ofHours(4)),
                                    no.nav.k9.søknad.felles.type.Periode(
                                        LocalDate.parse("2020-01-06"),
                                        LocalDate.parse("2020-01-10")
                                    ) to ArbeidstidPeriodeInfo()
                                        .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                        .medFaktiskArbeidTimerPerDag(Duration.ofHours(2))
                                )
                            )
                        )
                )
            ),
        ) = Søknad(

            SøknadId.of(søknadId.toString()),
            Versjon.of("1.0.0"),
            ZonedDateTime.parse("2020-01-01T10:00:00Z"),
            no.nav.k9.søknad.felles.personopplysninger.Søker(NorskIdentitetsnummer.of("12345678910")),
            PleiepengerSyktBarn()
                .medSøknadsperiode(søknadsPeriode)
                .medSøknadInfo(
                    DataBruktTilUtledning(
                        true, true, true, true, null, true,
                        listOf(
                            UkjentArbeidsforhold()
                                .medOrganisasjonsnummer(Organisasjonsnummer.of("926032925"))
                                .medErAnsatt(true)
                                .medArbeiderIPerioden(ArbeiderIPeriodenSvar.HELT_FRAVÆR)
                                .medNormalarbeidstid(
                                    no.nav.k9.søknad.ytelse.psb.v1.NormalArbeidstid()
                                        .medTimerPerUke(Duration.ofHours(8))
                                )
                        )
                    )
                )
                .medBarn(
                    no.nav.k9.søknad.felles.personopplysninger.Barn()
                        .medNorskIdentitetsnummer(NorskIdentitetsnummer.of("02119970079"))
                )
                .medArbeidstid(arbeidstid)
        )

        fun defaultK9SakInnsynSøknad(barn: Barn, søknad: Søknad) = K9SakInnsynSøknad(
            barn = barn,
            søknad = søknad
        )
    }
}
