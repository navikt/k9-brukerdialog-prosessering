package no.nav.brukerdialog.ytelse.opplæringspenger.utils

import no.nav.brukerdialog.utils.K9FormatUtils
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.OLPMottattSøknad
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles.*
import no.nav.k9.søknad.felles.type.Periode
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*


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
                norskIdentifikator = "02119970078",
                navn = "OLE DOLE",
                aktørId = "11111111111",
                relasjonTilBarnet = BarnRelasjon.ANNET,
                relasjonTilBarnetBeskrivelse = "Blaabla annet",
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
                jobberFortsattSomFrilans = true,
                harHattInntektSomFrilanser = true,
                sluttdato = null,
                arbeidsforhold = Arbeidsforhold(
                    jobberNormaltTimer = 7.5, arbeidIPeriode = ArbeidIPeriode(
                        jobberIPerioden = JobberIPeriodeSvar.HELT_FRAVÆR, enkeltdager = listOf(
                            Enkeltdag(LocalDate.parse("2021-01-01"), Duration.ofHours(7).plusMinutes(30)),
                            Enkeltdag(LocalDate.parse("2021-01-02"), Duration.ofHours(7).plusMinutes(30)),
                            Enkeltdag(LocalDate.parse("2021-01-03"), Duration.ofHours(7).plusMinutes(30)))
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
                    jobberNormaltTimer= 37.5, arbeidIPeriode = ArbeidIPeriode(
                        jobberIPerioden = JobberIPeriodeSvar.HELT_FRAVÆR, enkeltdager = listOf(
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
            k9FormatSøknad = K9FormatUtils.defaultK9FormatPSB(soknadsId, mottatt),
            kurs = Kurs(
                kursholder = Kursholder(
                    UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8"),
                    "Senter for Kurs AS"
                ),
                kursperioder = listOf(
                    Periode(
                        LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-10")
                    )
                ),
                reise = Reise(
                    reiserUtenforKursdager = true,
                    reisedager = listOf(
                        LocalDate.parse("2020-01-01"),
                        LocalDate.parse("2020-01-10")
                    ),
                    reisedagerBeskrivelse = "Reisetid til kurs tok mer enn en dag"
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
