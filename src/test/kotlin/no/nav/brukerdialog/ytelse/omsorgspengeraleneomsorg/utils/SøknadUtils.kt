package no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.utils

import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene.Barn
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene.OmsorgsdagerAleneOmOmsorgenSøknad
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene.TidspunktForAleneomsorg
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene.TypeBarn
import no.nav.brukerdialog.config.JacksonConfiguration
import java.time.LocalDate
import java.time.ZonedDateTime

object SøknadUtils {
    val defaultSøknad = OmsorgsdagerAleneOmOmsorgenSøknad(
        mottatt = ZonedDateTime.parse("2020-01-02T03:04:05.000Z", JacksonConfiguration.zonedDateTimeFormatter),
        barn = listOf(
            Barn(
                navn = "Barn1",
                type = TypeBarn.FRA_OPPSLAG,
                aktørId = "123",
                identitetsnummer = "25058118020",
                tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE
            ),
            Barn(
                navn = "Barn2",
                type = TypeBarn.FOSTERBARN,
                aktørId = "123",
                identitetsnummer = "25058118020",
                fødselsdato = LocalDate.parse("2024-01-01"),
                tidspunktForAleneomsorg = TidspunktForAleneomsorg.SISTE_2_ÅRENE,
                dato = LocalDate.parse("2022-01-01")
            )
        ),
        språk = "nb",
        harForståttRettigheterOgPlikter = true,
        harBekreftetOpplysninger = true,
        dataBruktTilUtledningAnnetData = "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
    )
}
