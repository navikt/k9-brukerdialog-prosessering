package no.nav.brukerdialog.ytelse.opplæringspenger.utils

import no.nav.brukerdialog.utils.K9FormatUtils
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.OLPMottattSøknad
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles.*
import no.nav.k9.søknad.felles.type.Periode
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime


object OlpPdfSøknadUtils {

    fun gyldigSøknad(soknadsId: String, mottatt: ZonedDateTime = ZonedDateTime.now()): OLPMottattSøknad {
        return OLPMottattSøknad(
            språk = "nb",
            søknadId = soknadsId,
            mottatt = mottatt,
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-02-01"),
            søker = Søker(
                aktørId = "123456",
                fornavn = "Ærling",
                mellomnavn = "ØVERBØ",
                etternavn = "ÅNSNES",
                fødselsnummer = "29099012345",
                fødselsdato = LocalDate.parse("1990-09-29"),
            ),
            barn = Barn(
                fødselsnummer = "02119970078", navn = "OLE DOLE", aktørId = "11111111111"
            ),
            vedleggId = listOf("123", "456"),
            medlemskap = Medlemskap(
                harBoddIUtlandetSiste12Mnd = true, utenlandsoppholdSiste12Mnd = listOf(
                    Bosted(
                        LocalDate.of(2020, 1, 2), LocalDate.of(2020, 1, 3), "US", "USA"
                    )
                ), skalBoIUtlandetNeste12Mnd = false
            ),
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true,
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
                skalOppholdeSegIUtlandetIPerioden = true, opphold = listOf(
                    Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2020-01-01"),
                        tilOgMed = LocalDate.parse("2020-01-10"),
                        landnavn = "Bahamas",
                        landkode = "BAH",
                        erUtenforEøs = true,
                        erBarnetInnlagt = true,
                        erSammenMedBarnet = true,
                        perioderBarnetErInnlagt = listOf(
                            Periode(
                                fraOgMed = LocalDate.parse("2020-01-01"), tilOgMed = LocalDate.parse("2020-01-01")
                            ), Periode(
                                fraOgMed = LocalDate.parse("2020-01-03"), tilOgMed = LocalDate.parse("2020-01-04")
                            )
                        ),
                        årsak = Årsak.ANNET
                    ), Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2020-01-01"),
                        tilOgMed = LocalDate.parse("2020-01-10"),
                        landnavn = "Sverige",
                        landkode = "BHS",
                        erUtenforEøs = false,
                        erBarnetInnlagt = true,
                        erSammenMedBarnet = true,
                        perioderBarnetErInnlagt = listOf(
                            Periode(
                                fraOgMed = LocalDate.parse("2020-01-01"), tilOgMed = LocalDate.parse("2020-01-01")
                            ), Periode(
                                fraOgMed = LocalDate.parse("2020-01-03"), tilOgMed = LocalDate.parse("2020-01-04")
                            ), Periode(
                                fraOgMed = LocalDate.parse("2020-01-05"), tilOgMed = LocalDate.parse("2020-01-05")
                            )
                        ),
                        årsak = Årsak.ANNET
                    )
                )
            ),
            ferieuttakIPerioden = FerieuttakIPerioden(
                skalTaUtFerieIPerioden = true, ferieuttak = listOf(
                    Ferieuttak(fraOgMed = LocalDate.parse("2020-01-01"), tilOgMed = LocalDate.parse("2020-01-05")),
                    Ferieuttak(fraOgMed = LocalDate.parse("2020-01-07"), tilOgMed = LocalDate.parse("2020-01-15")),
                    Ferieuttak(fraOgMed = LocalDate.parse("2020-02-01"), tilOgMed = LocalDate.parse("2020-02-05"))
                )
            ),
            utenlandskNæring = listOf(
                UtenlandskNæring(
                    næringstype = Næringstype.FISKE,
                    navnPåVirksomheten = "Fiskeriet AS",
                    land = Land(landkode = "NDL", landnavn = "Nederland"),
                    organisasjonsnummer = "123ABC",
                    fraOgMed = LocalDate.parse("2020-01-09")
                )
            ),

            stønadGodtgjørelse = StønadGodtgjørelse(
                mottarStønadGodtgjørelse = true,
                startdato = LocalDate.parse("2021-01-01"),
                sluttdato = LocalDate.parse("2021-01-21")
            ),
            arbeidsgivere = listOf(
                Arbeidsgiver(
                    navn = "Arbeidsplassen AS",
                    organisasjonsnummer = "917755736",
                    erAnsatt = true,
                    arbeidsforhold = Arbeidsforhold(
                        jobberNormaltTimer = 40.0, arbeidIPeriode = ArbeidIPeriode(
                            jobberIPerioden = JobberIPeriodeSvar.REDUSERT, enkeltdager = listOf(
                                Enkeltdag(LocalDate.parse("2022-01-01"), Duration.ofHours(4)),
                                Enkeltdag(LocalDate.parse("2022-01-02"), Duration.ofHours(4)),
                                Enkeltdag(LocalDate.parse("2022-01-03"), Duration.ofHours(4)),
                                Enkeltdag(LocalDate.parse("2022-01-04"), Duration.ofHours(4)),
                                Enkeltdag(LocalDate.parse("2022-02-01"), Duration.ofHours(4)),
                                Enkeltdag(LocalDate.parse("2022-02-02"), Duration.ofHours(4)),
                                Enkeltdag(LocalDate.parse("2022-04-10"), Duration.ofHours(4)),
                            )
                        )
                    )
                ), Arbeidsgiver(
                    navn = "JobberIkkeHerLenger AS",
                    organisasjonsnummer = "977155436",
                    erAnsatt = false,
                    sluttetFørSøknadsperiode = false
                )
            ),
            frilans = Frilans(
                startdato = LocalDate.parse("2019-01-01"),
                jobberFortsattSomFrilans = false,
                harHattInntektSomFrilanser = true,
                sluttdato = LocalDate.parse("2021-05-01"),
                arbeidsforhold = Arbeidsforhold(
                    jobberNormaltTimer = 7.5, arbeidIPeriode = ArbeidIPeriode(
                        jobberIPerioden = JobberIPeriodeSvar.HELT_FRAVÆR, enkeltdager = null
                    )
                )
            ),
            selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                virksomhet = Virksomhet(
                    fraOgMed = LocalDate.parse("2015-01-01"),
                    tilOgMed = LocalDate.parse("2021-01-01"),
                    næringstype = Næringstype.ANNEN,
                    fiskerErPåBladB = true,
                    navnPåVirksomheten = "Bjarnes Bakeri",
                    registrertINorge = false,
                    registrertIUtlandet = Land("CUB", "Cuba"),
                    næringsinntekt = 9656876,
                    erNyoppstartet = false,
                    harFlereAktiveVirksomheter = false
                ),
                arbeidsforhold = Arbeidsforhold(
                    37.5, ArbeidIPeriode(
                        JobberIPeriodeSvar.HELT_FRAVÆR, listOf(
                            Enkeltdag(LocalDate.parse("2021-01-01"), Duration.ofHours(7).plusMinutes(30)),
                            Enkeltdag(LocalDate.parse("2021-01-02"), Duration.ofHours(7).plusMinutes(30)),
                            Enkeltdag(LocalDate.parse("2021-01-03"), Duration.ofHours(7).plusMinutes(30)),
                            Enkeltdag(LocalDate.parse("2021-01-04"), Duration.ofHours(7).plusMinutes(30)),
                            Enkeltdag(LocalDate.parse("2021-01-05"), Duration.ofHours(7).plusMinutes(30)),
                            Enkeltdag(LocalDate.parse("2021-01-06"), Duration.ofHours(7).plusMinutes(30)),
                            Enkeltdag(LocalDate.parse("2021-01-07"), Duration.ofHours(7).plusMinutes(30)),
                            Enkeltdag(LocalDate.parse("2021-01-08"), Duration.ofHours(7).plusMinutes(30)),
                            Enkeltdag(LocalDate.parse("2021-01-09"), Duration.ofHours(7).plusMinutes(30)),
                            Enkeltdag(LocalDate.parse("2021-01-10"), Duration.ofHours(7).plusMinutes(30)),
                        )
                    )
                ),
            ),
            harVærtEllerErVernepliktig = true,
            barnRelasjon = BarnRelasjon.ANNET,
            barnRelasjonBeskrivelse = "Blaabla annet",
            k9FormatSøknad = K9FormatUtils.defaultK9FormatPSB(soknadsId, mottatt),
            kurs = Kurs(
                kursholder = Kursholder(
                    id = "0edb9541-dda5-4dc4-bfaf-587d01448a6a", navn = "Senter for Kurs AS"
                ), perioder = listOf(
                    KursPerioderMedReiseTid(
                        avreise = LocalDate.parse("2020-01-01"),
                        hjemkomst = LocalDate.parse("2020-01-10"),
                        kursperiode = Periode(
                            LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-10")
                        ),
                        beskrivelseReisetidTil = "Reisetid til kurs",
                        beskrivelseReisetidHjem = "Reisetid hjem fra kurs"
                    )
                )
            ),
            opptjeningIUtlandet = listOf(
                OpptjeningIUtlandet(
                    navn = "Yolo AS",
                    opptjeningType = OpptjeningType.ARBEIDSTAKER,
                    land = Land(landkode = "NDL", landnavn = "Nederland"),
                    fraOgMed = LocalDate.parse("2020-01-01"),
                    tilOgMed = LocalDate.parse("2020-10-01")
                )
            ),
        )
    }
}
