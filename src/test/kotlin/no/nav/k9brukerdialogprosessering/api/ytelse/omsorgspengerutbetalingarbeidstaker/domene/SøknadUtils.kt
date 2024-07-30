package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.AktivitetFravær
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Bosted
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.FraværÅrsak
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.k9brukerdialogprosessering.config.JacksonConfiguration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

object SøknadUtils {
    val defaultSøknad = OmsorgspengerutbetalingArbeidstakerSøknad(
        søknadId = UUID.randomUUID().toString(),
        mottatt = ZonedDateTime.parse("2022-01-02T03:04:05.000Z", JacksonConfiguration.zonedDateTimeFormatter),
        språk = "nb",
        vedlegg = listOf(),
        bosteder = listOf(
            Bosted(
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-10"),
                landkode = "BEL",
                landnavn = "Belgia",
                erEØSLand = true
            )
        ),
        opphold = listOf(
            Bosted(
                fraOgMed = LocalDate.parse("2022-01-20"),
                tilOgMed = LocalDate.parse("2022-01-25"),
                landkode = "BEL",
                landnavn = "Belgia",
                erEØSLand = true
            )
        ),
        bekreftelser = Bekreftelser(
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        ),
        arbeidsgivere = listOf(
            Arbeidsgiver(
                navn = "Kiwi AS",
                organisasjonsnummer = "825905162",
                utbetalingsårsak = Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER,
                konfliktForklaring = "Fordi blablabla",
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = true,
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = LocalDate.parse("2022-01-25"),
                        tilOgMed = LocalDate.parse("2022-01-28"),
                        årsak = FraværÅrsak.SMITTEVERNHENSYN,
                        aktivitetFravær = listOf(AktivitetFravær.ARBEIDSTAKER)
                    )
                )
            )
        ),
        dineBarn = DineBarn(
            harDeltBosted = false,
            barn = listOf(
                Barn(
                    identitetsnummer = "11223344567",
                    aktørId = "1234567890",
                    LocalDate.now(),
                    "Barn Barnesen",
                    TypeBarn.FRA_OPPSLAG
                )
            ),
        ),
        hjemmePgaSmittevernhensyn = true,
        hjemmePgaStengtBhgSkole = true,
        dataBruktTilUtledningAnnetData = "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
    )
}
