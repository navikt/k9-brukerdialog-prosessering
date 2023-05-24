package no.nav.k9brukerdialogprosessering.utils

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.opptjening.Frilanser
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.opptjening.SelvstendigNæringsdrivende
import no.nav.k9.søknad.felles.personopplysninger.Barn
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Søker
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.felles.type.VirksomhetType
import no.nav.k9.søknad.ytelse.psb.v1.ArbeiderIPeriodenSvar
import no.nav.k9.søknad.ytelse.psb.v1.Beredskap
import no.nav.k9.søknad.ytelse.psb.v1.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.psb.v1.LovbestemtFerie
import no.nav.k9.søknad.ytelse.psb.v1.Nattevåk
import no.nav.k9.søknad.ytelse.psb.v1.NormalArbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.Omsorg
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9.søknad.ytelse.psb.v1.UkjentArbeidsforhold
import no.nav.k9.søknad.ytelse.psb.v1.Uttak
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.TilsynPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.Tilsynsordning
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

object K9FormatUtils {
    fun defaultK9FormatPSB(søknadId: String, mottatt: ZonedDateTime) = Søknad(
        SøknadId.of(søknadId),
        Versjon.of("1.0.0"),
        mottatt,
        Søker(NorskIdentitetsnummer.of("12345678910")),
        PleiepengerSyktBarn()
            .medSøknadsperiode(Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-10")))
            .medSøknadInfo(
                DataBruktTilUtledning(
                    true, true, true, true, "commit-abc-123", true,
                    listOf(
                        UkjentArbeidsforhold()
                            .medOrganisasjonsnummer(Organisasjonsnummer.of("926032925"))
                            .medErAnsatt(true)
                            .medArbeiderIPerioden(ArbeiderIPeriodenSvar.HELT_FRAVÆR)
                            .medNormalarbeidstid(NormalArbeidstid().medTimerPerUke(Duration.ofHours(8)))
                    )
                )
            )
            .medBarn(Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of("10987654321")))
            .medOpptjeningAktivitet(
                OpptjeningAktivitet()
                    .medSelvstendigNæringsdrivende(
                        listOf(
                            SelvstendigNæringsdrivende()
                                .medOrganisasjonsnummer(Organisasjonsnummer.of("12345678910112233444455667"))
                                .medVirksomhetNavn("Mamsen Bamsen AS")
                                .medPerioder(
                                    mapOf(
                                        Periode(
                                            LocalDate.parse("2018-01-01"),
                                            LocalDate.parse("2020-01-01")
                                        ) to SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo()
                                            .medErNyoppstartet(true)
                                            .medRegistrertIUtlandet(false)
                                            .medBruttoInntekt(BigDecimal(5_000_000))
                                            .medErVarigEndring(true)
                                            .medEndringDato(LocalDate.parse("2020-01-01"))
                                            .medEndringBegrunnelse("Grunnet Covid-19")
                                            .medLandkode(Landkode.NORGE)
                                            .medRegnskapsførerNavn("Regnskapsfører Svensen")
                                            .medRegnskapsførerTlf("+4799887766")
                                            .medVirksomhetstyper(listOf(VirksomhetType.DAGMAMMA, VirksomhetType.ANNEN))
                                    )
                                ),
                            SelvstendigNæringsdrivende()
                                .medOrganisasjonsnummer(Organisasjonsnummer.of("54549049090490498048940940"))
                                .medVirksomhetNavn("Something Fishy AS")
                                .medPerioder(
                                    mapOf(
                                        Periode(
                                            LocalDate.parse("2015-01-01"),
                                            LocalDate.parse("2017-01-01")
                                        ) to SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo()
                                            .medErNyoppstartet(false)
                                            .medRegistrertIUtlandet(true)
                                            .medBruttoInntekt(BigDecimal(500_000))
                                            .medErVarigEndring(false)
                                            .medLandkode(Landkode.SPANIA)
                                            .medVirksomhetstyper(listOf(VirksomhetType.FISKE))
                                    )
                                )
                        )
                    )
                    .medFrilanser(Frilanser(LocalDate.parse("2020-01-01"), null))
            )
            .medBeredskap(
                Beredskap().medPerioder(
                    mapOf(
                        Periode(
                            LocalDate.parse("2020-01-01"),
                            LocalDate.parse("2020-01-05")
                        ) to Beredskap.BeredskapPeriodeInfo()
                            .medTilleggsinformasjon("Jeg skal være i beredskap. Basta!"),
                        Periode(
                            LocalDate.parse("2020-01-07"),
                            LocalDate.parse("2020-01-10")
                        ) to Beredskap.BeredskapPeriodeInfo()
                            .medTilleggsinformasjon("Jeg skal være i beredskap i denne perioden også. Basta!")
                    )
                )
            )
            .medNattevåk(
                Nattevåk().medPerioder(
                    mapOf(
                        Periode(
                            LocalDate.parse("2020-01-01"),
                            LocalDate.parse("2020-01-05")
                        ) to Nattevåk.NattevåkPeriodeInfo().medTilleggsinformasjon("Jeg skal ha nattevåk. Basta!"),
                        Periode(
                            LocalDate.parse("2020-01-07"),
                            LocalDate.parse("2020-01-10")
                        ) to Nattevåk.NattevåkPeriodeInfo()
                            .medTilleggsinformasjon("Jeg skal ha nattevåk i perioden også. Basta!")
                    )
                )
            )
            .medTilsynsordning(
                Tilsynsordning().medPerioder(
                    mapOf(
                        Periode(
                            LocalDate.parse("2020-01-01"),
                            LocalDate.parse("2020-01-05")
                        ) to TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(Duration.ofHours(8)),
                        Periode(
                            LocalDate.parse("2020-01-06"),
                            LocalDate.parse("2020-01-10")
                        ) to TilsynPeriodeInfo().medEtablertTilsynTimerPerDag(Duration.ofHours(4))
                    )
                )
            )
            .medArbeidstid(
                Arbeidstid().medArbeidstaker(
                    listOf(
                        Arbeidstaker()
                            .medNorskIdentitetsnummer(NorskIdentitetsnummer.of("12345678910"))
                            .medOrganisasjonsnummer(Organisasjonsnummer.of("926032925"))
                            .medArbeidstidInfo(
                                ArbeidstidInfo()
                                    .medPerioder(
                                        mapOf(
                                            Periode(
                                                LocalDate.parse("2018-01-01"),
                                                LocalDate.parse("2020-01-05")
                                            ) to ArbeidstidPeriodeInfo()
                                                .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                                .medFaktiskArbeidTimerPerDag(Duration.ofHours(4)),
                                            Periode(
                                                LocalDate.parse("2020-01-06"),
                                                LocalDate.parse("2020-01-10")
                                            ) to ArbeidstidPeriodeInfo()
                                                .medJobberNormaltTimerPerDag(Duration.ofHours(8))
                                                .medFaktiskArbeidTimerPerDag(Duration.ofHours(2))
                                        )
                                    )
                            )
                    )
                )
            )
            .medUttak(
                Uttak().medPerioder(
                    mapOf(
                        Periode(
                            LocalDate.parse("2020-01-01"),
                            LocalDate.parse("2020-01-05")
                        ) to Uttak.UttakPeriodeInfo(Duration.ofHours(4)),
                        Periode(
                            LocalDate.parse("2020-01-06"),
                            LocalDate.parse("2020-01-10")
                        ) to Uttak.UttakPeriodeInfo(Duration.ofHours(2))
                    )
                )
            )
            .medOmsorg(
                Omsorg()
                    .medRelasjonTilBarnet(Omsorg.BarnRelasjon.MOR)
                    .medBeskrivelseAvOmsorgsrollen("Blabla beskrivelse")
            )
            .medLovbestemtFerie(
                LovbestemtFerie().medPerioder(
                    mapOf(
                        Periode(
                            LocalDate.parse("2020-01-01"),
                            LocalDate.parse("2020-01-05")
                        ) to LovbestemtFerie.LovbestemtFeriePeriodeInfo(),
                        Periode(
                            LocalDate.parse("2020-01-06"),
                            LocalDate.parse("2020-01-10")
                        ) to LovbestemtFerie.LovbestemtFeriePeriodeInfo()
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
    )
}
