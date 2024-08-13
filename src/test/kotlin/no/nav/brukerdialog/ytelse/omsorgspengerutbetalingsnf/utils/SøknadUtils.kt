package no.nav.brukerdialog.ytelse.omsorgspengerutbetalingsnf.utils

import no.nav.brukerdialog.ytelse.fellesdomene.AktivitetFravær
import no.nav.brukerdialog.ytelse.fellesdomene.Bekreftelser
import no.nav.brukerdialog.ytelse.fellesdomene.Bosted
import no.nav.brukerdialog.ytelse.fellesdomene.FraværÅrsak
import no.nav.brukerdialog.ytelse.fellesdomene.Land
import no.nav.brukerdialog.ytelse.fellesdomene.Næringstype
import no.nav.brukerdialog.ytelse.fellesdomene.Regnskapsfører
import no.nav.brukerdialog.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.brukerdialog.ytelse.fellesdomene.VarigEndring
import no.nav.brukerdialog.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.ytelse.fellesdomene.YrkesaktivSisteTreFerdigliknedeArene
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene.Barn
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene.Frilans
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene.OmsorgspengerutbetalingSnfSøknad
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene.SpørsmålOgSvar
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene.TypeBarn
import no.nav.brukerdialog.config.JacksonConfiguration
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

object SøknadUtils {
    val defaultSøknad = OmsorgspengerutbetalingSnfSøknad(
        språk = "nb",
        mottatt = ZonedDateTime.parse("2022-01-02T03:04:05.000Z", JacksonConfiguration.zonedDateTimeFormatter),
        bosteder = listOf(
            Bosted(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-10"),
                landkode = "NLD",
                landnavn = "Nederland",
                erEØSLand = true
            )
        ),
        opphold = listOf(
            Bosted(
                fraOgMed = LocalDate.parse("2022-02-01"),
                tilOgMed = LocalDate.parse("2022-02-10"),
                landkode = "BEL",
                landnavn = "Belgia",
                erEØSLand = true
            )
        ),
        spørsmål = listOf(
            SpørsmålOgSvar("Har du hund?", true)
        ),
        harDekketTiFørsteDagerSelv = null,
        bekreftelser = Bekreftelser(
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        ),
        utbetalingsperioder = listOf(
            Utbetalingsperiode(
                fraOgMed = LocalDate.parse("2022-01-20"),
                tilOgMed = LocalDate.parse("2022-01-25"),
                antallTimerBorte = Duration.ofHours(5),
                antallTimerPlanlagt = Duration.ofHours(7),
                årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                aktivitetFravær = listOf(AktivitetFravær.FRILANSER)
            ),
            Utbetalingsperiode(
                fraOgMed = LocalDate.parse("2022-01-20"),
                tilOgMed = LocalDate.parse("2022-01-25"),
                årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                aktivitetFravær = listOf(AktivitetFravær.SELVSTENDIG_VIRKSOMHET)
            )
        ),
        erArbeidstakerOgså = false,
        barn = listOf(
            Barn(
                navn = "Barnesen",
                fødselsdato = LocalDate.parse("2022-01-01"),
                type = TypeBarn.FOSTERBARN,
                aktørId = null,
                identitetsnummer = "26104500284"
            )
        ),
        frilans = Frilans(
            startdato = LocalDate.parse("2022-01-01"),
            sluttdato = LocalDate.parse("2022-10-01"),
            jobberFortsattSomFrilans = false
        ),
        selvstendigNæringsdrivende = Virksomhet(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-10-01"),
            næringstype = Næringstype.JORDBRUK_SKOGBRUK,
            næringsinntekt = 3_000_000,
            navnPåVirksomheten = "Kiwi ASA",
            organisasjonsnummer = "975959171",
            registrertINorge = false,
            registrertIUtlandet = Land("BEL", "Belgia"),
            yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(
                oppstartsdato = LocalDate.parse("2022-01-01")
            ),
            varigEndring = VarigEndring(
                dato = LocalDate.parse("2022-01-01"),
                inntektEtterEndring = 1_500_00,
                forklaring = "Fordi atte atte atte"
            ),
            regnskapsfører = Regnskapsfører(
                navn = "Knut",
                telefon = "123123123"
            ),
            erNyoppstartet = true,
            harFlereAktiveVirksomheter = true
        ),
        vedlegg = listOf(),
        dataBruktTilUtledningAnnetData = "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
    )
}
