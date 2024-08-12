package no.nav.brukerdialog.pleiepengerilivetssluttfase.utils

import no.nav.brukerdialog.api.ytelse.fellesdomene.Land
import no.nav.brukerdialog.api.ytelse.fellesdomene.Næringstype
import no.nav.brukerdialog.api.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.pleiepengerilivetssluttfase.api.domene.PILSTestUtils
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.ArbeidIPeriode
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.Arbeidsforhold
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.Arbeidsgiver
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.Enkeltdag
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.Frilans
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.JobberIPeriodeSvar
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.Medlemskap
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.OpptjeningIUtlandet
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.OpptjeningType
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.PleiepengerILivetsSluttfaseSøknad
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.Pleietrengende
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.SelvstendigNæringsdrivende
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.UtenlandskNæring
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.Utenlandsopphold
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.UtenlandsoppholdIPerioden
import no.nav.brukerdialog.config.JacksonConfiguration
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

object SøknadUtils {
    internal val defaultSøknad = PleiepengerILivetsSluttfaseSøknad(
        søknadId = "4e62f8de-1ff6-40e9-bdcd-10485c789094",
        mottatt = ZonedDateTime.parse("2022-01-02T03:04:05Z", JacksonConfiguration.zonedDateTimeFormatter),
        språk = "nb",
        vedleggUrls = listOf(URI.create("http://localhost:8080/vedlegg/1").toURL()),
        opplastetIdVedleggUrls = listOf(URI.create("http://localhost:8080/vedlegg/2").toURL()),
        pleietrengende = Pleietrengende(norskIdentitetsnummer = "06098523047", navn = "Bjarne"),
        fraOgMed = LocalDate.parse("2021-01-01"),
        tilOgMed = LocalDate.parse("2021-01-10"),
        skalJobbeOgPleieSammeDag = true,
        dagerMedPleie = listOf(
            LocalDate.parse("2021-01-01"),
            LocalDate.parse("2021-01-02"),
            LocalDate.parse("2021-01-03"),
            LocalDate.parse("2021-01-04"),
            LocalDate.parse("2021-01-05"),
            LocalDate.parse("2021-01-06"),
            LocalDate.parse("2021-01-07"),
            LocalDate.parse("2021-01-08"),
            LocalDate.parse("2021-01-09"),
            LocalDate.parse("2021-01-10"),
        ),
        medlemskap = Medlemskap(
            harBoddIUtlandetSiste12Mnd = true,
            utenlandsoppholdSiste12Mnd = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2021-01-01"),
                    tilOgMed = LocalDate.parse("2021-01-10"),
                    landnavn = "Brazil",
                    landkode = "BRA"
                )
            ),
            skalBoIUtlandetNeste12Mnd = true,
            utenlandsoppholdNeste12Mnd = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2023-01-01"),
                    tilOgMed = LocalDate.parse("2023-01-10"),
                    landnavn = "Cuba",
                    landkode = "CUB"
                )
            )
        ),
        utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2020-02-01"),
                    tilOgMed = LocalDate.parse("2020-02-09"),
                    landnavn = "Cuba",
                    landkode = "CUB"
                )
            )
        ),
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
                            Enkeltdag(LocalDate.parse("2021-01-04"), PILSTestUtils.INGEN_ARBEIDSDAG),
                            Enkeltdag(LocalDate.parse("2021-01-05"), PILSTestUtils.INGEN_ARBEIDSDAG),
                            Enkeltdag(LocalDate.parse("2021-01-06"), PILSTestUtils.INGEN_ARBEIDSDAG),
                            Enkeltdag(LocalDate.parse("2021-01-07"), PILSTestUtils.INGEN_ARBEIDSDAG),
                            Enkeltdag(LocalDate.parse("2021-01-08"), PILSTestUtils.INGEN_ARBEIDSDAG),
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
                    JobberIPeriodeSvar.HELT_FRAVÆR, listOf(
                        Enkeltdag(LocalDate.parse("2021-01-01"), PILSTestUtils.INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2021-01-02"), PILSTestUtils.INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2021-01-03"), PILSTestUtils.INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2021-01-04"), PILSTestUtils.INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2021-01-05"), PILSTestUtils.INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2021-01-06"), PILSTestUtils.INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2021-01-07"), PILSTestUtils.INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2021-01-08"), PILSTestUtils.INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2021-01-09"), PILSTestUtils.INGEN_ARBEIDSDAG),
                        Enkeltdag(LocalDate.parse("2021-01-10"), PILSTestUtils.INGEN_ARBEIDSDAG),
                    )
                )
            ),
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
        utenlandskNæring = listOf(
            UtenlandskNæring(
                næringstype = Næringstype.FISKE,
                navnPåVirksomheten = "Fiskeriet AS",
                land = Land(
                    landkode = "BEL",
                    landnavn = "Belgia",
                ),
                fraOgMed = LocalDate.parse("2020-01-01")
            )
        ),
        pleierDuDenSykeHjemme = true,
        harForståttRettigheterOgPlikter = true,
        harBekreftetOpplysninger = true,
        dataBruktTilUtledningAnnetData = "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
    )
}
