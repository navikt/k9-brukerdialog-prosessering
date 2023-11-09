package no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetssluttfase.utils

import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.opptjening.Frilanser
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold.UtenlandsoppholdPeriodeInfo
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.pls.v1.PleipengerLivetsSluttfase
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.ArbeidIPeriode
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Arbeidsforhold
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Arbeidsgiver
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Bosted
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Enkeltdag
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Ferieuttak
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.FerieuttakIPerioden
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.FlereSokereSvar
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Frilans
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.JobberIPeriodeSvar
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Land
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Medlemskap
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Næringstype
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.OpptjeningIUtlandet
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.OpptjeningType
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.PilsSøknadMottatt
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Pleietrengende
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Regnskapsfører
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.SelvstendigNæringsdrivende
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.UtenlandskNæring
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Utenlandsopphold
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.UtenlandsoppholdIPerioden
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.VarigEndring
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Virksomhet
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.YrkesaktivSisteTreFerdigliknedeArene
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as k9FormatSøknad
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold as K9Utenlandsopphold
import no.nav.k9.søknad.ytelse.pls.v1.Pleietrengende as K9Pleietrengende

object PilsSøknadUtils {

    fun gyldigSøknad(
        søkerFødselsnummer: String = "02119970078",
        søknadId: String = UUID.randomUUID().toString(),
        mottatt: ZonedDateTime = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
    ) = PilsSøknadMottatt(
        språk = "nb",
        søknadId = søknadId,
        mottatt = mottatt,
        søker = Søker(
            aktørId = "123456",
            fødselsnummer = søkerFødselsnummer,
            fødselsdato = LocalDate.parse("2000-01-01"),
            etternavn = "Nordmann",
            mellomnavn = "Mellomnavn",
            fornavn = "Ola"
        ),
        flereSokere = FlereSokereSvar.JA,
        fraOgMed = LocalDate.parse("2022-01-01"),
        tilOgMed = LocalDate.parse("2022-02-01"),
        pleierDuDenSykeHjemme = true,
        skalJobbeOgPleieSammeDag = true,
        dagerMedPleie = listOf(
            LocalDate.parse("2022-01-01"),
            LocalDate.parse("2022-01-02"),
            LocalDate.parse("2022-01-04"),
            LocalDate.parse("2022-01-11"),
            LocalDate.parse("2022-01-12"),
            LocalDate.parse("2022-01-13"),
            LocalDate.parse("2022-01-14"),
            LocalDate.parse("2022-01-15"),
            LocalDate.parse("2022-01-16"),
            LocalDate.parse("2022-01-17"),
            LocalDate.parse("2022-01-18"),
            LocalDate.parse("2022-01-19"),
            LocalDate.parse("2022-01-21"),
            LocalDate.parse("2022-01-22"),
            LocalDate.parse("2022-01-24"),
            LocalDate.parse("2022-01-25"),
            LocalDate.parse("2022-01-26"),
            LocalDate.parse("2022-01-27"),
            LocalDate.parse("2022-01-28"),
            LocalDate.parse("2022-01-29"),
            LocalDate.parse("2022-01-30"),
            LocalDate.parse("2022-01-31"),
            LocalDate.parse("2022-02-01"),
        ),
        vedleggId = listOf("123", "456"),
        opplastetIdVedleggId = listOf("987"),
        pleietrengende = Pleietrengende(norskIdentitetsnummer = "02119970078", navn = "Bjarne"),
        medlemskap = Medlemskap(
            harBoddIUtlandetSiste12Mnd = true,
            utenlandsoppholdSiste12Mnd = listOf(
                Bosted(
                    fraOgMed = LocalDate.parse("2020-01-01"),
                    tilOgMed = LocalDate.parse("2020-01-10"),
                    landnavn = "Brasil",
                    landkode = "BR"
                )
            ),
            skalBoIUtlandetNeste12Mnd = true,
            utenlandsoppholdNeste12Mnd = listOf(
                Bosted(
                    fraOgMed = LocalDate.parse("2021-01-01"),
                    tilOgMed = LocalDate.parse("2021-01-10"),
                    landnavn = "Cuba",
                    landkode = "CU"
                )
            )
        ),
        utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2021-01-01"),
                    tilOgMed = LocalDate.parse("2021-01-10"),
                    landnavn = "Cuba",
                    landkode = "CU"
                ),
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2021-02-01"),
                    tilOgMed = LocalDate.parse("2021-02-10"),
                    landnavn = "Cuba",
                    landkode = "CU"
                )
            )
        ),
        arbeidsgivere = listOf(
            Arbeidsgiver(
                navn = "Something Fishy AS",
                organisasjonsnummer = "123456789",
                erAnsatt = true,
                sluttetFørSøknadsperiode = false,
                arbeidsforhold = Arbeidsforhold(
                    jobberNormaltTimer = 7.5,
                    arbeidIPeriode = ArbeidIPeriode(
                        jobberIPerioden = JobberIPeriodeSvar.REDUSERT,
                        enkeltdager = listOf(Enkeltdag(LocalDate.parse("2022-01-01"), Duration.ofHours(4)))
                    )
                )
            ),
            Arbeidsgiver(
                navn = "Slutta",
                organisasjonsnummer = "12121212",
                sluttetFørSøknadsperiode = true,
                erAnsatt = false
            )
        ),
        frilans = Frilans(
            startdato = LocalDate.parse("2015-01-01"),
            jobberFortsattSomFrilans = false,
            harHattInntektSomFrilanser = true,
            sluttdato = LocalDate.parse("2021-01-01"),
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 7.5,
                arbeidIPeriode = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.HELT_FRAVÆR,
                    enkeltdager = null
                )
            )
        ),
        selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            virksomhet = Virksomhet(
                fraOgMed = LocalDate.parse("2015-01-01"),
                næringstype = Næringstype.FISKE,
                yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.parse("2020-03-04")),
                fiskerErPåBladB = false,
                navnPåVirksomheten = "Bjarnes Bakeri",
                registrertINorge = false,
                registrertIUtlandet = Land("ABW","Aruba"),
                næringsinntekt = 9656876,
                erNyoppstartet = false,
                harFlereAktiveVirksomheter = false,
                varigEndring = VarigEndring(
                    dato = LocalDate.parse("2019-09-09"),
                    inntektEtterEndring = 854875,
                    forklaring = "Opplevde en varig endring fordi....."
                ),
                regnskapsfører = Regnskapsfører(
                    navn = "Regn",
                    telefon = "987654321"
                ),
                tilOgMed = null,
                organisasjonsnummer = null
            ),
            arbeidsforhold = Arbeidsforhold(
                jobberNormaltTimer = 7.5,
                arbeidIPeriode = ArbeidIPeriode(
                    jobberIPerioden = JobberIPeriodeSvar.SOM_VANLIG,
                    enkeltdager = null
                )
            )
        ),
        harVærtEllerErVernepliktig = false,
        opptjeningIUtlandet = listOf(
            OpptjeningIUtlandet(
                navn = "Kiwi AS",
                opptjeningType = OpptjeningType.ARBEIDSTAKER,
                land = Land(
                    landkode = "IKKE GYLDIG",
                    landnavn = "Belgia",
                ),
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-10")
            )
        ),
        utenlandskNæring = listOf(
            UtenlandskNæring(
                næringstype = Næringstype.DAGMAMMA,
                navnPåVirksomheten = "Dagmamma AS",
                land = Land(landkode = "NDL", landnavn = "Nederland"),
                organisasjonsnummer = "123ABC",
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-10")
            )
        ),
        k9Format = gyldigK9Format(søknadId, mottatt),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true,
        ferieuttakIPerioden = FerieuttakIPerioden(
            skalTaUtFerieIPerioden = true,
            ferieuttak = listOf(
                Ferieuttak(fraOgMed = LocalDate.parse("2022-01-05"),
                tilOgMed = LocalDate.parse("2022-01-06"))
            )
        )
    )

    fun gyldigK9Format(søknadId: String = UUID.randomUUID().toString(), mottatt: ZonedDateTime) = k9FormatSøknad(
        SøknadId(søknadId),
        Versjon("1.0.0"),
        mottatt,
        K9Søker(NorskIdentitetsnummer.of("02119970078")),
        PleipengerLivetsSluttfase()
            .medPleietrengende(K9Pleietrengende().medNorskIdentitetsnummer(NorskIdentitetsnummer.of("02119970078")))
            .medUtenlandsopphold(
                K9Utenlandsopphold()
                    .medPerioder(
                        mapOf(
                            Periode(
                                LocalDate.parse("2021-03-01"),
                                LocalDate.parse("2021-03-03")
                            ) to UtenlandsoppholdPeriodeInfo().medLand(Landkode.CANADA)
                        )
                    )
            )
            .medBosteder(
                Bosteder()
                    .medPerioder(
                        mapOf(
                            Periode(
                                LocalDate.parse("2021-01-01"),
                                LocalDate.parse("2021-01-01")
                            ) to Bosteder.BostedPeriodeInfo().medLand(
                                Landkode.DANMARK
                            )
                        )
                    )
            )
            .medOpptjeningAktivitet(
                OpptjeningAktivitet()
                    .medFrilanser(
                        Frilanser()
                            .medStartdato(LocalDate.parse("2015-01-01"))
                            .medSluttdato(LocalDate.parse("2021-01-01"))
                    )
            )
    ).medKildesystem(Kildesystem.SØKNADSDIALOG)
}
