package no.nav.brukerdialog.ytelse.opplæringspenger.utils

import no.nav.brukerdialog.ytelse.fellesdomene.Land
import no.nav.brukerdialog.ytelse.fellesdomene.Næringstype
import no.nav.brukerdialog.ytelse.fellesdomene.Regnskapsfører
import no.nav.brukerdialog.ytelse.fellesdomene.VarigEndring
import no.nav.brukerdialog.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.ytelse.fellesdomene.YrkesaktivSisteTreFerdigliknedeArene
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.*
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.ArbeidIPeriode
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.ArbeidIPeriodeType
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.Arbeidsforhold
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.NormalArbeidstid
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
                    erNyoppstartet = true
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
                kursholder = Kursholder(navn = "Kiwi AS", id = "0edb9541-dda5-4dc4-bfaf-587d01448a6a"),
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
