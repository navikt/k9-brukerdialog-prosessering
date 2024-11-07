package no.nav.brukerdialog.ytelse.opplæringspenger.utils

import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.Arbeidsforhold
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Arbeidsgiver
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.Enkeltdag
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Frilans
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.JobberIPeriodeSvar
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.SelvstendigNæringsdrivende
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.ytelse.fellesdomene.*
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.*
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Bosted
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.ArbeidIPeriode

import java.net.URI
import java.time.*
import java.util.*


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
                Arbeidsgiver(
                    navn = "JobberIkkeHerLenger",
                    organisasjonsnummer = "977155436",
                    erAnsatt = false,
                    sluttetFørSøknadsperiode = false
                )
            ),
            frilans = Frilans(
                startdato = LocalDate.parse("2019-01-01"),
                jobberFortsattSomFrilans = false,
                sluttdato = LocalDate.parse("2021-05-01"),
                harHattInntektSomFrilanser = false
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
            fødselsattestVedleggUrls = listOf(URI.create("http://localhost:8080/vedlegg/2").toURL()),
            fraOgMed = LocalDate.parse("2021-01-01"),
            tilOgMed = LocalDate.parse("2021-10-01"),
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
                kursholder = Kursholder(navn = "Opplæring for kurs AS", id = "0edb9541-dda5-4dc4-bfaf-587d01448a6a"),
                perioder = listOf(
                    KursPerioderMedReiseTid(
                        fraOgMed = LocalDate.parse("2022-01-01").atStartOfDay(),
                        tilOgMed = LocalDate.parse("2022-01-10").atStartOfDay(),
                        avreise = LocalDate.parse("2022-01-01"),
                        hjemkomst = LocalDate.parse("2022-01-10"),
                    )
                )
            ),
            utenlandskNæring = listOf(),
            harVærtEllerErVernepliktig = true,
            dataBruktTilUtledningAnnetData = "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
        )
    }
}
