package no.nav.brukerdialog.ytelse.opplæringspenger.utils

import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.ytelse.fellesdomene.Land
import no.nav.brukerdialog.ytelse.fellesdomene.Næringstype
import no.nav.brukerdialog.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.*
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.*
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Kursholder
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.felles.type.Periode as K9Periode


class SøknadUtils {
    companion object {
        val søker = Søker(
            aktørId = "12345",
            fødselsdato = LocalDate.parse("2000-01-01"),
            fornavn = "Kjell",
            fødselsnummer = "25037139184"
        )

        fun defaultSøknad(søknadId: String = UUID.randomUUID().toString()) = OpplæringspengerSøknad(
            newVersion = null,
            apiDataVersjon = "1.0.0",
            søknadId = søknadId,
            mottatt = ZonedDateTime.of(2024, 1, 10, 3, 4, 5, 6, ZoneId.of("UTC")),
            språk = Språk.nb,
            barn = BarnDetaljer(  // check
                norskIdentifikator = "03028104560",
                fødselsdato = LocalDate.parse("2018-01-01"),
                navn = "Barn Barnesen",
                aktørId = null,
                relasjonTilBarnet = BarnRelasjon.ANNET,
                relasjonTilBarnetBeskrivelse = "Gudfar til barnet",
                fødselsattestVedleggUrls = listOf(URI.create("http://localhost:8080/vedlegg/2").toURL()),
            ),
            arbeidsgivere = listOf(
                ArbeidsgiverOLP(
                    navn = "Org",
                    organisasjonsnummer = "917755736",
                    erAnsatt = true,
                    arbeidsforhold = ArbeidsforholdOLP(
                        jobberNormaltTimer = 40.0,
                        arbeidIPeriode = ArbeidIPeriode(
                            jobberIPerioden = JobberIPeriodeSvar.REDUSERT,
                            enkeltdager = listOf(
                                Enkeltdag(LocalDate.parse("2021-01-01"), Duration.ofHours(4)),
                                Enkeltdag(LocalDate.parse("2021-01-02"), Duration.ofHours(4)),
                                Enkeltdag(LocalDate.parse("2021-01-03"), Duration.ofHours(4)),
                                Enkeltdag(LocalDate.parse("2021-01-04"), Duration.ofHours(4)),
                                Enkeltdag(LocalDate.parse("2021-01-05"), Duration.ofHours(4)),
                                Enkeltdag(LocalDate.parse("2021-01-06"), Duration.ofHours(4)),
                                Enkeltdag(LocalDate.parse("2021-01-07"), Duration.ofHours(4)),
                                Enkeltdag(LocalDate.parse("2021-01-08"), Duration.ofHours(4)),
                                Enkeltdag(LocalDate.parse("2021-01-09"), Duration.ofHours(4)),
                                Enkeltdag(LocalDate.parse("2021-01-10"), Duration.ofHours(4)),
                            )
                        )
                    )
                ),
                ArbeidsgiverOLP(
                    navn = "JobberIkkeHerLenger",
                    organisasjonsnummer = "977155436",
                    erAnsatt = false,
                    sluttetFørSøknadsperiode = false
                )
            ),
            frilans = FrilansOLP(
                startdato = LocalDate.parse("2019-01-01"),
                jobberFortsattSomFrilans = false,
                sluttdato = LocalDate.parse("2021-05-01"),
                harHattInntektSomFrilanser = false
            ),
            selvstendigNæringsdrivende = SelvstendigNæringsdrivendeOLP(
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
                arbeidsforhold = ArbeidsforholdOLP(
                    37.5, ArbeidIPeriode(
                        JobberIPeriodeSvar.HELT_FRAVÆR,
                        listOf(
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
            vedlegg = listOf(URI.create("http://localhost:8080/vedlegg/1").toURL()),
            fraOgMed = LocalDate.parse("2021-01-01"),
            tilOgMed = LocalDate.parse("2021-01-10"),
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
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
                skalOppholdeSegIUtlandetIPerioden = true,
                opphold = listOf(
                    Utenlandsopphold(
                        fraOgMed = LocalDate.parse("2021-01-01"),
                        tilOgMed = LocalDate.parse("2021-01-10"),
                        landnavn = "Sverige",
                        landkode = "SWE"
                    )
                )
            ),
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true,
            ferieuttakIPerioden = FerieuttakIPerioden(
                skalTaUtFerieIPerioden = true, ferieuttak = listOf(
                    Ferieuttak(
                        fraOgMed = LocalDate.parse("2021-01-01"),
                        tilOgMed = LocalDate.parse("2021-01-10")
                    )
                )
            ),
            stønadGodtgjørelse = StønadGodtgjørelse(
                mottarStønadGodtgjørelse = true,
                startdato = LocalDate.parse("2018-01-01"),
                sluttdato = LocalDate.parse("2018-02-01")
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
            kurs = Kurs(
                kursholder = Kursholder(
                    UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8"),
                    "Senter for Kurs AS"
                ),
                kursperioder = listOf(
                    K9Periode(
                        LocalDate.parse("2021-01-01"),
                        LocalDate.parse("2021-01-10")
                    )
                ),
                reise = Reise(
                    reiserUtenforKursdager = true,
                    reisedager = listOf(
                        LocalDate.parse("2021-01-01"),
                        LocalDate.parse("2021-01-10")
                    ),
                    reisedagerBeskrivelse = "Reise til kurs tok mer enn en dag"
                )
            ),
            utenlandskNæring = listOf(),
            harVærtEllerErVernepliktig = true,
            dataBruktTilUtledningAnnetData = "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
        )
    }
}
