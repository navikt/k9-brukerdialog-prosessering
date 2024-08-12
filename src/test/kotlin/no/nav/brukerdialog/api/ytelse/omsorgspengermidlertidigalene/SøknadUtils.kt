package no.nav.brukerdialog.api.ytelse.omsorgspengermidlertidigalene

import no.nav.brukerdialog.api.ytelse.fellesdomene.Barn
import no.nav.brukerdialog.api.ytelse.omsorgspengermidlertidigalene.domene.AnnenForelder
import no.nav.brukerdialog.api.ytelse.omsorgspengermidlertidigalene.domene.OmsorgspengerMidlertidigAleneSøknad
import no.nav.brukerdialog.api.ytelse.omsorgspengermidlertidigalene.domene.Situasjon
import no.nav.brukerdialog.config.JacksonConfiguration
import java.time.LocalDate
import java.time.ZonedDateTime

object SøknadUtils {
    val defaultSøknad = OmsorgspengerMidlertidigAleneSøknad(
        id = "123456789",
        mottatt = ZonedDateTime.parse("2020-01-02T03:04:05.000Z", JacksonConfiguration.zonedDateTimeFormatter),
        språk = "nb",
        annenForelder = AnnenForelder(
            navn = "Berit",
            fnr = "02119970078",
            situasjon = Situasjon.FENGSEL,
            situasjonBeskrivelse = "Sitter i fengsel..",
            periodeOver6Måneder = false,
            periodeFraOgMed = LocalDate.parse("2020-01-01"),
            periodeTilOgMed = LocalDate.parse("2020-10-01")
        ),
        barn = listOf(
            Barn(
                navn = "Ole Dole",
                norskIdentifikator = "25058118020",
                aktørId = null
            )
        ),
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true,
        dataBruktTilUtledningAnnetData = "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
    )
}
